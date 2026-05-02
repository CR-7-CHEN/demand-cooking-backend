package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookComplaint;

/**
 * Cooking complaint business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookComplaint.class, reverseConvertGenerate = false)
public class DcCookComplaintBo extends BaseEntity {

    private Long complaintId;

    private Long orderId;

    private Long userId;

    private Long chefId;

    private String complaintType;

    private String content;

    private String imageUrls;

    private String status;

    private Boolean established;

    private String handleResult;

    private Long handlerId;
}
