package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookSettlement;
import org.dromara.system.domain.vo.cooking.DcCookChefWorkbenchVo;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.mapper.cooking.DcCookSettlementMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.impl.cooking.DcCookChefServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Cooking chef workbench alerts")
@Tag("dev")
public class DcCookChefWorkbenchTest {

    @Test
    @DisplayName("workbench rejects user without chef profile as forbidden")
    void workbenchRejectsUserWithoutChefProfileAsForbidden() {
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefServiceImpl service = newService(chefMapper);
        when(chefMapper.selectOne(any(Wrapper.class), eq(false))).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.queryWorkbench(1L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getCode());
        assertEquals("请先申请成为做饭人员", exception.getMessage());
    }

    @Test
    @DisplayName("commission orders rejects user without chef profile as forbidden")
    void commissionOrdersRejectsUserWithoutChefProfileAsForbidden() {
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefServiceImpl service = newService(chefMapper);
        when(chefMapper.selectOne(any(Wrapper.class), eq(false))).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.queryCommissionOrders(1L, "2026-05"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getCode());
        assertEquals("请先申请成为做饭人员", exception.getMessage());
    }

    @Test
    @DisplayName("paused chef workbench excludes paused alert item")
    void pausedChefWorkbenchExcludesPausedAlertItem() {
        initTableInfo(DcCookChef.class);
        initTableInfo(DcCookOrder.class);
        initTableInfo(DcCookSettlement.class);

        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookSettlementMapper settlementMapper = mock(DcCookSettlementMapper.class);
        DcCookChefServiceImpl service = new DcCookChefServiceImpl(
            chefMapper,
            mock(DcCookChefTimeMapper.class),
            orderMapper,
            mock(DcCookReviewMapper.class),
            settlementMapper,
            mock(IDcCookConfigService.class)
        );

        DcCookChef chef = new DcCookChef();
        chef.setChefId(10L);
        chef.setUserId(1L);
        chef.setAuditStatus("1");
        chef.setChefStatus("1");
        chef.setHealthCertExpireDate(Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        when(chefMapper.selectOne(any(Wrapper.class), eq(false))).thenReturn(chef);
        when(orderMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(orderMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(settlementMapper.selectOne(any(Wrapper.class), eq(false))).thenReturn(null);

        DcCookChefWorkbenchVo workbench = service.queryWorkbench(1L);

        assertTrue(workbench.getAlerts().stream().anyMatch(item -> "health_cert_expiring".equals(item.getKey())));
        assertFalse(workbench.getAlerts().stream().anyMatch(item -> "paused".equals(item.getKey())));
    }

    @Test
    @DisplayName("today income uses chef commission instead of gross paid amount")
    void todayIncomeUsesChefCommissionInsteadOfGrossPaidAmount() {
        initTableInfo(DcCookChef.class);
        initTableInfo(DcCookOrder.class);
        initTableInfo(DcCookSettlement.class);

        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookSettlementMapper settlementMapper = mock(DcCookSettlementMapper.class);
        DcCookChefServiceImpl service = new DcCookChefServiceImpl(
            chefMapper,
            mock(DcCookChefTimeMapper.class),
            orderMapper,
            mock(DcCookReviewMapper.class),
            settlementMapper,
            mock(IDcCookConfigService.class)
        );

        DcCookChef chef = new DcCookChef();
        chef.setChefId(10L);
        chef.setUserId(1L);
        chef.setAuditStatus("1");
        chef.setChefStatus("0");
        chef.setHealthCertExpireDate(Date.from(LocalDate.now().plusDays(60).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        DcCookOrder order = new DcCookOrder();
        order.setOrderId(100L);
        order.setChefId(10L);
        order.setStatus("5");
        order.setPayAmount(new BigDecimal("88.00"));
        order.setPayTime(new Date());
        order.setCompleteTime(new Date());

        when(chefMapper.selectOne(any(Wrapper.class), eq(false))).thenReturn(chef);
        when(orderMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(orderMapper.selectList(any(Wrapper.class))).thenReturn(List.of(order));
        when(settlementMapper.selectOne(any(Wrapper.class), eq(false))).thenReturn(null);

        DcCookChefWorkbenchVo workbench = service.queryWorkbench(1L);

        assertEquals(new BigDecimal("70.40"), workbench.getRevenueOverview().getTodayIncome());
        assertEquals(new BigDecimal("70.40"), workbench.getRevenueOverview().getMonthCommissionAmount());
        assertEquals(new BigDecimal("70.40"), workbench.getRevenueTrend().get(workbench.getRevenueTrend().size() - 1).getAmount());
    }

    private DcCookChefServiceImpl newService(DcCookChefMapper chefMapper) {
        return new DcCookChefServiceImpl(
            chefMapper,
            mock(DcCookChefTimeMapper.class),
            mock(DcCookOrderMapper.class),
            mock(DcCookReviewMapper.class),
            mock(DcCookSettlementMapper.class),
            mock(IDcCookConfigService.class)
        );
    }

    private void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), entityClass.getName());
        assistant.setCurrentNamespace(entityClass.getName());
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }
}
