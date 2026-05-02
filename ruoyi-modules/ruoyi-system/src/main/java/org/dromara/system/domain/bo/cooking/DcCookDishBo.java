package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookDish;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookDish.class, reverseConvertGenerate = false)
public class DcCookDishBo extends BaseEntity {

    private Long dishId;

    @NotBlank(message = "dishName is required")
    private String dishName;

    private String category;

    private String cuisine;

    private String imageUrl;

    private String description;

    private String status;

    private Integer sort;

    private String remark;
}
