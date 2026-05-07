package org.dromara.system.controller.cooking;

import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.service.ISysNoticeService;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Cooking config controller announcement")
class DcCookConfigControllerTest {

    @Test
    @DisplayName("announcement endpoint returns system notices for mini program carousel")
    void announcementReturnsSystemNotices() {
        IDcCookConfigService configService = mock(IDcCookConfigService.class);
        ISysNoticeService noticeService = mock(ISysNoticeService.class);
        DcCookConfigController controller = new DcCookConfigController(configService, noticeService);

        SysNoticeVo notice = new SysNoticeVo();
        notice.setNoticeId(10L);
        notice.setNoticeTitle("系统公告");
        notice.setNoticeContent("小程序工作台公告");
        when(noticeService.selectAppNoticeList()).thenReturn(List.of(notice));

        var response = controller.announcement();

        assertEquals(List.of(notice), response.getData());
    }
}
