package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookAreaBo;
import org.dromara.system.domain.vo.cooking.DcCookAreaVo;
import org.dromara.system.service.cooking.IDcCookAreaService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cooking/area")
public class DcCookAreaController {

    private final IDcCookAreaService areaService;

    @GetMapping("/list")
    public TableDataInfo<DcCookAreaVo> list(DcCookAreaBo bo, PageQuery pageQuery) {
        return areaService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{areaId}")
    public R<DcCookAreaVo> getInfo(@PathVariable Long areaId) {
        return R.ok(areaService.queryById(areaId));
    }

    @PostMapping
    public R<Void> add(@RequestBody DcCookAreaBo bo) {
        return areaService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DcCookAreaBo bo) {
        return areaService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @DeleteMapping("/{areaIds}")
    public R<Void> remove(@PathVariable Long[] areaIds) {
        return areaService.deleteWithValidByIds(Arrays.asList(areaIds), true) ? R.ok() : R.fail();
    }
}
