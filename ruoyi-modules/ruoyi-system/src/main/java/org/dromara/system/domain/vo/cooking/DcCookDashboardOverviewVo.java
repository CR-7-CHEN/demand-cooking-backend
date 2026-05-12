package org.dromara.system.domain.vo.cooking;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Cooking dashboard overview.
 */
@Data
public class DcCookDashboardOverviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long todayOrders;

    private BigDecimal todayRevenue;

    private Long serviceChefCount;

    private Long resignedChefCount;

    private Long registeredUserCount;

    private Long todayNewUserCount;

    private String trendMode;

    private List<TrendItem> revenueTrend;

    private List<PendingItem> pendingItems;

    private List<RecentOrderItem> recentOrders;

    private List<TopChefItem> topRatedChefs;

    @Data
    public static class TrendItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String label;

        private String date;

        private BigDecimal amount;
    }

    @Data
    public static class PendingItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String key;

        private String label;

        private String status;

        private Long count;

        private String tone;
    }

    @Data
    public static class RecentOrderItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String orderNo;

        private String status;

        private String statusLabel;

        private Date createTime;
    }

    @Data
    public static class TopChefItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long chefId;

        private String chefName;

        private BigDecimal rating;

        private Long completedOrders;

        private String avatarUrl;
    }
}
