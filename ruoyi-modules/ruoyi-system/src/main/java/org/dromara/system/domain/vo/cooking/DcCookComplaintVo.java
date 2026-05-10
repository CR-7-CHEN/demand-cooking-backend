package org.dromara.system.domain.vo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookComplaint;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Cooking complaint view object.
 */
@Data
@AutoMapper(target = DcCookComplaint.class)
public class DcCookComplaintVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long complaintId;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String userName;

    private String nickName;

    private Long chefId;

    private String chefName;

    private String complaintType;

    private String content;

    private String imageUrls;

    private String status;

    private String handleResult;

    private Long handlerId;

    private Date submitTime;

    private Date handleTime;

    private String remark;

    private Date createTime;

    private Date updateTime;
}
