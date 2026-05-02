package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookArea;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookArea.class, reverseConvertGenerate = false)
public class DcCookAreaBo extends BaseEntity {

    private Long areaId;

    private String areaCode;

    @NotBlank(message = "areaName is required")
    private String areaName;

    private String parentCode;

    private String areaLevel;

    private String status;

    private Integer sort;

    private String remark;
}
