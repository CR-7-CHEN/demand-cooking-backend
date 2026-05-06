package org.dromara.system.domain.vo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookFaq;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AutoMapper(target = DcCookFaq.class)
public class DcCookFaqVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long faqId;

    private String category;

    private String question;

    private String answer;

    private String keywords;

    private Integer sort;

    private String status;

    private String remark;

    private Date createTime;
}
