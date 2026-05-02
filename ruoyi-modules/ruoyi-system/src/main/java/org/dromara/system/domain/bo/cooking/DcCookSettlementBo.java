package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookSettlement;

/**
 * Cooking settlement business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookSettlement.class, reverseConvertGenerate = false)
public class DcCookSettlementBo extends BaseEntity {

    private Long settlementId;

    private Long chefId;

    private String settlementMonth;

    private String status;

    private String manualFlag;
}
