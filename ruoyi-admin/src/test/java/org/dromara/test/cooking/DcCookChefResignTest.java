package org.dromara.test.cooking;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.cooking.DcCookChef;
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
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cooking chef resign")
@Tag("dev")
public class DcCookChefResignTest {

    @Test
    @DisplayName("resign requires reason")
    void resignRequiresReason() {
        DcCookChefServiceImpl service = newService(mock(DcCookChefMapper.class), mock(DcCookOrderMapper.class));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.resign(1L, " "));

        assertEquals("resignReason is required", exception.getMessage());
    }

    @Test
    @DisplayName("resign stores trimmed reason")
    void resignStoresTrimmedReason() {
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookChefServiceImpl service = newService(chefMapper, orderMapper);
        DcCookChef chef = new DcCookChef();
        chef.setChefId(10L);
        chef.setUserId(1L);
        when(chefMapper.selectOne(any(), eq(false))).thenReturn(chef);
        when(orderMapper.exists(any())).thenReturn(false);
        when(chefMapper.updateById(any(DcCookChef.class))).thenReturn(1);

        service.resign(1L, "  时间安排不过来  ");

        ArgumentCaptor<DcCookChef> captor = ArgumentCaptor.forClass(DcCookChef.class);
        verify(chefMapper).updateById(captor.capture());
        assertEquals("3", captor.getValue().getChefStatus());
        assertEquals("时间安排不过来", captor.getValue().getResignReason());
    }

    private DcCookChefServiceImpl newService(DcCookChefMapper chefMapper, DcCookOrderMapper orderMapper) {
        return new DcCookChefServiceImpl(
            chefMapper,
            mock(DcCookChefTimeMapper.class),
            orderMapper,
            mock(DcCookReviewMapper.class),
            mock(DcCookSettlementMapper.class),
            mock(IDcCookConfigService.class)
        );
    }
}
