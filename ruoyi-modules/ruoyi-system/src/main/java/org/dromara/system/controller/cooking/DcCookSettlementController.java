package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
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
        return R.ok(settlementService.queryById(settlementId));
    }

    @PostMapping("/generate")
    public R<DcCookSettlementVo> generate(@RequestBody DcCookSettlementBo bo) {
        return R.ok(settlementService.generateMonth(bo));
    }

    @GetMapping("/chef/month")
    public TableDataInfo<DcCookSettlementVo> chefMonth(DcCookSettlementBo bo, PageQuery pageQuery) {
        if (bo.getChefId() == null) {
            DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
            if (chef != null) {
                bo.setChefId(chef.getChefId());
            }
        }
        return settlementService.queryPageList(bo, pageQuery);
    }
}
