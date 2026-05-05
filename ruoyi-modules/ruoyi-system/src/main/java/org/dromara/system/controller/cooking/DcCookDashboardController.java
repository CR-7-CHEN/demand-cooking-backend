package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.system.domain.vo.cooking.DcCookDashboardOverviewVo;
import org.dromara.system.service.cooking.IDcCookDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cooking/dashboard")
public class DcCookDashboardController {

    private final IDcCookDashboardService dashboardService;

    @GetMapping("/overview")
    public R<DcCookDashboardOverviewVo> overview(@RequestParam(required = false, defaultValue = "week") String trendMode) {
        return R.ok(dashboardService.overview(trendMode));
    }
}
