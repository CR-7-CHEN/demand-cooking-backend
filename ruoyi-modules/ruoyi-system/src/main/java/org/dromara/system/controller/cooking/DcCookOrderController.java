package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
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
        bo.setUserId(LoginHelper.getUserId());
        return orderService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/chef/list")
    public TableDataInfo<DcCookOrderVo> chefList(DcCookOrderBo bo, PageQuery pageQuery) {
        DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
        bo.setChefId(chef == null ? -1L : chef.getChefId());
        return orderService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{orderId}")
    public R<DcCookOrderVo> getInfo(@PathVariable Long orderId) {
        DcCookOrderVo order = orderService.queryById(orderId);
        assertAppOrderReadable(order);
        return R.ok(order);
    }

    @GetMapping("/detail/{orderId}")
    public R<DcCookOrderVo> detail(@PathVariable Long orderId) {
        DcCookOrderVo order = orderService.queryById(orderId);
        assertAppOrderReadable(order);
        return R.ok(order);
    }

    @PostMapping("/submit")
    public R<DcCookOrderVo> submit(@RequestBody DcCookOrderBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        return R.ok(orderService.submit(bo));
    }

    @PostMapping({"/quote", "/chef/quote"})
    public R<Void> quote(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertChefOrder(bo);
        }
        return orderService.quote(bo) ? R.ok() : R.fail();
    }

    @PostMapping({"/reject", "/chef/reject"})
    public R<Void> reject(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertChefOrder(bo);
        }
        return orderService.reject(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/objection")
    public R<Void> objection(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertUserOrder(bo);
            bo.setUserId(LoginHelper.getUserId());
        }
        return orderService.objection(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/pay/success")
    public R<Void> paySuccess(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertUserOrder(bo);
            bo.setUserId(LoginHelper.getUserId());
        }
        return orderService.paySuccess(bo) ? R.ok() : R.fail();
    }

    @PostMapping({"/serviceStart", "/chef/serviceStart"})
    public R<Void> serviceStart(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertChefOrder(bo);
        }
        return orderService.serviceStart(bo) ? R.ok() : R.fail();
    }

    @PostMapping({"/serviceComplete", "/chef/serviceComplete"})
    public R<Void> serviceComplete(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertChefOrder(bo);
        }
        return orderService.serviceComplete(bo) ? R.ok() : R.fail();
    }

    @PostMapping({"/confirm", "/user/confirm"})
    public R<Void> confirm(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertUserOrder(bo);
            bo.setUserId(LoginHelper.getUserId());
        }
        return orderService.confirm(bo) ? R.ok() : R.fail();
    }

    @GetMapping("/user/cancel/preview/{orderId}")
    public R<DcCookOrderCancelPreviewVo> previewUserCancel(@PathVariable Long orderId) {
        if (isAppUser()) {
            assertUserOrder(orderId);
        }
        return R.ok(orderService.previewUserCancel(orderId));
    }

    @PostMapping("/user/cancel")
    public R<Void> userCancel(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertUserOrder(bo);
            bo.setUserId(LoginHelper.getUserId());
        }
        return orderService.userCancel(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/chef/cancel")
    public R<Void> chefCancel(@RequestBody DcCookOrderActionBo bo) {
        if (isAppUser()) {
            assertChefOrder(bo);
        }
        return orderService.chefCancel(bo) ? R.ok() : R.fail();
    }

    private void assertAppOrderReadable(DcCookOrderVo order) {
        if (order == null || !isAppUser()) {
            return;
        }
        Long loginUserId = LoginHelper.getUserId();
        DcCookChefVo chef = chefService.queryByUserId(loginUserId);
        boolean readable = DcCookPermissionHelper.ownsOrder(loginUserId, order.getUserId())
            || DcCookPermissionHelper.servesOrder(chef == null ? null : chef.getChefId(), order.getChefId());
        if (!readable) {
            throw new ServiceException("无权查看该订单");
        }
    }

    private void assertUserOrder(DcCookOrderActionBo bo) {
        assertUserOrder(bo.getOrderId());
    }

    private void assertUserOrder(Long orderId) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }
        DcCookOrderVo order = orderService.queryById(orderId);
        if (order == null || !DcCookPermissionHelper.ownsOrder(LoginHelper.getUserId(), order.getUserId())) {
            throw new ServiceException("无权操作该订单");
        }
    }

    private void assertChefOrder(DcCookOrderActionBo bo) {
        if (bo.getOrderId() == null) {
            throw new ServiceException("订单ID不能为空");
        }
        DcCookOrderVo order = orderService.queryById(bo.getOrderId());
        DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
        Long chefId = chef == null ? null : chef.getChefId();
        if (order == null || !DcCookPermissionHelper.servesOrder(chefId, order.getChefId())) {
            throw new ServiceException("无权操作该订单");
        }
        bo.setChefId(chefId);
    }

    private boolean isAppUser() {
        try {
            return UserType.APP_USER.equals(LoginHelper.getUserType());
        } catch (Exception ignored) {
            return false;
        }
    }
}
