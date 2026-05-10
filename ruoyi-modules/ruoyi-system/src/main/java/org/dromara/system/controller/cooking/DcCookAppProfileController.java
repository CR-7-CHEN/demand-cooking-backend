package org.dromara.system.controller.cooking;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.file.MimeTypeUtils;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.bo.cooking.DcCookAppProfileBo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysOssService;
import org.dromara.system.service.ISysUserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cooking/app/profile")
public class DcCookAppProfileController {

    private final SysUserMapper userMapper;
    private final ISysUserService userService;
    private final ISysOssService ossService;

    @GetMapping
    public R<SysUserVo> profile() {
        return R.ok(userService.selectUserById(LoginHelper.getUserId()));
    }

    @PutMapping
    public R<SysUserVo> update(@Valid @RequestBody DcCookAppProfileBo bo) {
        Long userId = LoginHelper.getUserId();
        String phonenumber = StringUtils.trim(bo.getPhonenumber());
        if (StringUtils.isNotEmpty(phonenumber)) {
            SysUserBo userBo = new SysUserBo();
            userBo.setUserId(userId);
            userBo.setPhonenumber(phonenumber);
            if (!userService.checkPhoneUnique(userBo)) {
                return R.fail("手机号码已存在");
            }
        }
        int rows = DataPermissionHelper.ignore(() -> userMapper.update(null,
            Wrappers.lambdaUpdate(SysUser.class)
                .set(bo.getNickName() != null, SysUser::getNickName, bo.getNickName())
                .set(bo.getPhonenumber() != null, SysUser::getPhonenumber, phonenumber)
                .set(bo.getAvatar() != null, SysUser::getAvatar, bo.getAvatar())
                .eq(SysUser::getUserId, userId)));
        if (rows <= 0) {
            return R.fail("profile update failed");
        }
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser != null && bo.getNickName() != null) {
            loginUser.setNickname(bo.getNickName());
        }
        return R.ok(userService.selectUserById(userId));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<AvatarVo> avatar(@RequestPart("avatarfile") MultipartFile avatarfile) {
        if (ObjectUtil.isNotNull(avatarfile) && !avatarfile.isEmpty()) {
            String extension = FileUtil.extName(avatarfile.getOriginalFilename());
            if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
                return R.fail("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
            }
            SysOssVo oss = ossService.upload(avatarfile);
            boolean updateSuccess = DataPermissionHelper.ignore(() -> userService.updateUserAvatar(LoginHelper.getUserId(), oss.getOssId()));
            if (updateSuccess) {
                return R.ok(new AvatarVo(oss.getUrl()));
            }
        }
        return R.fail("上传图片异常，请联系管理员");
    }

    public record AvatarVo(String imgUrl) {
    }
}
