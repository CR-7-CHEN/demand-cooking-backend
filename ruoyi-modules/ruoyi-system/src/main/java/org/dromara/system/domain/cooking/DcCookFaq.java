package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_faq")
public class DcCookFaq extends TenantEntity {

    @TableId(value = "faq_id")
    private Long faqId;

    private String category;

    private String question;

    private String answer;

    private String keywords;

    private Integer sort;

    private String status;

    private String remark;

    @TableLogic
    private String delFlag;
}
