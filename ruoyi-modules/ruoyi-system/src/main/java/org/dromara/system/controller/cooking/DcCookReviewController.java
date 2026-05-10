package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookReviewBo;
import org.dromara.system.domain.vo.cooking.DcCookReviewVo;
import org.dromara.system.service.cooking.IDcCookReviewService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cooking/review")
public class DcCookReviewController {

    private final IDcCookReviewService reviewService;

    @GetMapping("/list")
    public TableDataInfo<DcCookReviewVo> list(DcCookReviewBo bo, PageQuery pageQuery) {
        return reviewService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{reviewId}")
    public R<DcCookReviewVo> getInfo(@PathVariable Long reviewId) {
        return R.ok(reviewService.queryById(reviewId));
    }

    @PostMapping
    public R<Void> submit(@RequestBody DcCookReviewBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        return reviewService.submit(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/hide/{reviewId}")
    public R<Void> hide(@PathVariable Long reviewId) {
        return reviewService.toggleDisplayStatus(reviewId) ? R.ok() : R.fail();
    }
}
