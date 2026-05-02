package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_dish")
public class DcCookDish extends TenantEntity {

    @TableId(value = "dish_id")
    private Long dishId;

    private String dishName;

    private String category;

    private String cuisine;

    private String imageUrl;

    private String description;

    private String status;

    private Integer sort;

    private String remark;

    @TableLogic
    private String delFlag;
}
