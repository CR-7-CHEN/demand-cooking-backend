package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookSupportTicket;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookSupportTicket.class, reverseConvertGenerate = false)
public class DcCookSupportTicketBo extends BaseEntity {

    private Long ticketId;

    private Long userId;

    private Long orderId;

    @NotBlank(message = "question is required")
    private String question;

    private String reply;

    private String status;

    private Long handlerId;

    private String remark;
}
