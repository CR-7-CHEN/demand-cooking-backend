package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookChef;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookChef.class, reverseConvertGenerate = false)
public class DcCookChefBo extends BaseEntity {

    private Long chefId;

    private Long userId;

    @NotBlank(message = "realName is required")
    @Size(max = 64, message = "realName max length is 64")
    private String chefName;

    private String gender;

    private Integer age;

    @NotBlank(message = "mobile is required")
    @Size(max = 20, message = "mobile max length is 20")
    private String mobile;

    private String avatarUrl;

    private Long areaId;

    private String areaName;

    private String skillTags;

    private String intro;

    private String healthCertNo;

    private String healthCertImageUrl;

    private Date healthCertExpireDate;

    private BigDecimal baseSalary;

    private BigDecimal rating;

    private Long completedOrders;

    private String recommendFlag;

    private String auditStatus;

    private String auditReason;

    private String chefStatus;

    private String remark;
}
