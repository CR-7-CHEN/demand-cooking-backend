package org.dromara.system.domain.vo.cooking;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookConfig;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcCookConfig.class)
public class DcCookConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "Config ID")
    private Long configId;

    @ExcelProperty(value = "Config Name")
    private String configName;

    @ExcelProperty(value = "Config Key")
    private String configKey;

    @ExcelProperty(value = "Config Value")
    private String configValue;

    private String valueType;

    private String configType;

    private String ruleFlag;

    private Date effectiveTime;

    private String publishStatus;

    private String changeReason;

    private String announcementContent;

    private String remark;

    private Date createTime;
}
