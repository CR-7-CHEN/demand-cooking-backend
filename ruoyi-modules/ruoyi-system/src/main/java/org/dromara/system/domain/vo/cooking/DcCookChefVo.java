package org.dromara.system.domain.vo.cooking;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookChef;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcCookChef.class)
public class DcCookChefVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "Chef ID")
    private Long chefId;

    private Long userId;

    @ExcelProperty(value = "Chef Name")
    private String chefName;

    private String gender;

    private Integer age;

    @ExcelProperty(value = "Mobile")
    private String mobile;

    private String avatarUrl;

    private Long areaId;

    @ExcelProperty(value = "Area Name")
    private String areaName;

    @ExcelProperty(value = "Skill Tags")
    private String skillTags;

    private String intro;

    private String healthCertNo;

    private String healthCertImageUrl;

    @ExcelProperty(value = "Health Expire Date")
    private Date healthCertExpireDate;

    private String workImageUrls;

    @ExcelProperty(value = "Base Salary")
    private BigDecimal baseSalary;

    @ExcelProperty(value = "Rating")
    private BigDecimal rating;

    @ExcelProperty(value = "Completed Orders")
    private Long completedOrders;

    @ExcelProperty(value = "Recommend")
    private String recommendFlag;

    @ExcelProperty(value = "Audit Status")
    private String auditStatus;

    private String auditReason;

    @ExcelProperty(value = "Chef Status")
    private String chefStatus;

    private String resignReason;

    private String remark;

    private String availableTimeText;

    private List<DcCookChefTimeVo> availableTimes;

    private Date createTime;
}
