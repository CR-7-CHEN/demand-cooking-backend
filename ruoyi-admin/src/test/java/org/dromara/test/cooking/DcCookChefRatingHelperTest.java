package org.dromara.test.cooking;

import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.service.impl.cooking.DcCookChefRatingHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cooking chef rating helper")
@Tag("dev")
public class DcCookChefRatingHelperTest {

    @Test
    @DisplayName("recalculates chef rating as the average of all review ratings")
    void recalculatesChefRatingAsAverageOfAllReviewRatings() {
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookReviewMapper reviewMapper = mock(DcCookReviewMapper.class);
        DcCookChefRatingHelper helper = new DcCookChefRatingHelper(chefMapper, reviewMapper);

        DcCookChef chef = new DcCookChef();
        chef.setChefId(10L);
        chef.setRating(new BigDecimal("9.99"));
        when(chefMapper.selectById(10L)).thenReturn(chef);
        when(reviewMapper.selectList(any())).thenReturn(List.of(review("5.00"), review("0.00")));
        when(chefMapper.updateById(any(DcCookChef.class))).thenReturn(1);

        boolean updated = helper.refreshRating(10L);

        ArgumentCaptor<DcCookChef> captor = ArgumentCaptor.forClass(DcCookChef.class);
        verify(chefMapper).updateById(captor.capture());
        assertTrue(updated);
        assertEquals(0, captor.getValue().getRating().compareTo(new BigDecimal("2.50")));
    }

    private DcCookReview review(String rating) {
        DcCookReview review = new DcCookReview();
        review.setRating(new BigDecimal(rating));
        return review;
    }
}
