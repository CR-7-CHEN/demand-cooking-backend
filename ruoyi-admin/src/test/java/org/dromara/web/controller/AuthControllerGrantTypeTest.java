package org.dromara.web.controller;

import org.dromara.system.domain.vo.SysClientVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("登录客户端授权类型校验")
@Tag("dev")
public class AuthControllerGrantTypeTest {

    @Test
    @DisplayName("账号密码和微信快捷登录授权类型互不影响")
    public void grantTypeMustMatchExactly() {
        SysClientVo appClient = new SysClientVo();
        appClient.setGrantType("password,sms,social");

        SysClientVo xcxClient = new SysClientVo();
        xcxClient.setGrantType("xcx");

        assertTrue(AuthController.supportsGrantType(appClient, "password"));
        assertFalse(AuthController.supportsGrantType(appClient, "xcx"));
        assertFalse(AuthController.supportsGrantType(appClient, "pass"));

        assertTrue(AuthController.supportsGrantType(xcxClient, "xcx"));
        assertFalse(AuthController.supportsGrantType(xcxClient, "password"));
    }
}
