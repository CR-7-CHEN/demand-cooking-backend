package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookFaq;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookFaq.class, reverseConvertGenerate = false)
public class DcCookFaqBo extends BaseEntity {

    private Long faqId;

    @NotBlank(message = "category is required")
    private String category;

    @NotBlank(message = "question is required")
    private String question;

    @NotBlank(message = "answer is required")
    private String answer;

    private String keywords;

    private Integer sort;

    private String status;

    private String remark;
}
