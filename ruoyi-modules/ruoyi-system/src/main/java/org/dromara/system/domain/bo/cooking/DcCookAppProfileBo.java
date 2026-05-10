package org.dromara.system.domain.bo.cooking;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.constant.RegexConstants;
import org.dromara.common.core.xss.Xss;

@Data
public class DcCookAppProfileBo {

    @Xss(message = "nickName cannot contain script characters")
    @Size(max = 30, message = "nickName length cannot exceed {max}")
    private String nickName;

    @Pattern(regexp = "^$|" + RegexConstants.MOBILE, message = "手机号格式不正确")
    private String phonenumber;

    private Long avatar;
}
