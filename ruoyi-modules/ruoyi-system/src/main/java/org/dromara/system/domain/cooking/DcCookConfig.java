package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_config")
public class DcCookConfig extends TenantEntity {

    @TableId(value = "config_id")
    private Long configId;

    private String configName;

    private String configKey;

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
