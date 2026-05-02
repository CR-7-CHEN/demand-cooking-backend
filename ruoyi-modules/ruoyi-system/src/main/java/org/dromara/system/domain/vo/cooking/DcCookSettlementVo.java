package org.dromara.system.domain.vo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookSettlement;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Cooking settlement view object.
 */
@Data
@AutoMapper(target = DcCookSettlement.class)
public class DcCookSettlementVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long settlementId;

    private Long chefId;

    private String settlementMonth;

    private BigDecimal baseSalary;

    private Integer orderCount;

    private BigDecimal orderAmount;

    private BigDecimal chefRate;

    private BigDecimal chefCommission;

    private BigDecimal platformRate;

    private BigDecimal platformCommission;

    private Integer violationCount;

    private BigDecimal violationDeduction;

    private BigDecimal finalCommission;

    private BigDecimal payableAmount;

    private String status;

    private String manualFlag;

    private Date generatedTime;

    private String remark;

    private Date createTime;

    private Date updateTime;
}
