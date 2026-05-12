package org.dromara.test.cooking;

import org.dromara.system.domain.vo.cooking.DcCookChefTimeVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.impl.cooking.DcCookChefServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@DisplayName("Chef available time text")
class DcCookChefAvailableTimeTextTest {

    @Test
    @DisplayName("includes all future available time segments in text output")
    void formatAvailableTimeTextIncludesAllSegments() throws Exception {
        DcCookChefServiceImpl service = new DcCookChefServiceImpl(
            mock(DcCookChefMapper.class),
            mock(DcCookChefTimeMapper.class),
            mock(DcCookOrderMapper.class),
            mock(org.dromara.system.mapper.cooking.DcCookReviewMapper.class),
            mock(org.dromara.system.mapper.cooking.DcCookSettlementMapper.class),
            mock(IDcCookConfigService.class),
            mock(SysUserMapper.class)
        );

        Method method = DcCookChefServiceImpl.class.getDeclaredMethod("formatAvailableTimeText", List.class);
        method.setAccessible(true);

        String text = (String) method.invoke(service, List.of(
            timeVo("2026-05-11 17:00:00", "2026-05-11 21:00:00"),
            timeVo("2026-05-12 07:00:00", "2026-05-12 11:00:00"),
            timeVo("2026-05-12 10:00:00", "2026-05-12 14:00:00"),
            timeVo("2026-05-12 17:00:00", "2026-05-12 21:00:00")
        ));

        assertEquals(
            "2026-05-11 17:00 - 2026-05-11 21:00; 2026-05-12 07:00 - 2026-05-12 11:00; 2026-05-12 10:00 - 2026-05-12 14:00; 2026-05-12 17:00 - 2026-05-12 21:00",
            text
        );
    }

    private DcCookChefTimeVo timeVo(String startTime, String endTime) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DcCookChefTimeVo vo = new DcCookChefTimeVo();
        vo.setStartTime(format.parse(startTime));
        vo.setEndTime(format.parse(endTime));
        return vo;
    }
}
