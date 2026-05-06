package org.dromara.system.domain.bo.cooking;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DcCookSupportAskBo {

    @NotBlank(message = "question is required")
    private String question;

    private Long orderId;
}
