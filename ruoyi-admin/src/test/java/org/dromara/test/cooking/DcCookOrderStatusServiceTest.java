package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.bo.cooking.DcCookOrderActionBo;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookMessage;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookAddressMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookMessageMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.impl.cooking.DcCookOrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cooking order status scheduled processing")
@Tag("dev")
public class DcCookOrderStatusServiceTest {

    @Test
    @DisplayName("uses pay timeout before objection timeout and updates only current target status")
    public void processTimeoutOrdersPrefersPayTimeoutForPriceObjection() {
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookMessageMapper messageMapper = mock(DcCookMessageMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, messageMapper);
        DcCookOrder objection = order(1L, DcCookOrderStatus.PRICE_OBJECTION);
        objection.setPayDeadline(minutesAgo(31));
        objection.setObjectionTime(minutesAgo(31));

        when(orderMapper.selectList(any(Wrapper.class)))
            .thenReturn(List.of())
            .thenReturn(List.of(objection))
            .thenReturn(List.of())
            .thenReturn(List.of())
            .thenReturn(List.of());
        when(orderMapper.update(any(DcCookOrder.class), any(Wrapper.class))).thenReturn(1);

        int processed = service.processScheduledStatusTransitions();

        assertEquals(1, processed);
        assertEquals(DcCookOrderStatus.PAY_TIMEOUT_CLOSED, objection.getStatus());
        verify(messageMapper).insert(any(DcCookMessage.class));
    }

    @Test
    @DisplayName("moves overdue service orders to waiting confirm and overdue confirmations to completed")
    public void processTimeoutOrdersMovesServiceAndConfirmationForward() {
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookMessageMapper messageMapper = mock(DcCookMessageMapper.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, chefMapper, messageMapper);
        DcCookOrder serviceOrder = order(2L, DcCookOrderStatus.WAITING_SERVICE);
        serviceOrder.setServiceEndTime(minutesAgo(1));
        DcCookOrder confirmOrder = order(3L, DcCookOrderStatus.WAITING_CONFIRM);
        confirmOrder.setServiceCompleteTime(hoursAgo(25));

        when(orderMapper.selectList(any(Wrapper.class)))
            .thenReturn(List.of())
            .thenReturn(List.of())
            .thenReturn(List.of())
            .thenReturn(List.of(serviceOrder))
            .thenReturn(List.of(confirmOrder));
        when(orderMapper.update(any(DcCookOrder.class), any(Wrapper.class))).thenReturn(1);

        int processed = service.processScheduledStatusTransitions();

        assertEquals(2, processed);
        assertEquals(DcCookOrderStatus.WAITING_CONFIRM, serviceOrder.getStatus());
        assertEquals(DcCookOrderStatus.COMPLETE_BY_SYSTEM, serviceOrder.getServiceCompleteType());
        assertEquals(DcCookOrderStatus.COMPLETED, confirmOrder.getStatus());
        verify(messageMapper, times(2)).insert(any(DcCookMessage.class));
        verify(chefMapper, never()).updateById(any(DcCookChef.class));
    }

    @Test
    @DisplayName("chef service complete keeps waiting confirm and preserves planned end time")
    public void serviceCompletePreservesPlannedEndTime() {
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookMessageMapper messageMapper = mock(DcCookMessageMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, messageMapper);
        DcCookOrder order = order(4L, DcCookOrderStatus.WAITING_SERVICE);
        order.setServiceStartTime(hoursAgo(1));
        Date plannedEndTime = hoursFromNow(2);
        order.setServiceEndTime(plannedEndTime);
        when(orderMapper.selectById(order.getOrderId())).thenReturn(order);
        when(orderMapper.updateById(any(DcCookOrder.class))).thenReturn(1);

        DcCookOrderActionBo bo = new DcCookOrderActionBo();
        bo.setOrderId(order.getOrderId());
        Date before = new Date();

        Boolean updated = service.serviceComplete(bo);

        Date after = new Date();
        assertTrue(updated);
        assertEquals(DcCookOrderStatus.WAITING_CONFIRM, order.getStatus());
        assertEquals(DcCookOrderStatus.COMPLETE_BY_CHEF, order.getServiceCompleteType());
        assertNotNull(order.getServiceCompleteTime());
        assertEquals(plannedEndTime, order.getServiceEndTime());
        assertTrue(!order.getServiceCompleteTime().before(before) && !order.getServiceCompleteTime().after(after));
        verify(messageMapper).insert(any(DcCookMessage.class));
    }

    @Test
    @DisplayName("chef service start records actual start fields without changing main status")
    public void serviceStartRecordsActualStartFields() throws Exception {
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookMessageMapper messageMapper = mock(DcCookMessageMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, messageMapper);
        DcCookOrder order = order(5L, DcCookOrderStatus.WAITING_SERVICE);
        order.setServiceStartTime(hoursAgo(1));
        order.setServiceEndTime(hoursFromNow(2));
        when(orderMapper.selectById(order.getOrderId())).thenReturn(order);
        when(orderMapper.updateById(any(DcCookOrder.class))).thenReturn(1);

        DcCookOrderActionBo bo = new DcCookOrderActionBo();
        bo.setOrderId(order.getOrderId());
        Date before = new Date();
        Method method = assertDoesNotThrow(() -> DcCookOrderServiceImpl.class.getMethod("serviceStart", DcCookOrderActionBo.class));

        Boolean updated = (Boolean) method.invoke(service, bo);

        Date after = new Date();
        assertTrue(updated);
        assertEquals(DcCookOrderStatus.WAITING_SERVICE, order.getStatus());
        assertEquals("1", order.getServiceStartedFlag());
        assertNotNull(order.getServiceStartedTime());
        assertTrue(!order.getServiceStartedTime().before(before) && !order.getServiceStartedTime().after(after));
        verify(orderMapper).updateById(order);
        verify(messageMapper).insert(any(DcCookMessage.class));
    }

