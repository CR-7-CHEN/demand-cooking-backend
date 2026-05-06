package org.dromara.system.domain.bo.cooking;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.xss.Xss;

@Data
public class DcCookAppProfileBo {

    @Xss(message = "nickName cannot contain script characters")
    @Size(max = 30, message = "nickName length cannot exceed {max}")
    private String nickName;

    private Long avatar;
}
