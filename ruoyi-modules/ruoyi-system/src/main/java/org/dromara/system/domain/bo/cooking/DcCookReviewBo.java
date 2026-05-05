package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookReview;

import java.math.BigDecimal;

/**
 * Cooking review business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookReview.class, reverseConvertGenerate = false)
public class DcCookReviewBo extends BaseEntity {

    private Long reviewId;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String userKeyword;

    private Long chefId;

    private String chefName;

    private BigDecimal rating;

    private String content;

    private String imageUrls;

    private String displayStatus;
}
