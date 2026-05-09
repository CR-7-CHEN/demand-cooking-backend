package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.system.domain.bo.cooking.DcCookSettlementBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookSettlement;
import org.dromara.system.domain.vo.cooking.DcCookSettlementVo;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookSettlementMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.impl.cooking.DcCookSettlementServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cooking settlement flow")
@Tag("dev")
public class DcCookSettlementFlowTest {

    @Test
    @DisplayName("apply review then keep then confirm then pay follows the lightweight settlement flow")
    void applyReviewThenKeepThenConfirmThenPayFollowsFlow() throws Exception {
        initTableInfo(DcCookSettlement.class);

        DcCookSettlementMapper settlementMapper = mock(DcCookSettlementMapper.class);
        DcCookSettlement settlement = new DcCookSettlement();
        settlement.setSettlementId(1L);
        settlement.setChefId(8L);
        settlement.setSettlementMonth("2026-05");
        settlement.setStatus("GENERATED");
        when(settlementMapper.selectById(1L)).thenReturn(settlement);
        when(settlementMapper.updateById(any(DcCookSettlement.class))).thenReturn(1);

        DcCookSettlementServiceImpl service = newService(settlementMapper, mock(DcCookOrderMapper.class), mock(DcCookChefMapper.class));

        invokeAction(service, "applyReview", actionBo(1L, bo -> {
            setProperty(bo, "reviewReasonType", "AMOUNT");
            setProperty(bo, "reviewRemark", "订单金额需要复核");
        }));
        assertEquals("REVIEWING", settlement.getStatus());
        assertEquals("AMOUNT", getProperty(settlement, "reviewReasonType"));
        assertEquals("订单金额需要复核", getProperty(settlement, "reviewRemark"));
        assertNotNull(getProperty(settlement, "reviewApplyTime"));

        invokeAction(service, "handleReview", actionBo(1L, bo -> {
            setProperty(bo, "reviewResult", "KEEP");
            setProperty(bo, "reviewReply", "平台已复核，无需重算");
        }));
        assertEquals("GENERATED", settlement.getStatus());
        assertEquals("KEEP", getProperty(settlement, "reviewResult"));
        assertEquals("平台已复核，无需重算", getProperty(settlement, "reviewReply"));
        assertNotNull(getProperty(settlement, "reviewHandleTime"));

        invokeAction(service, "confirm", actionBo(1L, bo -> {
        }));
        assertEquals("CONFIRMED", settlement.getStatus());
        assertNotNull(getProperty(settlement, "confirmTime"));

        invokeAction(service, "pay", actionBo(1L, bo -> setProperty(bo, "payRemark", "线下打款完成")));
        assertEquals("PAID", settlement.getStatus());
        assertEquals("线下打款完成", getProperty(settlement, "payRemark"));
        assertNotNull(getProperty(settlement, "payTime"));

        verify(settlementMapper, never()).insert(any(DcCookSettlement.class));
    }

