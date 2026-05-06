package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookFaqBo;
import org.dromara.system.domain.bo.cooking.DcCookSupportAskBo;
import org.dromara.system.domain.bo.cooking.DcCookSupportTicketBo;
import org.dromara.system.domain.vo.cooking.DcCookFaqVo;
import org.dromara.system.domain.vo.cooking.DcCookSupportAnswerVo;
import org.dromara.system.domain.vo.cooking.DcCookSupportTicketVo;

import java.util.List;

public interface IDcCookSupportService {

    List<String> queryEnabledCategories();

    List<DcCookFaqVo> queryEnabledFaqList(String category);

    DcCookSupportAnswerVo ask(Long userId, DcCookSupportAskBo bo);

    DcCookSupportTicketVo submitTicket(Long userId, DcCookSupportTicketBo bo);

    TableDataInfo<DcCookSupportTicketVo> queryMyTicketPage(Long userId, DcCookSupportTicketBo bo, PageQuery pageQuery);

    DcCookSupportTicketVo queryMyTicketById(Long userId, Long ticketId);

    TableDataInfo<DcCookFaqVo> queryFaqPage(DcCookFaqBo bo, PageQuery pageQuery);

    DcCookFaqVo queryFaqById(Long faqId);

    Boolean insertFaq(DcCookFaqBo bo);

    Boolean updateFaq(DcCookFaqBo bo);

    Boolean updateFaqStatus(Long faqId, String status);

    Boolean deleteFaqs(List<Long> faqIds);

    TableDataInfo<DcCookSupportTicketVo> queryTicketPage(DcCookSupportTicketBo bo, PageQuery pageQuery);

    DcCookSupportTicketVo queryTicketById(Long ticketId);

    Boolean replyTicket(DcCookSupportTicketBo bo);

    Boolean closeTicket(Long ticketId, Long handlerId);
}
