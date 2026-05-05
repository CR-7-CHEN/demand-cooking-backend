package org.dromara.system.domain.vo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookReview;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Cooking review view object.
 */
@Data
@AutoMapper(target = DcCookReview.class)
public class DcCookReviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long reviewId;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String userName;

    private String nickName;

    private Long chefId;

    private String chefName;

    private BigDecimal rating;

    private String content;

    private String imageUrls;

    private String displayStatus;

    private String complaintAdjusted;

    private Date reviewTime;

    private String remark;

    private Date createTime;

    private Date updateTime;
}
