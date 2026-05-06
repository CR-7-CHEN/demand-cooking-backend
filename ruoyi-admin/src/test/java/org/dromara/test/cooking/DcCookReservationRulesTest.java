package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.bo.cooking.DcCookOrderBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookOrder;
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
import org.mockito.ArgumentCaptor;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    private Date hoursFromNow(int hours) {
        return new Date(System.currentTimeMillis() + hours * 60 * 60_000L);
    }
}
