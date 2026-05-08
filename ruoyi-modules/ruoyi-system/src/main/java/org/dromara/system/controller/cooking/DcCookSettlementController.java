package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookSettlementBo;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;
import org.dromara.system.domain.vo.cooking.DcCookSettlementVo;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.dromara.system.service.cooking.IDcCookSettlementService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cooking/settlement")
public class DcCookSettlementController {

    private final IDcCookSettlementService settlementService;
    private final IDcCookChefService chefService;

    @GetMapping("/list")
    public TableDataInfo<DcCookSettlementVo> list(DcCookSettlementBo bo, PageQuery pageQuery) {
        return settlementService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{settlementId}")
    public R<DcCookSettlementVo> getInfo(@PathVariable Long settlementId) {
        DcCookSettlementVo settlement = settlementService.queryById(settlementId);
        assertAppSettlementReadable(settlement);
        return R.ok(settlement);
    }

    @PostMapping("/generate")
    public R<DcCookSettlementVo> generate(@RequestBody DcCookSettlementBo bo) {
        return R.ok(settlementService.generateMonth(bo));
    }

    @PostMapping({"/review", "/chef/review"})
    public R<Void> applyReview(@RequestBody DcCookSettlementBo bo) {
        if (isAppUser()) {
            assertChefSettlement(bo);
        }
        return settlementService.applyReview(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/review/resolve")
    public R<Void> handleReview(@RequestBody DcCookSettlementBo bo) {
        return settlementService.handleReview(bo) ? R.ok() : R.fail();
    }

    @PostMapping({"/confirm", "/chef/confirm"})
    public R<Void> confirm(@RequestBody DcCookSettlementBo bo) {
        if (isAppUser()) {
            assertChefSettlement(bo);
        }
        return settlementService.confirm(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/pay")
    public R<Void> pay(@RequestBody DcCookSettlementBo bo) {
        return settlementService.pay(bo) ? R.ok() : R.fail();
    }

    @GetMapping("/chef/month")
    public TableDataInfo<DcCookSettlementVo> chefMonth(DcCookSettlementBo bo, PageQuery pageQuery) {
        if (bo.getChefId() == null) {
            DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
            bo.setChefId(chef == null ? -1L : chef.getChefId());
        }
        return settlementService.queryPageList(bo, pageQuery);
    }

    private void assertAppSettlementReadable(DcCookSettlementVo settlement) {
        if (settlement == null || !isAppUser()) {
            return;
        }
        DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
        Long chefId = chef == null ? null : chef.getChefId();
        if (chefId == null || !chefId.equals(settlement.getChefId())) {
            throw new ServiceException("no permission to access this settlement");
        }
    }

    private void assertChefSettlement(DcCookSettlementBo bo) {
        if (bo.getSettlementId() == null) {
            throw new ServiceException("settlementId is required");
        }
        DcCookSettlementVo settlement = settlementService.queryById(bo.getSettlementId());
        DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
        Long chefId = chef == null ? null : chef.getChefId();
        if (settlement == null || chefId == null || !chefId.equals(settlement.getChefId())) {
            throw new ServiceException("no permission to operate this settlement");
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
