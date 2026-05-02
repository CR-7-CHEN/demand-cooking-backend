package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * Cooking complaint entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_complaint")
public class DcCookComplaint extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "complaint_id")
    private Long complaintId;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long chefId;

    private String complaintType;

    private String content;

    private String imageUrls;

    private String status;

    private String handleResult;

    private Long handlerId;

    private Date submitTime;

    private Date handleTime;

    private String remark;
}
