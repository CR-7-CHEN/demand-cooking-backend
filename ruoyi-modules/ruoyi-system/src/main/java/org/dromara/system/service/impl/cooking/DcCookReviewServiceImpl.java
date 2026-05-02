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
import org.dromara.system.domain.bo.cooking.DcCookReviewBo;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.domain.vo.cooking.DcCookReviewVo;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.service.cooking.IDcCookReviewService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookReviewServiceImpl implements IDcCookReviewService {

    private final DcCookReviewMapper baseMapper;
    private final DcCookOrderMapper orderMapper;

    @Override
    public DcCookReviewVo queryById(Long reviewId) {
        return baseMapper.selectVoById(reviewId);
    }

    @Override
    public TableDataInfo<DcCookReviewVo> queryPageList(DcCookReviewBo bo, PageQuery pageQuery) {
        Page<DcCookReviewVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public Boolean submit(DcCookReviewBo bo) {
        DcCookOrder order = orderMapper.selectById(bo.getOrderId());
        if (order == null || !DcCookOrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new ServiceException("only completed order can be reviewed");
        }
        boolean exists = baseMapper.exists(Wrappers.lambdaQuery(DcCookReview.class)
            .eq(DcCookReview::getOrderId, bo.getOrderId()));
        if (exists) {
            throw new ServiceException("order already reviewed");
        }
        DcCookReview add = MapstructUtils.convert(bo, DcCookReview.class);
        add.setOrderNo(order.getOrderNo());
        add.setUserId(order.getUserId());
        add.setChefId(order.getChefId());
        if (add.getRating() == null) {
            add.setRating(BigDecimal.ZERO);
        }
        if (StringUtils.isBlank(add.getDisplayStatus())) {
            add.setDisplayStatus("SHOW");
        }
        add.setComplaintAdjusted("N");
        add.setReviewTime(new Date());
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean hide(Long reviewId) {
        DcCookReview update = new DcCookReview();
        update.setReviewId(reviewId);
        update.setDisplayStatus("HIDE");
        return baseMapper.updateById(update) > 0;
    }

    private LambdaQueryWrapper<DcCookReview> buildQueryWrapper(DcCookReviewBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookReview> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReviewId() != null, DcCookReview::getReviewId, bo.getReviewId());
        lqw.eq(bo.getOrderId() != null, DcCookReview::getOrderId, bo.getOrderId());
        lqw.eq(bo.getUserId() != null, DcCookReview::getUserId, bo.getUserId());
        lqw.eq(bo.getChefId() != null, DcCookReview::getChefId, bo.getChefId());
        lqw.eq(StringUtils.isNotBlank(bo.getDisplayStatus()), DcCookReview::getDisplayStatus, bo.getDisplayStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookReview::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookReview::getCreateTime);
        return lqw;
    }
}
