package org.dromara.system.domain.vo.cooking;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Paid cancellation amount preview.
 */
@Data
public class DcCookOrderCancelPreviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private BigDecimal payAmount;

    private BigDecimal feeRate;

    private BigDecimal feeAmount;

    private BigDecimal refundAmount;

    private String refundNotice;
}
