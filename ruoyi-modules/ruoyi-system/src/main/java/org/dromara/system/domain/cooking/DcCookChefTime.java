package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_chef_time")
public class DcCookChefTime extends TenantEntity {

    @TableId(value = "time_id")
    private Long timeId;

    private Long chefId;

    private Date startTime;

    private Date endTime;

    private String status;

    private String remark;

    @TableLogic
    private String delFlag;
}