    @Test
    @DisplayName("chef service complete rejects orders before actual service start")
    public void serviceCompleteRejectsBeforeActualServiceStart() {
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookMessageMapper messageMapper = mock(DcCookMessageMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, messageMapper);
        DcCookOrder order = order(6L, DcCookOrderStatus.WAITING_SERVICE);
        order.setServiceStartTime(hoursAgo(1));
        order.setServiceEndTime(hoursFromNow(4));
        when(orderMapper.selectById(order.getOrderId())).thenReturn(order);

        DcCookOrderActionBo bo = new DcCookOrderActionBo();
        bo.setOrderId(order.getOrderId());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.serviceComplete(bo));

        assertEquals("service has not started", ex.getMessage());
        verify(orderMapper, never()).updateById(any(DcCookOrder.class));
        verify(messageMapper, never()).insert(any(DcCookMessage.class));
    }

    @Test
    @DisplayName("chef can re-quote once after user objection")
    public void quoteAllowsSingleRequoteAfterObjection() {
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookMessageMapper messageMapper = mock(DcCookMessageMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, messageMapper);
        DcCookOrder order = order(8L, DcCookOrderStatus.PRICE_OBJECTION);
        order.setQuoteUpdateCount(1);
        when(orderMapper.selectById(order.getOrderId())).thenReturn(order);
        when(orderMapper.updateById(any(DcCookOrder.class))).thenReturn(1);

        DcCookOrderActionBo bo = new DcCookOrderActionBo();
        bo.setOrderId(order.getOrderId());
        bo.setQuoteAmount(new java.math.BigDecimal("128.00"));
        bo.setQuoteRemark("updated after objection");

        Boolean updated = assertDoesNotThrow(() -> service.quote(bo));

        assertTrue(updated);
        assertEquals(DcCookOrderStatus.WAITING_PAY, order.getStatus());
        assertEquals(2, order.getQuoteUpdateCount());
        assertEquals(new java.math.BigDecimal("128.00"), order.getQuoteAmount());
        assertEquals("updated after objection", order.getQuoteRemark());
        verify(orderMapper).updateById(order);
        verify(messageMapper).insert(any(DcCookMessage.class));
    }

    @Test
    @DisplayName("user cancel rejects orders after actual service start")
    public void userCancelRejectsAfterActualServiceStart() {
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookMessageMapper messageMapper = mock(DcCookMessageMapper.class);
        DcCookOrderServiceImpl service = newService(orderMapper, messageMapper);
        DcCookOrder order = order(7L, DcCookOrderStatus.WAITING_SERVICE);
        order.setPayAmount(new java.math.BigDecimal("88.00"));
        order.setServiceStartTime(hoursFromNow(2));
        order.setServiceStartedFlag("1");
        order.setServiceStartedTime(minutesAgo(5));
        when(orderMapper.selectById(order.getOrderId())).thenReturn(order);

        DcCookOrderActionBo bo = new DcCookOrderActionBo();
        bo.setOrderId(order.getOrderId());
        bo.setCancelReason("user changed plan");

        ServiceException ex = assertThrows(ServiceException.class, () -> service.userCancel(bo));

        assertEquals("service has already started", ex.getMessage());
        verify(orderMapper, never()).updateById(any(DcCookOrder.class));
        verify(messageMapper, never()).insert(any(DcCookMessage.class));
    }

    private DcCookOrderServiceImpl newService(DcCookOrderMapper orderMapper, DcCookMessageMapper messageMapper) {
        return newService(orderMapper, mock(DcCookChefMapper.class), messageMapper);
    }

    private DcCookOrderServiceImpl newService(DcCookOrderMapper orderMapper, DcCookChefMapper chefMapper,
                                              DcCookMessageMapper messageMapper) {
        IDcCookConfigService configService = mock(IDcCookConfigService.class);
        when(configService.selectConfigValueByKey("cooking.response.timeout.minutes")).thenReturn("30");
        when(configService.selectConfigValueByKey("cooking.pay.timeout.minutes")).thenReturn("30");
        when(configService.selectConfigValueByKey("cooking.confirm.timeout.hours")).thenReturn("24");
        return new DcCookOrderServiceImpl(
            orderMapper,
            chefMapper,
            mock(DcCookChefTimeMapper.class),
            mock(DcCookAddressMapper.class),
            messageMapper,
            mock(SysUserMapper.class),
            configService
        );
    }

    private DcCookOrder order(Long orderId, String status) {
        DcCookOrder order = new DcCookOrder();
        order.setOrderId(orderId);
        order.setOrderNo("OD" + orderId);
        order.setUserId(10L + orderId);
        order.setChefId(20L + orderId);
        order.setCreateTime(minutesAgo(60));
        order.setStatus(status);
        return order;
    }

    private Date minutesAgo(int minutes) {
        return new Date(System.currentTimeMillis() - minutes * 60_000L);
    }

    private Date hoursAgo(int hours) {
        return new Date(System.currentTimeMillis() - hours * 60 * 60_000L);
    }

    private Date hoursFromNow(int hours) {
        return new Date(System.currentTimeMillis() + hours * 60 * 60_000L);
    }
}
