package org.dromara.system.controller.cooking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookFaqBo;
import org.dromara.system.domain.bo.cooking.DcCookSupportAskBo;
import org.dromara.system.domain.bo.cooking.DcCookSupportTicketBo;
import org.dromara.system.domain.vo.cooking.DcCookFaqVo;
import org.dromara.system.domain.vo.cooking.DcCookSupportAnswerVo;
import org.dromara.system.domain.vo.cooking.DcCookSupportTicketVo;
import org.dromara.system.service.cooking.IDcCookSupportService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class DcCookSupportController {

    private final IDcCookSupportService supportService;

    @GetMapping("/cooking/app/support/faq/categories")
    public R<List<String>> appFaqCategories() {
        return R.ok(supportService.queryEnabledCategories());
    }

    @GetMapping("/cooking/app/support/faq/list")
    public R<List<DcCookFaqVo>> appFaqList(@RequestParam(required = false) String category) {
        return R.ok(supportService.queryEnabledFaqList(category));
    }

    @PostMapping("/cooking/app/support/robot/ask")
    public R<DcCookSupportAnswerVo> ask(@Valid @RequestBody DcCookSupportAskBo bo) {
        return R.ok(supportService.ask(LoginHelper.getUserId(), bo));
    }

    @PostMapping("/cooking/app/support/ticket")
    public R<DcCookSupportTicketVo> submitTicket(@Valid @RequestBody DcCookSupportTicketBo bo) {
        return R.ok(supportService.submitTicket(LoginHelper.getUserId(), bo));
    }

    @GetMapping("/cooking/app/support/ticket/list")
    public TableDataInfo<DcCookSupportTicketVo> myTickets(DcCookSupportTicketBo bo, PageQuery pageQuery) {
        return supportService.queryMyTicketPage(LoginHelper.getUserId(), bo, pageQuery);
    }

    @GetMapping("/cooking/app/support/ticket/{ticketId}")
    public R<DcCookSupportTicketVo> myTicket(@PathVariable Long ticketId) {
        return R.ok(supportService.queryMyTicketById(LoginHelper.getUserId(), ticketId));
    }

    @GetMapping("/cooking/support/faq/list")
    public TableDataInfo<DcCookFaqVo> faqList(DcCookFaqBo bo, PageQuery pageQuery) {
        return supportService.queryFaqPage(bo, pageQuery);
    }

    @GetMapping("/cooking/support/faq/{faqId}")
    public R<DcCookFaqVo> faqInfo(@PathVariable Long faqId) {
        return R.ok(supportService.queryFaqById(faqId));
    }

    @PostMapping("/cooking/support/faq")
    public R<Void> addFaq(@Valid @RequestBody DcCookFaqBo bo) {
        return supportService.insertFaq(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/support/faq")
    public R<Void> editFaq(@Valid @RequestBody DcCookFaqBo bo) {
        return supportService.updateFaq(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/support/faq/status")
    public R<Void> faqStatus(@RequestBody DcCookFaqBo bo) {
        return supportService.updateFaqStatus(bo.getFaqId(), bo.getStatus()) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/support/faq/{faqIds}")
    public R<Void> removeFaq(@PathVariable Long[] faqIds) {
        return supportService.deleteFaqs(Arrays.asList(faqIds)) ? R.ok() : R.fail();
    }

    @GetMapping("/cooking/support/ticket/list")
    public TableDataInfo<DcCookSupportTicketVo> ticketList(DcCookSupportTicketBo bo, PageQuery pageQuery) {
        return supportService.queryTicketPage(bo, pageQuery);
    }

    @GetMapping("/cooking/support/ticket/{ticketId}")
    public R<DcCookSupportTicketVo> ticketInfo(@PathVariable Long ticketId) {
        return R.ok(supportService.queryTicketById(ticketId));
    }

    @PutMapping("/cooking/support/ticket/reply")
    public R<Void> replyTicket(@RequestBody DcCookSupportTicketBo bo) {
        bo.setHandlerId(LoginHelper.getUserId());
        return supportService.replyTicket(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/support/ticket/close/{ticketId}")
    public R<Void> closeTicket(@PathVariable Long ticketId) {
        return supportService.closeTicket(ticketId, LoginHelper.getUserId()) ? R.ok() : R.fail();
    }
}
