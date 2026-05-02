package org.dromara.system.domain.bo.cooking;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Cooking order action request body.
 */
@Data
public class DcCookOrderActionBo {

    private Long orderId;

    private Long userId;

    private Long chefId;

    private BigDecimal quoteAmount;

    private String quoteRemark;

    private String objectionReason;

    private String objectionRemark;

    private BigDecimal payAmount;

    private String payNo;

    private String cancelReason;

    private String failReason;
}
