package org.dromara.system.domain.vo.cooking;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookDish;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcCookDish.class)
public class DcCookDishVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "Dish ID")
    private Long dishId;

    @ExcelProperty(value = "Dish Name")
    private String dishName;

    @ExcelProperty(value = "Category")
    private String category;

    @ExcelProperty(value = "Cuisine")
    private String cuisine;

    private String imageUrl;

    private String description;

    @ExcelProperty(value = "Status")
    private String status;

    private Integer sort;

    private String remark;

    private Date createTime;
}