    @Test
    @DisplayName("handle review with regenerate recalculates the current settlement row instead of inserting a new one")
    void handleReviewWithRegenerateRecalculatesCurrentRow() throws Exception {
        initTableInfo(DcCookSettlement.class);
        initTableInfo(DcCookOrder.class);
        initTableInfo(DcCookChef.class);

        DcCookSettlementMapper settlementMapper = mock(DcCookSettlementMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        IDcCookConfigService configService = mock(IDcCookConfigService.class);

        DcCookSettlement settlement = new DcCookSettlement();
        settlement.setSettlementId(2L);
        settlement.setChefId(8L);
        settlement.setSettlementMonth("2026-05");
        settlement.setStatus("REVIEWING");
        settlement.setManualFlag("N");
        when(settlementMapper.selectById(2L)).thenReturn(settlement);
        when(settlementMapper.updateById(any(DcCookSettlement.class))).thenReturn(1);

        DcCookChef chef = new DcCookChef();
        chef.setChefId(8L);
        chef.setBaseSalary(new BigDecimal("50.00"));
        when(chefMapper.selectById(8L)).thenReturn(chef);

        DcCookOrder order1 = new DcCookOrder();
        order1.setPayAmount(new BigDecimal("120.00"));
        DcCookOrder order2 = new DcCookOrder();
        order2.setPayAmount(new BigDecimal("80.00"));
        when(orderMapper.selectList(any())).thenReturn(List.of(order1, order2));
        when(orderMapper.selectCount(any())).thenReturn(0L);
        when(configService.selectConfigValueByKey("dc.cooking.platform.rate")).thenReturn("0.10");

        DcCookSettlementServiceImpl service = new DcCookSettlementServiceImpl(settlementMapper, orderMapper, chefMapper, configService);

        invokeAction(service, "handleReview", actionBo(2L, bo -> {
            setProperty(bo, "reviewResult", "REGENERATE");
            setProperty(bo, "reviewReply", "按当前源数据重算");
        }));

        assertEquals(2L, settlement.getSettlementId());
        assertEquals("GENERATED", settlement.getStatus());
        assertEquals(Integer.valueOf(2), settlement.getOrderCount());
        assertBigDecimal("200.00", settlement.getOrderAmount());
        assertBigDecimal("0.9000", settlement.getChefRate());
        assertBigDecimal("180.00", settlement.getChefCommission());
        assertBigDecimal("0.1000", settlement.getPlatformRate());
        assertBigDecimal("20.00", settlement.getPlatformCommission());
        assertBigDecimal("180.00", settlement.getFinalCommission());
        assertBigDecimal("230.00", settlement.getPayableAmount());
        assertEquals("REGENERATE", getProperty(settlement, "reviewResult"));
        assertEquals("按当前源数据重算", getProperty(settlement, "reviewReply"));
        assertNotNull(settlement.getGeneratedTime());
        assertNotNull(getProperty(settlement, "reviewHandleTime"));

        verify(settlementMapper, never()).insert(any(DcCookSettlement.class));
    }

    @Test
    @DisplayName("query by id normalizes legacy paid offline status to paid")
    void queryByIdNormalizesLegacyPaidOfflineStatus() {
        DcCookSettlementMapper settlementMapper = mock(DcCookSettlementMapper.class);
        DcCookSettlementVo vo = new DcCookSettlementVo();
        vo.setSettlementId(3L);
        vo.setStatus("PAID_OFFLINE");
        when(settlementMapper.selectVoById(3L)).thenReturn(vo);

        DcCookSettlementServiceImpl service = newService(settlementMapper, mock(DcCookOrderMapper.class), mock(DcCookChefMapper.class));

        DcCookSettlementVo result = service.queryById(3L);

        assertEquals("PAID", result.getStatus());
    }

    @Test
    @DisplayName("generate monthly settlements creates only missing chef rows with completed orders")
    void generateMonthlySettlementsCreatesOnlyMissingChefRowsWithCompletedOrders() {
        initTableInfo(DcCookSettlement.class);
        initTableInfo(DcCookOrder.class);
        initTableInfo(DcCookChef.class);

        DcCookSettlementMapper settlementMapper = mock(DcCookSettlementMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        IDcCookConfigService configService = mock(IDcCookConfigService.class);

        DcCookChef firstChef = chef(8L, "100.00");
        DcCookChef existingChef = chef(9L, "100.00");
        DcCookChef emptyChef = chef(10L, "100.00");
        when(chefMapper.selectList(any())).thenReturn(List.of(firstChef, existingChef, emptyChef));
        when(settlementMapper.exists(any())).thenReturn(false, true, false);
        when(orderMapper.selectList(any())).thenReturn(List.of(order("200.00")), List.of());
        when(orderMapper.selectCount(any())).thenReturn(0L);
        when(configService.selectConfigValueByKey(eq("dc.cooking.platform.rate"))).thenReturn("0.20");
        when(settlementMapper.insert(any(DcCookSettlement.class))).thenReturn(1);

        DcCookSettlementServiceImpl service = new DcCookSettlementServiceImpl(
            settlementMapper,
            orderMapper,
            chefMapper,
            configService
        );

        int generated = service.generateMonthlySettlements("2026-04");

        assertEquals(1, generated);
        ArgumentCaptor<DcCookSettlement> captor = forClass(DcCookSettlement.class);
        verify(settlementMapper).insert(captor.capture());
        DcCookSettlement settlement = captor.getValue();
        assertEquals(8L, settlement.getChefId());
        assertEquals("2026-04", settlement.getSettlementMonth());
        assertEquals("GENERATED", settlement.getStatus());
        assertEquals("N", settlement.getManualFlag());
        assertEquals(Integer.valueOf(1), settlement.getOrderCount());
        assertBigDecimal("200.00", settlement.getOrderAmount());
        assertBigDecimal("160.00", settlement.getChefCommission());
        assertBigDecimal("260.00", settlement.getPayableAmount());
        verify(orderMapper, times(2)).selectList(any());
    }

    private DcCookSettlementServiceImpl newService(
        DcCookSettlementMapper settlementMapper,
        DcCookOrderMapper orderMapper,
        DcCookChefMapper chefMapper
    ) {
        return new DcCookSettlementServiceImpl(
            settlementMapper,
            orderMapper,
            chefMapper,
            mock(IDcCookConfigService.class)
        );
    }

    private DcCookSettlementBo actionBo(Long settlementId, ThrowingConsumer<DcCookSettlementBo> customizer) throws Exception {
        DcCookSettlementBo bo = new DcCookSettlementBo();
        bo.setSettlementId(settlementId);
        customizer.accept(bo);
        return bo;
    }

    private void invokeAction(DcCookSettlementServiceImpl service, String methodName, DcCookSettlementBo bo) throws Exception {
        Method method = DcCookSettlementServiceImpl.class.getMethod(methodName, DcCookSettlementBo.class);
        Object result = method.invoke(service, bo);
        assertTrue(Boolean.TRUE.equals(result));
    }

    private void setProperty(Object target, String propertyName, Object value) {
        BeanWrapper wrapper = new BeanWrapperImpl(target);
        wrapper.setPropertyValue(propertyName, value);
    }

    private Object getProperty(Object target, String propertyName) {
        BeanWrapper wrapper = new BeanWrapperImpl(target);
        return wrapper.getPropertyValue(propertyName);
    }

    private DcCookChef chef(Long chefId, String baseSalary) {
        DcCookChef chef = new DcCookChef();
        chef.setChefId(chefId);
        chef.setAuditStatus("1");
        chef.setBaseSalary(new BigDecimal(baseSalary));
        return chef;
    }

    private DcCookOrder order(String payAmount) {
        DcCookOrder order = new DcCookOrder();
        order.setPayAmount(new BigDecimal(payAmount));
        return order;
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }

    private void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), entityClass.getName());
        assistant.setCurrentNamespace(entityClass.getName());
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T value) throws Exception;
    }
}
