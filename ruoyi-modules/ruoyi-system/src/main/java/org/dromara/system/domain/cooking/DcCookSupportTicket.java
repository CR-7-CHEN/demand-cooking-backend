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
@TableName("dc_cook_support_ticket")
public class DcCookSupportTicket extends TenantEntity {

    @TableId(value = "ticket_id")
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

    @TableLogic
    private String delFlag;
}
