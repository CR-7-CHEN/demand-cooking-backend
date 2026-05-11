package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.bo.cooking.DcCookOrderBo;
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.domain.bo.cooking.DcCookChefTimeBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookAddressMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookMessageMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.impl.cooking.DcCookChefServiceImpl;
import org.dromara.system.service.impl.cooking.DcCookChefTimeServiceImpl;
import org.dromara.system.service.impl.cooking.DcCookOrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Cooking reservation rules")
@Tag("dev")
public class DcCookReservationRulesTest {

    @Test
    @DisplayName("rejects overlapping non-terminal chef orders")
    void submitRejectsOverlappingChefLock() {
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefTimeMapper chefTimeMapper = mock(DcCookChefTimeMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, chefMapper, chefTimeMapper);
        when(chefMapper.selectById(20L)).thenReturn(approvedChef(20L));
        when(chefTimeMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(orderMapper.exists(any(Wrapper.class))).thenReturn(true);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.submit(orderBo()));

        assertEquals("chef is locked for this time range", ex.getMessage());
        verify(orderMapper, never()).insert(any(DcCookOrder.class));
    }

    @Test
    @DisplayName("defaults each reservation to a three hour lock")
    void submitCreatesThreeHourLock() {
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefTimeMapper chefTimeMapper = mock(DcCookChefTimeMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, chefMapper, chefTimeMapper);
        when(chefMapper.selectById(20L)).thenReturn(approvedChef(20L));
        when(chefTimeMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(orderMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(orderMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(orderMapper.insert(any(DcCookOrder.class))).thenReturn(1);

        DcCookOrderBo bo = orderBo();
        service.submit(bo);

        ArgumentCaptor<DcCookOrder> captor = ArgumentCaptor.forClass(DcCookOrder.class);
        verify(orderMapper).insert(captor.capture());
        long lockMillis = captor.getValue().getServiceEndTime().getTime() - bo.getServiceStartTime().getTime();
        assertEquals(3 * 60 * 60_000L, lockMillis);
    }

    @Test
    @DisplayName("requires chef availability to cover the full fixed three hour service window")
    void submitRequiresAvailableWindowToCoverThreeHours() {
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefTimeMapper chefTimeMapper = mock(DcCookChefTimeMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, chefMapper, chefTimeMapper);
        when(chefMapper.selectById(20L)).thenReturn(approvedChef(20L));
        when(chefTimeMapper.exists(any(Wrapper.class))).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.submit(orderBo()));

        assertEquals("reservation time is not available for this chef", ex.getMessage());
        verify(orderMapper, never()).insert(any(DcCookOrder.class));
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(chefTimeMapper).exists(wrapperCaptor.capture());
        @SuppressWarnings("unchecked")
        LambdaQueryWrapper<?> wrapper = (LambdaQueryWrapper<?>) wrapperCaptor.getValue();
        assertTrue(wrapper.getSqlSegment().contains("startTime"));
        assertTrue(wrapper.getSqlSegment().contains("endTime"));
    }

    @Test
    @DisplayName("generates order number from today's maximum sequence")
    void submitGeneratesOrderNoFromTodayMaxSequence() {
        initTableInfo(DcCookOrder.class);

        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefTimeMapper chefTimeMapper = mock(DcCookChefTimeMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, chefMapper, chefTimeMapper);
        when(chefMapper.selectById(20L)).thenReturn(approvedChef(20L));
        when(chefTimeMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(orderMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(orderMapper.selectList(any(Wrapper.class))).thenReturn(List.of(orderWithNo(todayOrderPrefix() + "0099")));
        when(orderMapper.insert(any(DcCookOrder.class))).thenReturn(1);

        service.submit(orderBo());

        ArgumentCaptor<DcCookOrder> captor = ArgumentCaptor.forClass(DcCookOrder.class);
        verify(orderMapper).insert(captor.capture());
        assertEquals(todayOrderPrefix() + "0100", captor.getValue().getOrderNo());
    }

    @Test
    @DisplayName("reservation overlap query ignores chef completed orders pending user confirmation")
    void submitOverlapQueryReleasesWaitingConfirmOrders() {
        initTableInfo(DcCookOrder.class);

        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefTimeMapper chefTimeMapper = mock(DcCookChefTimeMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, chefMapper, chefTimeMapper);
        when(chefMapper.selectById(20L)).thenReturn(approvedChef(20L));
        when(chefTimeMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(orderMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(orderMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(orderMapper.insert(any(DcCookOrder.class))).thenReturn(1);

        service.submit(orderBo());

        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(orderMapper).exists(wrapperCaptor.capture());
        @SuppressWarnings("unchecked")
        LambdaQueryWrapper<DcCookOrder> wrapper = (LambdaQueryWrapper<DcCookOrder>) wrapperCaptor.getValue();
        Map<String, Object> values = wrapper.getParamNameValuePairs();

        assertTrue(values.containsValue(DcCookOrderStatus.WAITING_CONFIRM));
        assertTrue(wrapper.getSqlSegment().contains("serviceCompleteTime"));
        assertTrue(wrapper.getSqlSegment().contains("status"));
    }

    @Test
    @DisplayName("rejects chef profile save when any available time is shorter than three hours")
    void chefProfileSaveRejectsAvailableTimeShorterThanThreeHours() {
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefTimeMapper chefTimeMapper = mock(DcCookChefTimeMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        DcCookChefServiceImpl service = new DcCookChefServiceImpl(
            chefMapper,
            chefTimeMapper,
            mock(DcCookOrderMapper.class),
            mock(org.dromara.system.mapper.cooking.DcCookReviewMapper.class),
            mock(org.dromara.system.mapper.cooking.DcCookSettlementMapper.class),
            mock(IDcCookConfigService.class),
            userMapper
        );
        when(chefMapper.insert(any(DcCookChef.class))).thenAnswer(invocation -> {
            DcCookChef chef = invocation.getArgument(0);
            chef.setChefId(88L);
            return 1;
        });

        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertByBo(chefBoWithShortAvailableTime()));

        assertEquals("availableTime must be at least 3 hours", ex.getMessage());
        verify(userMapper).exists(any(Wrapper.class));
        verify(chefMapper).insert(any(DcCookChef.class));
    }

    @Test
    @DisplayName("rejects single chef available time updates shorter than three hours")
    void chefTimeUpdateRejectsWindowShorterThanThreeHours() {
        DcCookChefTimeMapper chefTimeMapper = mock(DcCookChefTimeMapper.class);
        DcCookChefTimeServiceImpl service = new DcCookChefTimeServiceImpl(chefTimeMapper);
        DcCookChefTimeBo bo = shortChefTimeBo();
        bo.setTimeId(66L);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateByBo(bo));

        assertEquals("availableTime must be at least 3 hours", ex.getMessage());
        verifyNoInteractions(chefTimeMapper);
    }

    private DcCookOrderServiceImpl newService(DcCookOrderMapper orderMapper, DcCookChefMapper chefMapper,
                                              DcCookChefTimeMapper chefTimeMapper) {
        IDcCookConfigService configService = mock(IDcCookConfigService.class);
        when(configService.selectConfigValueByKey("cooking.service.duration.hours")).thenReturn("3");
        when(configService.selectConfigValueByKey("cooking.reserve.min.advance.minutes")).thenReturn("60");
        when(configService.selectConfigValueByKey("cooking.reserve.future.days")).thenReturn("3");
        return new DcCookOrderServiceImpl(
            orderMapper,
            chefMapper,
            chefTimeMapper,
            mock(DcCookAddressMapper.class),
            mock(DcCookMessageMapper.class),
            mock(SysUserMapper.class),
            configService
        );
    }

    private DcCookOrderBo orderBo() {
        DcCookOrderBo bo = new DcCookOrderBo();
        bo.setChefId(20L);
        bo.setUserId(10L);
        bo.setAddressId(30L);
        bo.setServiceStartTime(hoursFromNow(2));
        bo.setDishSnapshot("{}");
        return bo;
    }

    private DcCookChef approvedChef(Long chefId) {
        DcCookChef chef = new DcCookChef();
        chef.setChefId(chefId);
        chef.setAuditStatus("1");
        chef.setChefStatus("0");
        chef.setHealthCertExpireDate(hoursFromNow(48));
        return chef;
    }

    private DcCookChefBo chefBoWithShortAvailableTime() {
        DcCookChefBo bo = new DcCookChefBo();
        bo.setUserId(10L);
        bo.setChefName("Chef Test");
        bo.setMobile("13800138000");
        bo.setAvailableTimes(List.of(shortChefTimeBo()));
        return bo;
    }

    private DcCookChefTimeBo shortChefTimeBo() {
        DcCookChefTimeBo bo = new DcCookChefTimeBo();
        bo.setChefId(88L);
        bo.setRemark("早餐");
        bo.setStartTime(hoursFromNow(24));
        bo.setEndTime(new Date(bo.getStartTime().getTime() + (2 * 60 * 60_000L) + (30 * 60_000L)));
        return bo;
    }

    private Date hoursFromNow(int hours) {
        return new Date(System.currentTimeMillis() + hours * 60 * 60_000L);
    }

    private DcCookOrder orderWithNo(String orderNo) {
        DcCookOrder order = new DcCookOrder();
        order.setOrderNo(orderNo);
        return order;
    }

    private String todayOrderPrefix() {
        return "OD" + new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());
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
