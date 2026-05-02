package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookOrder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Cooking order business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookOrder.class, reverseConvertGenerate = false)
public class DcCookOrderBo extends BaseEntity {

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

    private Date payDeadline;

    private BigDecimal payAmount;

    private String remark;
}
