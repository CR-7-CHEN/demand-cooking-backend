package org.dromara.system.domain.vo.cooking;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Chef monthly commission order detail view object.
 */
@Data
public class DcCookChefCommissionOrdersVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Summary summary;

    private List<Row> rows;

    @Data
    public static class Summary implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String month;

        private Integer orderCount;

        private BigDecimal commissionTotal;
    }

    @Data
    public static class Row implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long orderId;

        private String orderNo;

        private BigDecimal payAmount;

        private BigDecimal commissionAmount;

        private Date completeTime;

        private BigDecimal rating;

        private String reviewContent;

        private Date reviewTime;
    }
}
