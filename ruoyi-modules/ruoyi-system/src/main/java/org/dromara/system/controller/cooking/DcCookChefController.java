package org.dromara.system.controller.cooking;

import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.file.MimeTypeUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.domain.bo.cooking.DcCookChefTimeBo;
import org.dromara.system.domain.vo.SysOssUploadVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;
import org.dromara.system.domain.vo.cooking.DcCookChefTimeVo;
import org.dromara.system.service.ISysOssService;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.dromara.system.service.cooking.IDcCookChefTimeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class DcCookChefController {

    private final IDcCookChefService chefService;
    private final IDcCookChefTimeService chefTimeService;
    private final ISysOssService ossService;

    @GetMapping("/cooking/chef/list")
    public TableDataInfo<DcCookChefVo> list(DcCookChefBo bo, PageQuery pageQuery) {
        return chefService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/cooking/chef/{chefId}")
    public R<DcCookChefVo> getInfo(@PathVariable Long chefId) {
        return R.ok(chefService.queryById(chefId));
    }

    @PostMapping("/cooking/chef")
    public R<Void> add(@RequestBody DcCookChefBo bo) {
        return chefService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/chef")
    public R<Void> edit(@RequestBody DcCookChefBo bo) {
        return chefService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/chef/{chefIds}")
    public R<Void> remove(@PathVariable Long[] chefIds) {
        return chefService.deleteWithValidByIds(Arrays.asList(chefIds), true) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/chef/audit")
    public R<Void> audit(@RequestBody DcCookChefBo bo) {
        return chefService.audit(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/chef/status")
    public R<Void> status(@RequestBody DcCookChefBo bo) {
        return chefService.changeStatus(bo) ? R.ok() : R.fail();
    }

    @GetMapping("/cooking/app/chef/list")
    public TableDataInfo<DcCookChefVo> appList(DcCookChefBo bo, PageQuery pageQuery) {
        return chefService.queryAppPageList(bo, pageQuery);
    }

    @GetMapping("/cooking/app/chef/{chefId}")
    public R<DcCookChefVo> appDetail(@PathVariable Long chefId) {
        return R.ok(chefService.queryDisplayById(chefId));
    }

    @GetMapping("/cooking/chef/my")
    public R<DcCookChefVo> my() {
        return R.ok(chefService.queryByUserId(LoginHelper.getUserId()));
    }

    @PutMapping("/cooking/chef/my")
    public R<Void> updateMy(@RequestBody DcCookChefBo bo) {
        Long userId = LoginHelper.getUserId();
        DcCookChefVo mine = chefService.queryByUserId(userId);
        if (mine != null) {
            bo.setChefId(mine.getChefId());
        }
        bo.setUserId(userId);
        return chefService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/cooking/chef/apply")
    public R<Void> apply(@RequestBody DcCookChefBo bo) {
        if (bo.getUserId() == null) {
            bo.setUserId(LoginHelper.getUserId());
        }
        return chefService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PostMapping(value = "/cooking/chef/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadVo> upload(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.fail("上传图片不能为空");
        }
        String extension = FileUtil.extName(file.getOriginalFilename());
        if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
            return R.fail("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
        }
        SysOssVo oss = ossService.upload(file);
        SysOssUploadVo uploadVo = new SysOssUploadVo();
        uploadVo.setUrl(oss.getUrl());
        uploadVo.setFileName(oss.getOriginalName());
        uploadVo.setOssId(oss.getOssId().toString());
        return R.ok(uploadVo);
    }

    @PostMapping("/cooking/chef/pause")
    public R<Void> pause() {
        return chefService.pause(LoginHelper.getUserId()) ? R.ok() : R.fail();
    }

    @PostMapping("/cooking/chef/resume")
    public R<Void> resume() {
        return chefService.resume(LoginHelper.getUserId()) ? R.ok() : R.fail();
    }

    @PostMapping("/cooking/chef/resign")
    public R<Void> resign() {
        return chefService.resign(LoginHelper.getUserId()) ? R.ok() : R.fail();
    }

    @GetMapping("/cooking/chef/time")
    public R<List<DcCookChefTimeVo>> timeList(DcCookChefTimeBo bo) {
        DcCookChefVo chef = requireCurrentChef();
        bo.setChefId(chef.getChefId());
        return R.ok(chefTimeService.queryList(bo));
    }

    @PostMapping("/cooking/chef/time")
    public R<Void> addTime(@RequestBody DcCookChefTimeBo bo) {
        DcCookChefVo chef = requireCurrentChef();
        bo.setChefId(chef.getChefId());
        return chefTimeService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/chef/time")
    public R<Void> updateTime(@RequestBody DcCookChefTimeBo bo) {
        DcCookChefVo chef = requireCurrentChef();
        bo.setChefId(chef.getChefId());
        return chefTimeService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/chef/time")
    public R<Void> deleteTime(@RequestParam("id") Long timeId) {
        DcCookChefVo chef = requireCurrentChef();
        return chefTimeService.deleteById(timeId, chef.getChefId()) ? R.ok() : R.fail();
    }

    private DcCookChefVo requireCurrentChef() {
        DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
        if (chef == null) {
            throw new ServiceException("chef profile not found");
        }
        return chef;
    }
}
