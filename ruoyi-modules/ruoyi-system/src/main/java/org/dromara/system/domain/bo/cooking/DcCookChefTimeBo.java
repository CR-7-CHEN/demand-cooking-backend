package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookChefTime;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookChefTime.class, reverseConvertGenerate = false)
public class DcCookChefTimeBo extends BaseEntity {

    private Long timeId;

    private Long chefId;

    private Date startTime;

    private Date endTime;

    private String status;

    private String remark;
}
