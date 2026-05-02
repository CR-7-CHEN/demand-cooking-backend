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
@TableName("dc_cook_message")
public class DcCookMessage extends TenantEntity {

    @TableId(value = "message_id")
    private Long messageId;

    private String messageType;

    private String channel;

    private String receiverType;

    private Long receiverId;

    private String receiverMobileMask;

    private String receiverOpenidMask;

    private Long relatedOrderId;

    private String relatedOrderNo;

    private String relatedBizType;

    private Long relatedBizId;

    private String contentSummary;

    private String sendStatus;

    private Date sendTime;

    private String failReason;

    private String remark;

    @TableLogic
    private String delFlag;
}
