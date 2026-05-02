package org.dromara.system.domain.vo.cooking;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookArea;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcCookArea.class)
public class DcCookAreaVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "Area ID")
    private Long areaId;

    @ExcelProperty(value = "Area Code")
    private String areaCode;

    @ExcelProperty(value = "Area Name")
    private String areaName;

    private String parentCode;

    private String areaLevel;

    @ExcelProperty(value = "Status")
    private String status;

    private Integer sort;

    private String remark;

    private Date createTime;
}
