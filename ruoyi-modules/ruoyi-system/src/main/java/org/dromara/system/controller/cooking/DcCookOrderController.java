package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookOrderActionBo;
import org.dromara.system.domain.bo.cooking.DcCookOrderBo;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;
import org.dromara.system.domain.vo.cooking.DcCookOrderCancelPreviewVo;
import org.dromara.system.domain.vo.cooking.DcCookOrderVo;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.dromara.system.service.cooking.IDcCookOrderService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cooking/order")
public class DcCookOrderController {

    private final IDcCookOrderService orderService;
    private final IDcCookChefService chefService;

    @GetMapping("/list")
    public TableDataInfo<DcCookOrderVo> list(DcCookOrderBo bo, PageQuery pageQuery) {
        return orderService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/my/list")
    public TableDataInfo<DcCookOrderVo> myList(DcCookOrderBo bo, PageQuery pageQuery) {
        if (bo.getUserId() == null) {
            bo.setUserId(LoginHelper.getUserId());
        }
        return orderService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/chef/list")
    public TableDataInfo<DcCookOrderVo> chefList(DcCookOrderBo bo, PageQuery pageQuery) {
        if (bo.getChefId() == null) {
            DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
            if (chef != null) {
                bo.setChefId(chef.getChefId());
            }
        }
        return orderService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{orderId}")
    public R<DcCookOrderVo> getInfo(@PathVariable Long orderId) {
        return R.ok(orderService.queryById(orderId));
    }

    @GetMapping("/detail/{orderId}")
    public R<DcCookOrderVo> detail(@PathVariable Long orderId) {
        return R.ok(orderService.queryById(orderId));
    }

    @PostMapping("/submit")
    public R<DcCookOrderVo> submit(@RequestBody DcCookOrderBo bo) {
        if (bo.getUserId() == null) {
            bo.setUserId(LoginHelper.getUserId());
        }
        return R.ok(orderService.submit(bo));
    }

    @PostMapping({"/quote", "/chef/quote"})
    public R<Void> quote(@RequestBody DcCookOrderActionBo bo) {
        return orderService.quote(bo) ? R.ok() : R.fail();
    }

    @PostMapping({"/reject", "/chef/reject"})
    public R<Void> reject(@RequestBody DcCookOrderActionBo bo) {
        return orderService.reject(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/objection")
    public R<Void> objection(@RequestBody DcCookOrderActionBo bo) {
        return orderService.objection(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/pay/success")
    public R<Void> paySuccess(@RequestBody DcCookOrderActionBo bo) {
        return orderService.paySuccess(bo) ? R.ok() : R.fail();
    }

    @PostMapping({"/serviceComplete", "/chef/serviceComplete"})
    public R<Void> serviceComplete(@RequestBody DcCookOrderActionBo bo) {
        return orderService.serviceComplete(bo) ? R.ok() : R.fail();
    }

    @PostMapping({"/confirm", "/user/confirm"})
    public R<Void> confirm(@RequestBody DcCookOrderActionBo bo) {
        return orderService.confirm(bo) ? R.ok() : R.fail();
    }

    @GetMapping("/user/cancel/preview/{orderId}")
    public R<DcCookOrderCancelPreviewVo> previewUserCancel(@PathVariable Long orderId) {
        return R.ok(orderService.previewUserCancel(orderId));
    }

    @PostMapping("/user/cancel")
    public R<Void> userCancel(@RequestBody DcCookOrderActionBo bo) {
        return orderService.userCancel(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/chef/cancel")
    public R<Void> chefCancel(@RequestBody DcCookOrderActionBo bo) {
        return orderService.chefCancel(bo) ? R.ok() : R.fail();
    }
}
