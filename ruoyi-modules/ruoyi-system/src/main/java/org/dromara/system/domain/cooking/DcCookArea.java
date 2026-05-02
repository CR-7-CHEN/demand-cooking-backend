package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_area")
public class DcCookArea extends TenantEntity {

    @TableId(value = "area_id")
    private Long areaId;

    private String areaCode;

    private String areaName;

    private String parentCode;

    private String areaLevel;

    private String status;

    private Integer sort;

    private String remark;

    @TableLogic
    private String delFlag;
}
