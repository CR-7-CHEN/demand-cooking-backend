package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookConfigBo;
import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.domain.vo.cooking.DcCookConfigVo;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.ISysNoticeService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class DcCookConfigController {

    private final IDcCookConfigService configService;
    private final ISysNoticeService noticeService;

    @GetMapping("/cooking/config/list")
    public TableDataInfo<DcCookConfigVo> list(DcCookConfigBo bo, PageQuery pageQuery) {
        return configService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/cooking/config/{configId}")
    public R<DcCookConfigVo> getInfo(@PathVariable Long configId) {
        return R.ok(configService.queryById(configId));
    }

    @GetMapping("/cooking/config/key/{configKey}")
    public R<String> getByKey(@PathVariable String configKey) {
        return R.ok(configService.selectConfigValueByKey(configKey));
    }

    @PostMapping("/cooking/config")
    public R<Void> add(@RequestBody DcCookConfigBo bo) {
        return configService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/config")
    public R<Void> edit(@RequestBody DcCookConfigBo bo) {
        return configService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/config/publish/{configId}")
    public R<Void> publish(@PathVariable Long configId) {
        return configService.publishCommissionNotice(configId) ? R.ok() : R.fail();
    }

    @GetMapping("/cooking/config/commission/announcement")
    public R<List<SysNoticeVo>> announcement() {
        return R.ok(noticeService.selectAppNoticeList());
    }

    @DeleteMapping("/cooking/config/{configIds}")
    public R<Void> remove(@PathVariable Long[] configIds) {
        return configService.deleteWithValidByIds(Arrays.asList(configIds), true) ? R.ok() : R.fail();
    }
}
