package org.dromara.system.domain.vo.cooking;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Chef app workbench view object.
 */
@Data
public class DcCookChefWorkbenchVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String chefStatus;

    private String auditStatus;

    private Boolean takingOrders;

    private RevenueOverview revenueOverview;

    private List<AlertItem> alerts;

    private List<TrendItem> revenueTrend;

    @Data
    public static class RevenueOverview implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private BigDecimal todayIncome;

        private BigDecimal monthIncome;

        private BigDecimal monthCommissionAmount;

        private Long monthCompletedOrders;

        private BigDecimal monthDeduction;

        private BigDecimal monthPayableAmount;
    }

    @Data
    public static class AlertItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String key;

        private String title;

        private String content;

        private String tone;

        private Long count;
    }

    @Data
    public static class TrendItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String date;

        private String label;

        private BigDecimal amount;
    }
}
