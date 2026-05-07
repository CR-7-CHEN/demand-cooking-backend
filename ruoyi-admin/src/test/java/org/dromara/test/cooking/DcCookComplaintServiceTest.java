package org.dromara.test.cooking;

import org.dromara.system.domain.bo.cooking.DcCookComplaintBo;
import org.dromara.system.domain.cooking.DcCookComplaint;
import org.dromara.system.domain.cooking.DcCookComplaintStatus;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.mapper.cooking.DcCookComplaintMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.service.impl.cooking.DcCookChefRatingHelper;
import org.dromara.system.service.impl.cooking.DcCookComplaintServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cooking complaint service")
@Tag("dev")
public class DcCookComplaintServiceTest {

    @Test
    @DisplayName("handle zeros the review and refreshes chef rating when complaint is established")
    void handleZerosReviewAndRefreshesChefRatingWhenComplaintIsEstablished() {
        DcCookComplaintMapper complaintMapper = mock(DcCookComplaintMapper.class);
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookReviewMapper reviewMapper = mock(DcCookReviewMapper.class);
        DcCookChefRatingHelper ratingHelper = mock(DcCookChefRatingHelper.class);
        DcCookComplaintServiceImpl service = new DcCookComplaintServiceImpl(
            complaintMapper,
            orderMapper,
            reviewMapper,
            ratingHelper
        );

        DcCookComplaint complaint = new DcCookComplaint();
        complaint.setComplaintId(200L);
        complaint.setOrderId(100L);
        complaint.setChefId(9L);
        complaint.setStatus(DcCookComplaintStatus.PENDING);
        when(complaintMapper.selectById(200L)).thenReturn(complaint);
        when(complaintMapper.updateById(any(DcCookComplaint.class))).thenReturn(1);
        DcCookReview review = new DcCookReview();
        review.setReviewId(300L);
        review.setOrderId(100L);
        review.setChefId(9L);
        review.setRating(new BigDecimal("4.50"));
        when(reviewMapper.selectOne(any(), eq(false))).thenReturn(review);
        when(reviewMapper.updateById(any(DcCookReview.class))).thenReturn(1);
        when(ratingHelper.refreshRating(9L)).thenReturn(true);

        DcCookComplaintBo bo = new DcCookComplaintBo();
        bo.setComplaintId(200L);
        bo.setEstablished(true);
        bo.setHandlerId(7L);
        bo.setHandleResult("test");

        assertTrue(service.handle(bo));

        ArgumentCaptor<DcCookReview> reviewCaptor = ArgumentCaptor.forClass(DcCookReview.class);
        verify(reviewMapper).updateById(reviewCaptor.capture());
        assertEquals(0, reviewCaptor.getValue().getRating().compareTo(BigDecimal.ZERO));
        assertEquals("Y", reviewCaptor.getValue().getComplaintAdjusted());
        verify(ratingHelper).refreshRating(9L);
    }
}
