package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
@Component
public class DcCookChefRatingHelper {

    private final DcCookChefMapper chefMapper;
    private final DcCookReviewMapper reviewMapper;

    public boolean refreshRating(Long chefId) {
        if (chefId == null) {
            return false;
        }
        DcCookChef chef = chefMapper.selectById(chefId);
        if (chef == null) {
            return false;
        }
        List<DcCookReview> reviews = reviewMapper.selectList(Wrappers.lambdaQuery(DcCookReview.class)
            .eq(DcCookReview::getChefId, chefId));
        if (reviews == null) {
            reviews = List.of();
        }
        BigDecimal totalRating = reviews.stream()
            .map(DcCookReview::getRating)
            .map(this::defaultAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageRating = reviews.isEmpty()
            ? BigDecimal.ZERO
            : totalRating.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);
        chef.setRating(averageRating);
        return chefMapper.updateById(chef) > 0;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
