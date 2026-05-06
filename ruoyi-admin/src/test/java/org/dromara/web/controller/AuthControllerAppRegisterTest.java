package org.dromara.web.controller;

import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.domain.model.RegisterBody;
import org.dromara.common.social.config.properties.SocialProperties;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.service.ISysClientService;
import org.dromara.system.service.ISysConfigService;
import org.dromara.system.service.ISysSocialService;
import org.dromara.system.service.ISysTenantService;
import org.dromara.web.domain.vo.LoginVo;
import org.dromara.web.service.SysLoginService;
import org.dromara.web.service.SysRegisterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("App 注册后自动登录")
@Tag("dev")
class AuthControllerAppRegisterTest {

    @Test
    @DisplayName("注册成功后返回登录凭证")
    void appRegisterReturnsLoginVoAfterRegisteringUser() {
        ISysConfigService configService = mock(ISysConfigService.class);
        ISysClientService clientService = mock(ISysClientService.class);
        SysRegisterService registerService = mock(SysRegisterService.class);

        RegisterBody body = new RegisterBody();
        body.setTenantId("000000");
        body.setUsername("zuofan");
        body.setPassword("123456");
        body.setClientId("client-app");
        body.setGrantType("password");

        SysClientVo client = new SysClientVo();
        client.setClientId("client-app");
        client.setGrantType("password");
        client.setStatus(SystemConstants.NORMAL);

        LoginVo expected = new LoginVo();
        expected.setAccessToken("token-value");

        when(configService.selectRegisterEnabled("000000")).thenReturn(true);
        when(clientService.queryByClientId("client-app")).thenReturn(client);

        AuthController controller = new TestAuthController(configService, clientService, registerService, expected);

        R<LoginVo> response = controller.appRegister(body);

        assertSame(expected, response.getData());
        inOrder(registerService, clientService);
        org.mockito.InOrder order = inOrder(registerService, clientService);
        order.verify(clientService).queryByClientId("client-app");
        order.verify(registerService).register(body, false);
    }

    private static class TestAuthController extends AuthController {

        private final LoginVo loginVo;

        private TestAuthController(ISysConfigService configService, ISysClientService clientService,
                                   SysRegisterService registerService, LoginVo loginVo) {
            super(mock(SocialProperties.class), mock(SysLoginService.class), registerService,
                configService, mock(ISysTenantService.class), mock(ISysSocialService.class),
                clientService, mock(ScheduledExecutorService.class));
            this.loginVo = loginVo;
        }

        @Override
        protected LoginVo loginRegisteredAppUser(RegisterBody user, SysClientVo client) {
            return loginVo;
        }
    }
}
