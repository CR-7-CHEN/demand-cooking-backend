package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.bo.cooking.DcCookSupportAskBo;
import org.dromara.system.domain.cooking.DcCookFaq;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.vo.cooking.DcCookSupportAnswerVo;
import org.dromara.system.mapper.cooking.DcCookFaqMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookSupportTicketMapper;
import org.dromara.system.service.impl.cooking.DcCookSupportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Cooking support robot")
@Tag("dev")
public class DcCookSupportRobotServiceTest {

    @Test
    @DisplayName("answers enabled FAQ when question hits keywords")
    public void askAnswersFaqByKeyword() {
        DcCookFaqMapper faqMapper = mock(DcCookFaqMapper.class);
        DcCookSupportServiceImpl service = newService(faqMapper, mock(DcCookOrderMapper.class));
        DcCookFaq faq = new DcCookFaq();
        faq.setFaqId(1L);
        faq.setQuestion("如何取消订单");
        faq.setAnswer("未付款订单可在订单详情取消。");
        faq.setKeywords("取消,退单");
        when(faqMapper.selectList(any(Wrapper.class))).thenReturn(List.of(faq));

        DcCookSupportAskBo bo = new DcCookSupportAskBo();
        bo.setQuestion("我想取消订单");
        DcCookSupportAnswerVo answer = service.ask(100L, bo);

        assertEquals("FAQ", answer.getAnswerType());
        assertEquals(1L, answer.getFaqId());
        assertEquals("未付款订单可在订单详情取消。", answer.getAnswer());
    }

    @Test
    @DisplayName("answers order status only when order belongs to current user")
    public void askOrderStatusRequiresCurrentUserOwnership() {
        DcCookFaqMapper faqMapper = mock(DcCookFaqMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookSupportServiceImpl service = newService(faqMapper, orderMapper);
        DcCookOrder order = new DcCookOrder();
        order.setOrderId(2L);
        order.setOrderNo("OD2");
        order.setUserId(200L);
        order.setStatus(DcCookOrderStatus.WAITING_PAY);

        when(faqMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(orderMapper.selectById(2L)).thenReturn(order);

        DcCookSupportAskBo bo = new DcCookSupportAskBo();
        bo.setOrderId(2L);
        bo.setQuestion("订单状态是什么");

        assertThrows(ServiceException.class, () -> service.ask(100L, bo));
    }

    private DcCookSupportServiceImpl newService(DcCookFaqMapper faqMapper, DcCookOrderMapper orderMapper) {
        return new DcCookSupportServiceImpl(faqMapper, mock(DcCookSupportTicketMapper.class), orderMapper);
    }
}
