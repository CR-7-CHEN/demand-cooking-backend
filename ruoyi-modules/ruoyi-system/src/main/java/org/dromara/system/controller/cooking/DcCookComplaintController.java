package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookComplaintBo;
import org.dromara.system.domain.vo.cooking.DcCookComplaintVo;
import org.dromara.system.service.cooking.IDcCookComplaintService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cooking/complaint")
public class DcCookComplaintController {

    private final IDcCookComplaintService complaintService;

    @GetMapping("/list")
    public TableDataInfo<DcCookComplaintVo> list(DcCookComplaintBo bo, PageQuery pageQuery) {
        return complaintService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{complaintId}")
    public R<DcCookComplaintVo> getInfo(@PathVariable Long complaintId) {
        return R.ok(complaintService.queryById(complaintId));
    }

    @PostMapping
    public R<Void> submit(@RequestBody DcCookComplaintBo bo) {
        if (bo.getUserId() == null) {
            bo.setUserId(LoginHelper.getUserId());
        }
        return complaintService.submit(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/handle")
    public R<Void> handle(@RequestBody DcCookComplaintBo bo) {
        if (bo.getHandlerId() == null) {
            bo.setHandlerId(LoginHelper.getUserId());
        }
        return complaintService.handle(bo) ? R.ok() : R.fail();
    }
}
