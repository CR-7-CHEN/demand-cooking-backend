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
 * Cooking review entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_review")
public class DcCookReview extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "review_id")
    private Long reviewId;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long chefId;

    private BigDecimal rating;

    private String content;

    private String imageUrls;

    private String displayStatus;

    private String complaintAdjusted;

    private Date reviewTime;

    private String remark;
}
