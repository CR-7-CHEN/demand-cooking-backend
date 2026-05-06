package org.dromara.system.domain.vo.cooking;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookChefTime;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcCookChefTime.class)
public class DcCookChefTimeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "Time ID")
    private Long timeId;

    @ExcelProperty(value = "Chef ID")
    private Long chefId;

    @ExcelProperty(value = "Start Time")
    private Date startTime;

    @ExcelProperty(value = "End Time")
    private Date endTime;

    @ExcelProperty(value = "Status")
    private String status;

    private String remark;

    private Date createTime;
}
