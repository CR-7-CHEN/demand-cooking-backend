package org.dromara.test.cooking;

import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.vo.cooking.DcCookMessageVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookMessageMapper;
import org.dromara.system.service.impl.cooking.DcCookMessageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Cooking message service")
@Tag("dev")
public class DcCookMessageServiceTest {

    @Test
    @DisplayName("hydrates chef receiver name for message display")
    void hydratesChefReceiverName() {
        DcCookMessageMapper messageMapper = mock(DcCookMessageMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookMessageServiceImpl service = new DcCookMessageServiceImpl(messageMapper, userMapper, chefMapper);

        DcCookMessageVo message = new DcCookMessageVo();
        message.setMessageId(1L);
        message.setReceiverType("CHEF");
        message.setReceiverId(8L);
        when(messageMapper.selectVoById(1L)).thenReturn(message);

        DcCookChef chef = new DcCookChef();
        chef.setChefId(8L);
        chef.setChefName("火凤");
        when(chefMapper.selectList(any())).thenReturn(List.of(chef));

        DcCookMessageVo result = service.queryById(1L);

        assertEquals("火凤", result.getReceiverName());
        assertEquals("火凤", result.getChefName());
    }
}
