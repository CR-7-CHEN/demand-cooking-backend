package org.dromara.web.controller;

import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.social.config.properties.SocialProperties;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.service.ISysClientService;
import org.dromara.system.service.ISysConfigService;
import org.dromara.system.service.ISysSocialService;
import org.dromara.system.service.ISysTenantService;
import org.dromara.web.domain.vo.LoginVo;
import org.dromara.web.service.IAuthStrategy;
import org.dromara.web.service.SysLoginService;
import org.dromara.web.service.SysRegisterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Tenantless auth")
@Tag("dev")
class AuthControllerTenantlessAuthTest {

    @Test
    @DisplayName("/auth/login should work without tenantId")
    void loginDoesNotRequireTenantId() {
        AuthFixture fixture = new AuthFixture();
        String body = "{\"clientId\":\"client-web\",\"grantType\":\"password\"}";

        try (MockedStatic<IAuthStrategy> authStrategy = org.mockito.Mockito.mockStatic(IAuthStrategy.class);
             MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            authStrategy.when(() -> IAuthStrategy.login(body, fixture.client, "password", true))
                .thenReturn(fixture.loginVo);
            loginHelper.when(LoginHelper::getUserId).thenReturn(1L);

            R<LoginVo> response = fixture.controller.login(body);

            assertSame(fixture.loginVo, response.getData());
            verify(fixture.loginService, never()).checkTenant(null);
        }
    }

    @Test
    @DisplayName("/auth/app/login should work without tenantId")
    void appLoginDoesNotRequireTenantId() {
        AuthFixture fixture = new AuthFixture();
        String body = "{\"clientId\":\"client-web\",\"grantType\":\"password\"}";

        try (MockedStatic<IAuthStrategy> authStrategy = org.mockito.Mockito.mockStatic(IAuthStrategy.class);
             MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            authStrategy.when(() -> IAuthStrategy.login(body, fixture.client, "password", false))
                .thenReturn(fixture.loginVo);
            loginHelper.when(LoginHelper::getUserId).thenReturn(1L);

            R<LoginVo> response = fixture.controller.appLogin(body);

            assertSame(fixture.loginVo, response.getData());
            verify(fixture.loginService, never()).checkTenant(null);
        }
    }

    private static class AuthFixture {

        private final SysLoginService loginService = mock(SysLoginService.class);
        private final SysClientVo client = buildClient();
        private final LoginVo loginVo = buildLoginVo();
        private final AuthController controller;

        private AuthFixture() {
            ISysClientService clientService = mock(ISysClientService.class);
            when(clientService.queryByClientId("client-web")).thenReturn(client);
            controller = new AuthController(
                mock(SocialProperties.class),
                loginService,
                mock(SysRegisterService.class),
                mock(ISysConfigService.class),
                mock(ISysTenantService.class),
                mock(ISysSocialService.class),
                clientService,
                mock(ScheduledExecutorService.class)
            );
        }

        private static SysClientVo buildClient() {
            SysClientVo client = new SysClientVo();
            client.setClientId("client-web");
            client.setGrantType("password");
            client.setStatus(SystemConstants.NORMAL);
            return client;
        }

        private static LoginVo buildLoginVo() {
            LoginVo loginVo = new LoginVo();
            loginVo.setAccessToken("token-value");
            loginVo.setClientId("client-web");
            return loginVo;
        }
    }
}
