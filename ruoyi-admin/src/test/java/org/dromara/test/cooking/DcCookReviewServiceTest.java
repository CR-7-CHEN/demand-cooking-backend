package org.dromara.test.cooking;

import io.github.linpeilie.Converter;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.system.domain.bo.cooking.DcCookReviewBo;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.service.impl.cooking.DcCookChefRatingHelper;
import org.dromara.system.service.impl.cooking.DcCookReviewServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cooking review service")
@Tag("dev")
public class DcCookReviewServiceTest {

    @Test
    @DisplayName("submit triggers chef rating refresh after inserting review")
    void submitTriggersChefRatingRefreshAfterInsertingReview() {
        DcCookReviewMapper reviewMapper = mock(DcCookReviewMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefRatingHelper ratingHelper = mock(DcCookChefRatingHelper.class);
        Converter converter = mock(Converter.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(Converter.class)).thenReturn(converter);
        new SpringUtils().setApplicationContext(applicationContext);
        DcCookReviewServiceImpl service = new DcCookReviewServiceImpl(
            reviewMapper,
            orderMapper,
            userMapper,
            chefMapper,
            ratingHelper
        );

        DcCookOrder order = new DcCookOrder();
        order.setOrderId(100L);
        order.setUserId(1L);
        order.setChefId(9L);
        order.setStatus(DcCookOrderStatus.COMPLETED);
        order.setOrderNo("OD100");
        order.setCreateTime(new Date());
        when(converter.convert(any(DcCookReviewBo.class), any(Class.class))).thenReturn(new DcCookReview());
        when(orderMapper.selectById(100L)).thenReturn(order);
        when(reviewMapper.exists(any())).thenReturn(false);
        when(reviewMapper.insert(any(DcCookReview.class))).thenReturn(1);
        when(ratingHelper.refreshRating(9L)).thenReturn(true);

        DcCookReviewBo bo = new DcCookReviewBo();
        bo.setOrderId(100L);
        bo.setUserId(1L);

        assertTrue(service.submit(bo));
        verify(ratingHelper).refreshRating(9L);
    }

    @Test
    @DisplayName("toggle display status restores hidden reviews to shown")
    void toggleDisplayStatusRestoresHiddenReviewsToShown() {
        DcCookReviewMapper reviewMapper = mock(DcCookReviewMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookChefRatingHelper ratingHelper = mock(DcCookChefRatingHelper.class);
        DcCookReviewServiceImpl service = new DcCookReviewServiceImpl(
            reviewMapper,
            orderMapper,
            userMapper,
            chefMapper,
            ratingHelper
        );
        DcCookReview hiddenReview = new DcCookReview();
        hiddenReview.setReviewId(200L);
        hiddenReview.setDisplayStatus("HIDE");
        when(reviewMapper.selectById(200L)).thenReturn(hiddenReview);
        when(reviewMapper.updateById(any(DcCookReview.class))).thenReturn(1);

        assertTrue(service.toggleDisplayStatus(200L));

        ArgumentCaptor<DcCookReview> captor = ArgumentCaptor.forClass(DcCookReview.class);
        verify(reviewMapper).updateById(captor.capture());
        assertEquals(200L, captor.getValue().getReviewId());
        assertEquals("SHOW", captor.getValue().getDisplayStatus());
    }
}
