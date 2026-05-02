package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookConfig;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookConfig.class, reverseConvertGenerate = false)
public class DcCookConfigBo extends BaseEntity {

    private Long configId;

    @NotBlank(message = "configName is required")
    private String configName;

    @NotBlank(message = "configKey is required")
    private String configKey;

    @NotBlank(message = "configValue is required")
    private String configValue;

    private String valueType;

    private String configType;

    private String ruleFlag;

    private Date effectiveTime;

    private String publishStatus;

    private String changeReason;

    private String announcementContent;

    private String remark;
}
