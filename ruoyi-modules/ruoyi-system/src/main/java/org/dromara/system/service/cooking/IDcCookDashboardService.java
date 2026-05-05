package org.dromara.system.service.cooking;

import org.dromara.system.domain.vo.cooking.DcCookDashboardOverviewVo;

/**
 * Cooking dashboard service.
 */
public interface IDcCookDashboardService {

    DcCookDashboardOverviewVo overview(String trendMode);
}
