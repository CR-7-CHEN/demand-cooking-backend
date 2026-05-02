package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookMessageBo;
import org.dromara.system.domain.vo.cooking.DcCookMessageVo;
import org.dromara.system.service.cooking.IDcCookMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cooking/message")
public class DcCookMessageController {

    private final IDcCookMessageService messageService;

    @GetMapping("/list")
    public TableDataInfo<DcCookMessageVo> list(DcCookMessageBo bo, PageQuery pageQuery) {
        return messageService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{messageId}")
    public R<DcCookMessageVo> getInfo(@PathVariable Long messageId) {
        return R.ok(messageService.queryById(messageId));
    }

    @PostMapping
    public R<Void> add(@RequestBody DcCookMessageBo bo) {
        return messageService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping
    public R<Void> edit(@RequestBody DcCookMessageBo bo) {
        return messageService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @DeleteMapping("/{messageIds}")
    public R<Void> remove(@PathVariable Long[] messageIds) {
        return messageService.deleteWithValidByIds(Arrays.asList(messageIds), true) ? R.ok() : R.fail();
    }
}
