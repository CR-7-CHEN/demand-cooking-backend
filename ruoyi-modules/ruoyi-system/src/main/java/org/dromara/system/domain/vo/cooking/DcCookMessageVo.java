package org.dromara.system.domain.vo.cooking;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookMessage;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcCookMessage.class)
public class DcCookMessageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "Message ID")
    private Long messageId;

    @ExcelProperty(value = "Message Type")
    private String messageType;

    @ExcelProperty(value = "Channel")
    private String channel;

    @ExcelProperty(value = "Receiver Type")
    private String receiverType;

    private Long receiverId;

    private String receiverMobileMask;

    private String receiverOpenidMask;

    private Long relatedOrderId;

    private String relatedOrderNo;

    private String relatedBizType;

    private Long relatedBizId;

    @ExcelProperty(value = "Content Summary")
    private String contentSummary;

    @ExcelProperty(value = "Send Status")
    private String sendStatus;

    @ExcelProperty(value = "Send Time")
    private Date sendTime;

    private String failReason;

    private String remark;

    private Date createTime;
}
