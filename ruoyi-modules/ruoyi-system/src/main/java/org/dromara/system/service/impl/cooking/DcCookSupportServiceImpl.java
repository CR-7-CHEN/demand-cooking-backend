package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookFaqBo;
import org.dromara.system.domain.bo.cooking.DcCookSupportAskBo;
import org.dromara.system.domain.bo.cooking.DcCookSupportTicketBo;
import org.dromara.system.domain.cooking.DcCookFaq;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookSupportTicket;
import org.dromara.system.domain.cooking.DcCookSupportTicketStatus;
import org.dromara.system.domain.vo.cooking.DcCookFaqVo;
import org.dromara.system.domain.vo.cooking.DcCookSupportAnswerVo;
import org.dromara.system.domain.vo.cooking.DcCookSupportTicketVo;
import org.dromara.system.mapper.cooking.DcCookFaqMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookSupportTicketMapper;
import org.dromara.system.service.cooking.IDcCookSupportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class DcCookSupportServiceImpl implements IDcCookSupportService {

    private static final String ENABLED = "0";
    private static final String TICKET_PENDING = DcCookSupportTicketStatus.PENDING;
    private static final String TICKET_REPLIED = DcCookSupportTicketStatus.REPLIED;
    private static final String TICKET_CLOSED = DcCookSupportTicketStatus.CLOSED;

    private static final List<String> ORDER_WORDS = List.of("订单", "状态", "进度", "支付", "报价", "服务", "取消", "退款");

    private final DcCookFaqMapper faqMapper;
    private final DcCookSupportTicketMapper ticketMapper;
    private final DcCookOrderMapper orderMapper;

    @Override
    public List<String> queryEnabledCategories() {
        return faqMapper.selectList(enabledFaqWrapper(null)).stream()
            .map(DcCookFaq::getCategory)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .toList();
    }

    @Override
    public List<DcCookFaqVo> queryEnabledFaqList(String category) {
        return faqMapper.selectVoList(enabledFaqWrapper(category));
    }

    @Override
    public DcCookSupportAnswerVo ask(Long userId, DcCookSupportAskBo bo) {
        if (userId == null) {
            throw new ServiceException("userId is required");
        }
        String question = StringUtils.trimToEmpty(bo.getQuestion());
        if (StringUtils.isBlank(question)) {
            throw new ServiceException("question is required");
        }
        DcCookFaq faq = matchFaq(question);
        if (faq != null) {
            DcCookSupportAnswerVo answer = baseAnswer("FAQ", faq.getAnswer(), false);
            answer.setFaqId(faq.getFaqId());
            return answer;
        }
        if (bo.getOrderId() != null || isOrderQuestion(question)) {
            DcCookOrder order = resolveUserOrder(userId, bo.getOrderId());
            if (order != null) {
                DcCookSupportAnswerVo answer = baseAnswer("ORDER", buildOrderAnswer(order), false);
                answer.setOrderId(order.getOrderId());
                answer.setOrderNo(order.getOrderNo());
                answer.setOrderStatus(order.getStatus());
                return answer;
            }
        }
        return baseAnswer("TICKET_REQUIRED", "当前问题暂无法自动回答，请提交工单。", true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DcCookSupportTicketVo submitTicket(Long userId, DcCookSupportTicketBo bo) {
        if (userId == null) {
            throw new ServiceException("userId is required");
        }
        if (bo.getOrderId() != null) {
            assertUserOrder(userId, bo.getOrderId());
        }
        DcCookSupportTicket ticket = new DcCookSupportTicket();
        ticket.setUserId(userId);
        ticket.setOrderId(bo.getOrderId());
        ticket.setQuestion(bo.getQuestion());
        ticket.setStatus(TICKET_PENDING);
        ticket.setRemark(bo.getRemark());
        ticketMapper.insert(ticket);
        return normalizeReadStatus(ticketMapper.selectVoById(ticket.getTicketId()));
    }

    @Override
    public TableDataInfo<DcCookSupportTicketVo> queryMyTicketPage(Long userId, DcCookSupportTicketBo bo, PageQuery pageQuery) {
        bo.setUserId(userId);
        Page<DcCookSupportTicketVo> page = ticketMapper.selectVoPage(pageQuery.build(), buildTicketWrapper(bo));
        if (page.getRecords() != null) {
            page.getRecords().forEach(this::normalizeReadStatus);
        }
        return TableDataInfo.build(page);
    }

    @Override
    public DcCookSupportTicketVo queryMyTicketById(Long userId, Long ticketId) {
        DcCookSupportTicket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            return null;
        }
        if (!Objects.equals(userId, ticket.getUserId())) {
            throw new ServiceException("no permission to access this ticket");
        }
        return normalizeReadStatus(ticketMapper.selectVoById(ticketId));
    }

    @Override
    public TableDataInfo<DcCookFaqVo> queryFaqPage(DcCookFaqBo bo, PageQuery pageQuery) {
        Page<DcCookFaqVo> page = faqMapper.selectVoPage(pageQuery.build(), buildFaqWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public DcCookFaqVo queryFaqById(Long faqId) {
        return faqMapper.selectVoById(faqId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean insertFaq(DcCookFaqBo bo) {
        DcCookFaq faq = MapstructUtils.convert(bo, DcCookFaq.class);
        if (StringUtils.isBlank(faq.getStatus())) {
            faq.setStatus(ENABLED);
        }
        if (faq.getSort() == null) {
            faq.setSort(0);
        }
        return faqMapper.insert(faq) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateFaq(DcCookFaqBo bo) {
        DcCookFaq faq = MapstructUtils.convert(bo, DcCookFaq.class);
        return faqMapper.updateById(faq) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateFaqStatus(Long faqId, String status) {
        if (!ENABLED.equals(status) && !"1".equals(status)) {
            throw new ServiceException("invalid FAQ status");
        }
        return faqMapper.update(null, Wrappers.lambdaUpdate(DcCookFaq.class)
            .set(DcCookFaq::getStatus, status)
            .eq(DcCookFaq::getFaqId, faqId)) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteFaqs(List<Long> faqIds) {
        return faqMapper.deleteByIds(faqIds) > 0;
    }

    @Override
    public TableDataInfo<DcCookSupportTicketVo> queryTicketPage(DcCookSupportTicketBo bo, PageQuery pageQuery) {
        Page<DcCookSupportTicketVo> page = ticketMapper.selectVoPage(pageQuery.build(), buildTicketWrapper(bo));
        if (page.getRecords() != null) {
            page.getRecords().forEach(this::normalizeReadStatus);
        }
        return TableDataInfo.build(page);
    }

    @Override
    public DcCookSupportTicketVo queryTicketById(Long ticketId) {
        return normalizeReadStatus(ticketMapper.selectVoById(ticketId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean replyTicket(DcCookSupportTicketBo bo) {
        if (bo.getTicketId() == null) {
            throw new ServiceException("ticketId is required");
        }
        if (StringUtils.isBlank(bo.getReply())) {
            throw new ServiceException("reply is required");
        }
        DcCookSupportTicket update = new DcCookSupportTicket();
        update.setTicketId(bo.getTicketId());
        update.setReply(bo.getReply());
        update.setStatus(TICKET_REPLIED);
        update.setHandlerId(bo.getHandlerId());
        update.setHandleTime(new Date());
        return ticketMapper.updateById(update) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean closeTicket(Long ticketId, Long handlerId) {
        DcCookSupportTicket update = new DcCookSupportTicket();
        update.setTicketId(ticketId);
        update.setStatus(TICKET_CLOSED);
        update.setHandlerId(handlerId);
        update.setCloseTime(new Date());
        return ticketMapper.updateById(update) > 0;
    }

    private LambdaQueryWrapper<DcCookFaq> enabledFaqWrapper(String category) {
        return Wrappers.lambdaQuery(DcCookFaq.class)
            .eq(DcCookFaq::getStatus, ENABLED)
            .eq(StringUtils.isNotBlank(category), DcCookFaq::getCategory, category)
            .orderByAsc(DcCookFaq::getSort)
            .orderByDesc(DcCookFaq::getCreateTime);
    }

    private LambdaQueryWrapper<DcCookFaq> buildFaqWrapper(DcCookFaqBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookFaq> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getFaqId() != null, DcCookFaq::getFaqId, bo.getFaqId());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), DcCookFaq::getCategory, bo.getCategory());
        lqw.like(StringUtils.isNotBlank(bo.getQuestion()), DcCookFaq::getQuestion, bo.getQuestion());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), DcCookFaq::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookFaq::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(DcCookFaq::getSort).orderByDesc(DcCookFaq::getCreateTime);
        return lqw;
    }

    private LambdaQueryWrapper<DcCookSupportTicket> buildTicketWrapper(DcCookSupportTicketBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookSupportTicket> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getTicketId() != null, DcCookSupportTicket::getTicketId, bo.getTicketId());
        lqw.eq(bo.getUserId() != null, DcCookSupportTicket::getUserId, bo.getUserId());
        lqw.eq(bo.getOrderId() != null, DcCookSupportTicket::getOrderId, bo.getOrderId());
        lqw.in(StringUtils.isNotBlank(bo.getStatus()), DcCookSupportTicket::getStatus,
            DcCookSupportTicketStatus.compatibleStatuses(bo.getStatus()));
        lqw.like(StringUtils.isNotBlank(bo.getQuestion()), DcCookSupportTicket::getQuestion, bo.getQuestion());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookSupportTicket::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookSupportTicket::getCreateTime);
        return lqw;
    }

    private DcCookFaq matchFaq(String question) {
        return faqMapper.selectList(enabledFaqWrapper(null)).stream()
            .filter(faq -> question.contains(StringUtils.trimToEmpty(faq.getQuestion()))
                || keywordHit(question, faq.getKeywords()))
            .findFirst()
            .orElse(null);
    }

    private boolean keywordHit(String question, String keywords) {
        if (StringUtils.isBlank(keywords)) {
            return false;
        }
        return Arrays.stream(keywords.split("[,，]"))
            .map(StringUtils::trimToEmpty)
            .filter(StringUtils::isNotBlank)
            .anyMatch(question::contains);
    }

    private boolean isOrderQuestion(String question) {
        return ORDER_WORDS.stream().anyMatch(question::contains);
    }

    private DcCookOrder resolveUserOrder(Long userId, Long orderId) {
        if (orderId != null) {
            return assertUserOrder(userId, orderId);
        }
        return orderMapper.selectOne(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getUserId, userId)
            .orderByDesc(DcCookOrder::getCreateTime)
            .last("limit 1"));
    }

    private DcCookOrder assertUserOrder(Long userId, Long orderId) {
        DcCookOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("order not found");
        }
        if (!Objects.equals(userId, order.getUserId())) {
            throw new ServiceException("no permission to access this order");
        }
        return order;
    }

    private DcCookSupportAnswerVo baseAnswer(String answerType, String answer, boolean ticketRequired) {
        DcCookSupportAnswerVo vo = new DcCookSupportAnswerVo();
        vo.setAnswerType(answerType);
        vo.setAnswer(answer);
        vo.setTicketRequired(ticketRequired);
        return vo;
    }

    private String buildOrderAnswer(DcCookOrder order) {
        return "订单" + StringUtils.blankToDefault(order.getOrderNo(), String.valueOf(order.getOrderId()))
            + "当前状态：" + order.getStatus() + "。";
    }

    private DcCookSupportTicketVo normalizeReadStatus(DcCookSupportTicketVo vo) {
        if (vo != null) {
            vo.setStatus(DcCookSupportTicketStatus.normalize(vo.getStatus()));
        }
        return vo;
    }
}
