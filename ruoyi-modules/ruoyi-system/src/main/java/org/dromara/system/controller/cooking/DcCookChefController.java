package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RequiredArgsConstructor
@RestController
public class DcCookChefController {

    private final IDcCookChefService chefService;

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
}
