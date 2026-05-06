package org.dromara.system.domain.vo.cooking;

import lombok.Data;

@Data
public class DcCookSupportAnswerVo {

    private String answerType;

    private String answer;

    private Long faqId;

    private Long orderId;

    private String orderNo;

    private String orderStatus;

    private Boolean ticketRequired;
}
