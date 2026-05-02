package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Cooking order entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_order")
public class DcCookOrder extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id")
    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long chefId;

    private Long addressId;

    private String contactName;

    private String contactPhone;

    private String serviceArea;

    private String addressSnapshot;

    private String dishSnapshot;

    private String userRemark;

    private Date serviceStartTime;

    private Date serviceEndTime;

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
}
