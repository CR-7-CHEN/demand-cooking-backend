package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookDishBo;
import org.dromara.system.domain.vo.cooking.DcCookDishVo;
import org.dromara.system.service.cooking.IDcCookDishService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class DcCookDishController {

    private final IDcCookDishService dishService;

    @GetMapping("/cooking/dish/list")
    public TableDataInfo<DcCookDishVo> list(DcCookDishBo bo, PageQuery pageQuery) {
        return dishService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/cooking/app/dish/list")
    public R<List<DcCookDishVo>> appList(DcCookDishBo bo) {
        return R.ok(dishService.queryEnabledList(bo));
    }

    @GetMapping("/cooking/dish/{dishId}")
    public R<DcCookDishVo> getInfo(@PathVariable Long dishId) {
        return R.ok(dishService.queryById(dishId));
    }

    @PostMapping("/cooking/dish")
    public R<Void> add(@RequestBody DcCookDishBo bo) {
        return dishService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/dish")
    public R<Void> edit(@RequestBody DcCookDishBo bo) {
        return dishService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/dish/{dishIds}")
    public R<Void> remove(@PathVariable Long[] dishIds) {
        return dishService.deleteWithValidByIds(Arrays.asList(dishIds), true) ? R.ok() : R.fail();
    }
}
