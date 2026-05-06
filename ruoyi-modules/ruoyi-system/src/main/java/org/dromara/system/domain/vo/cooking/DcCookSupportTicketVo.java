package org.dromara.system.domain.vo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookSupportTicket;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AutoMapper(target = DcCookSupportTicket.class)
public class DcCookSupportTicketVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long ticketId;

    private Long userId;

    private Long orderId;

    private String question;

    private String reply;

    private String status;

    private Long handlerId;

    private Date handleTime;

    private Date closeTime;

    private String remark;

    private Date createTime;
}
