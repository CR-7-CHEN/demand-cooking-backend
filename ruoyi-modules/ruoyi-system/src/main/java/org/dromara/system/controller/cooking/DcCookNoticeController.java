package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.service.ISysNoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class DcCookNoticeController {

    private final ISysNoticeService noticeService;

    @GetMapping("/cooking/notice/announcement")
    public R<List<SysNoticeVo>> announcement() {
        return R.ok(noticeService.selectAppNoticeList());
    }
}
