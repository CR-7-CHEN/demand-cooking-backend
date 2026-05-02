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
 * Cooking monthly settlement entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_settlement")
public class DcCookSettlement extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "settlement_id")
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
}
