package org.dromara.web.service.impl;

import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.domain.model.PasswordLoginBody;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.system.domain.bo.SysSocialBo;
import org.dromara.system.domain.vo.SysSocialVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysSocialService;
import org.dromara.web.service.SysLoginService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("账号密码登录微信 openid 绑定")
@Tag("dev")
class PasswordAuthStrategyMiniProgramBindingTest {

    @Test
    @DisplayName("未绑定时写入微信小程序 openid 与当前用户关系")
    void insertMiniProgramBindingWhenOpenidIsUnbound() throws Throwable {
        ISysSocialService socialService = mock(ISysSocialService.class);
        when(socialService.selectByAuthId("wechat_miniprogram:openid-1")).thenReturn(List.of());
        when(socialService.queryList(any(SysSocialBo.class))).thenReturn(List.of());

        PasswordAuthStrategy strategy = newStrategy(socialService);
        PasswordLoginBody body = new PasswordLoginBody();
        body.setXcxCode("wx-code");
        LoginUser loginUser = newLoginUser(100L);

        invokeSaveMiniProgramBinding(strategy, body, loginUser,
            newIdentity("openid-1", "union-1", "", "refresh-token"), "000000");

        ArgumentCaptor<SysSocialBo> captor = ArgumentCaptor.forClass(SysSocialBo.class);
        verify(socialService).insertByBo(captor.capture());
        SysSocialBo bo = captor.getValue();
        assertEquals("000000", bo.getTenantId());
        assertEquals(100L, bo.getUserId());
        assertEquals("wechat_miniprogram:openid-1", bo.getAuthId());
        assertEquals("wechat_miniprogram", bo.getSource());
        assertEquals("openid-1", bo.getOpenId());
        assertEquals("union-1", bo.getUnionId());
        assertEquals("openid-1", bo.getAccessToken());
        assertEquals("zuofan", bo.getUserName());
        assertEquals("wx-code", bo.getCode());
    }

    @Test
    @DisplayName("openid 已绑定其他账号时拒绝覆盖")
    void rejectBindingWhenOpenidBelongsToAnotherUser() throws Throwable {
        ISysSocialService socialService = mock(ISysSocialService.class);
        SysSocialVo existing = new SysSocialVo();
        existing.setId(1L);
        existing.setUserId(200L);
        when(socialService.selectByAuthId("wechat_miniprogram:openid-1")).thenReturn(List.of(existing));

        PasswordAuthStrategy strategy = newStrategy(socialService);
        PasswordLoginBody body = new PasswordLoginBody();
        body.setXcxCode("wx-code");
        LoginUser loginUser = newLoginUser(100L);

        ServiceException ex = assertThrows(ServiceException.class, () ->
            invokeSaveMiniProgramBinding(strategy, body, loginUser,
                newIdentity("openid-1", "union-1", "token", "refresh-token"), "000000"));
        assertEquals("此微信已绑定其他账号", ex.getMessage());
        verify(socialService, never()).insertByBo(any(SysSocialBo.class));
        verify(socialService, never()).updateByBo(any(SysSocialBo.class));
    }

    private PasswordAuthStrategy newStrategy(ISysSocialService socialService) {
        return new PasswordAuthStrategy(
            mock(CaptchaProperties.class),
            mock(SysLoginService.class),
            mock(SysUserMapper.class),
            socialService
        );
    }

    private LoginUser newLoginUser(Long userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setTenantId("000000");
        loginUser.setUserId(userId);
        loginUser.setUsername("zuofan");
        loginUser.setNickname("做饭用户");
        return loginUser;
    }

    private Object newIdentity(String openid, String unionId, String accessToken, String refreshToken) throws Exception {
        Class<?> identityType = Class.forName(PasswordAuthStrategy.class.getName() + "$MiniProgramIdentity");
        Constructor<?> constructor = identityType.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(openid, unionId, accessToken, refreshToken);
    }

    private void invokeSaveMiniProgramBinding(PasswordAuthStrategy strategy, PasswordLoginBody body,
                                              LoginUser loginUser, Object identity, String tenantId) throws Throwable {
        Method method = PasswordAuthStrategy.class.getDeclaredMethod(
            "saveMiniProgramBinding", PasswordLoginBody.class, LoginUser.class, identity.getClass(), String.class);
        method.setAccessible(true);
        try {
            method.invoke(strategy, body, loginUser, identity, tenantId);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
