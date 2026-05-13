package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_chef")
public class DcCookChef extends TenantEntity {

    @TableId(value = "chef_id")
    private Long chefId;

    private Long userId;

    private String chefName;

    private String gender;

    private Integer age;

    private String mobile;

    private String avatarUrl;

    private String workImageUrls;

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

    private Long auditBy;

    private Date auditTime;

    private String chefStatus;

    private String resignReason;

    private String remark;

    @TableLogic
    private String delFlag;
}
