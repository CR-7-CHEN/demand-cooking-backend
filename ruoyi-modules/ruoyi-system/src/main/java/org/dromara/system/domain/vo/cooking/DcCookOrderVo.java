package org.dromara.system.domain.vo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookOrder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Cooking order view object.
 */
@Data
@AutoMapper(target = DcCookOrder.class)
public class DcCookOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String userName;

    private String nickName;

    private Long chefId;

    private String chefName;

    private Long addressId;

    private String contactName;

    private String contactPhone;

    private String serviceArea;

    private String addressSnapshot;

    private String dishSnapshot;

    private String userRemark;

    private Date serviceStartTime;

    private Date serviceEndTime;

    private String serviceStartedFlag;

    private Date serviceStartedTime;

    private String status;

    private BigDecimal quoteAmount;

    private String quoteRemark;

    private Date quoteTime;

    private Integer quoteUpdateCount;

    private Date payDeadline;

    private BigDecimal payAmount;

    private String payNo;

    private Date payTime;

    private Integer objectionCount;

    private String objectionReason;

    private String objectionRemark;

    private Date objectionTime;

    private Date objectionHandleTime;

    private String cancelType;

    private String cancelReason;

    private Date cancelTime;

    private BigDecimal refundAmount;

    private BigDecimal refundFeeAmount;

    private BigDecimal refundFeeRate;

    private Date serviceCompleteTime;

    private String serviceCompleteType;

    private Date confirmTime;

    private Date completeTime;

    private String remark;

    private Date createTime;

    private Date updateTime;
}
