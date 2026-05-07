package org.dromara.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWechatMiniProgramRequest;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.domain.model.PasswordLoginBody;
import org.dromara.common.core.enums.LoginType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysSocialBo;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.domain.vo.SysSocialVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysSocialService;
import org.dromara.web.domain.vo.LoginVo;
import org.dromara.web.service.IAuthStrategy;
import org.dromara.web.service.SysLoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Password auth strategy.
 *
 * This project uses password login to opportunistically bind mini-program
 * openid when xcxCode is present, so the next WeChat quick login can locate
 * the account directly.
 */
@Slf4j
@Service("password" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class PasswordAuthStrategy implements IAuthStrategy {

    private static final String MINI_PROGRAM_SOURCE = "wechat_miniprogram";

    private final CaptchaProperties captchaProperties;
    private final SysLoginService loginService;
    private final SysUserMapper userMapper;
    private final ISysSocialService socialService;

    @Value("${wechat.mini-program.appid:}")
    private String wechatAppid;

    @Value("${wechat.mini-program.secret:}")
    private String wechatSecret;

    @Override
    public LoginVo login(String body, SysClientVo client) {
        return login(body, client, true);
    }

    @Override
    public LoginVo login(String body, SysClientVo client, boolean checkCaptcha) {
        PasswordLoginBody loginBody = JsonUtils.parseObject(body, PasswordLoginBody.class);
        ValidatorUtils.validate(loginBody);
        String tenantId = loginBody.getTenantId();
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String code = loginBody.getCode();
        String uuid = loginBody.getUuid();

        boolean captchaEnabled = captchaProperties.getEnable();
        if (checkCaptcha && captchaEnabled) {
            validateCaptcha(tenantId, username, code, uuid);
        }
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            SysUserVo user = loadUserByUsername(username);
            loginService.checkLogin(LoginType.PASSWORD, tenantId, username, () -> !BCrypt.checkpw(password, user.getPassword()));
            return loginService.buildLoginUser(user);
        });
        bindMiniProgramOpenidIfPresent(loginBody, loginUser);
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType(client.getDeviceType());
        model.setTimeout(client.getTimeout());
        model.setActiveTimeout(client.getActiveTimeout());
        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
        LoginHelper.login(loginUser, model);

        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        return loginVo;
    }

    private void bindMiniProgramOpenidIfPresent(PasswordLoginBody loginBody, LoginUser loginUser) {
        if (StringUtils.isBlank(loginBody.getXcxCode())) {
            return;
        }
        MiniProgramIdentity identity = resolveMiniProgramIdentity(loginBody);
        String tenantId = StringUtils.blankToDefault(loginUser.getTenantId(), TenantConstants.DEFAULT_TENANT_ID);
        TenantHelper.dynamic(tenantId, () -> saveMiniProgramBinding(loginBody, loginUser, identity, tenantId));
    }

    private MiniProgramIdentity resolveMiniProgramIdentity(PasswordLoginBody loginBody) {
        String appid = StringUtils.isNotBlank(loginBody.getAppid()) ? loginBody.getAppid() : wechatAppid;
        validateWechatConfig(appid);

        AuthRequest authRequest = new AuthWechatMiniProgramRequest(AuthConfig.builder()
            .clientId(appid).clientSecret(wechatSecret)
            .ignoreCheckRedirectUri(true).ignoreCheckState(true).build());
        AuthCallback authCallback = new AuthCallback();
        authCallback.setCode(loginBody.getXcxCode());
        AuthResponse<AuthUser> resp = authRequest.login(authCallback);
        if (!resp.ok()) {
            throw new ServiceException(resp.getMsg());
        }
        AuthUser authUser = resp.getData();
        AuthToken token = ObjectUtil.isNull(authUser) ? null : authUser.getToken();
        if (ObjectUtil.isNull(token) || StringUtils.isBlank(token.getOpenId())) {
            throw new ServiceException("未获取到微信 openid");
        }
        return new MiniProgramIdentity(token.getOpenId(), token.getUnionId(), token.getAccessToken(), token.getRefreshToken());
    }

    private void saveMiniProgramBinding(PasswordLoginBody loginBody, LoginUser loginUser,
                                        MiniProgramIdentity identity, String tenantId) {
        String authId = buildMiniProgramAuthId(identity.openid);
        SysSocialBo bo = new SysSocialBo();
        bo.setTenantId(tenantId);
        bo.setUserId(loginUser.getUserId());
        bo.setAuthId(authId);
        bo.setSource(MINI_PROGRAM_SOURCE);
        bo.setOpenId(identity.openid);
        bo.setUnionId(identity.unionId);
        bo.setAccessToken(StringUtils.blankToDefault(identity.accessToken, identity.openid));
        bo.setRefreshToken(identity.refreshToken);
        bo.setUserName(StringUtils.blankToDefault(loginUser.getUsername(), ""));
        bo.setNickName(StringUtils.blankToDefault(loginUser.getNickname(), ""));
        bo.setCode(loginBody.getXcxCode());

        List<SysSocialVo> authBindings = socialService.selectByAuthId(authId);
        if (CollUtil.isNotEmpty(authBindings)) {
            SysSocialVo existing = authBindings.get(0);
            if (!loginUser.getUserId().equals(existing.getUserId())) {
                log.info("mini program openid rebind from user {} to user {}", existing.getUserId(), loginUser.getUserId());
            }
            bo.setId(existing.getId());
            socialService.updateByBo(bo);
            return;
        }

        SysSocialBo params = new SysSocialBo();
        params.setUserId(loginUser.getUserId());
        params.setSource(MINI_PROGRAM_SOURCE);
        List<SysSocialVo> userBindings = socialService.queryList(params);
        if (CollUtil.isEmpty(userBindings)) {
            socialService.insertByBo(bo);
        } else {
            bo.setId(userBindings.get(0).getId());
            socialService.updateByBo(bo);
        }
    }

    private void validateWechatConfig(String appid) {
        if (StringUtils.isBlank(appid) || StringUtils.isBlank(wechatSecret)) {
            throw new ServiceException("微信小程序配置不完整");
        }
        if (StringUtils.isNotBlank(wechatAppid) && !StringUtils.equals(wechatAppid, appid)) {
            throw new ServiceException("微信小程序 appid 不匹配");
        }
    }

    private String buildMiniProgramAuthId(String openid) {
        return MINI_PROGRAM_SOURCE + ":" + openid;
    }

    private void validateCaptcha(String tenantId, String username, String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            loginService.recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!StringUtils.equalsIgnoreCase(code, captcha)) {
            loginService.recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    private SysUserVo loadUserByUsername(String username) {
        SysUserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username));
        if (ObjectUtil.isNull(user)) {
            log.info("login user {} does not exist", username);
            throw new UserException("user.not.exists", username);
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("login user {} is disabled", username);
            throw new UserException("user.blocked", username);
        }
        return user;
    }

    private static class MiniProgramIdentity {

        private final String openid;
        private final String unionId;
        private final String accessToken;
        private final String refreshToken;

        private MiniProgramIdentity(String openid, String unionId, String accessToken, String refreshToken) {
            this.openid = openid;
            this.unionId = unionId;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
