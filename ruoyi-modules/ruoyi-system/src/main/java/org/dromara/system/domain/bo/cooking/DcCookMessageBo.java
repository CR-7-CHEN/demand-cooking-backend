package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookMessage;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookMessage.class, reverseConvertGenerate = false)
public class DcCookMessageBo extends BaseEntity {

    private Long messageId;

    @NotBlank(message = "messageType is required")
    private String messageType;

    @NotBlank(message = "channel is required")
    private String channel;

    @NotBlank(message = "receiverType is required")
    private String receiverType;

    private Long receiverId;

    private String receiverKeyword;

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
}
