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
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.cooking.DcCookReviewBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.domain.vo.cooking.DcCookReviewVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.service.cooking.IDcCookReviewService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DcCookReviewServiceImpl implements IDcCookReviewService {

    private final DcCookReviewMapper baseMapper;
    private final DcCookOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final DcCookChefMapper chefMapper;
    private final DcCookChefRatingHelper chefRatingHelper;

    @Override
    public DcCookReviewVo queryById(Long reviewId) {
        DcCookReviewVo vo = baseMapper.selectVoById(reviewId);
        if (vo != null) {
            hydrateDisplayNames(List.of(vo));
        }
        return vo;
    }

    @Override
    public TableDataInfo<DcCookReviewVo> queryPageList(DcCookReviewBo bo, PageQuery pageQuery) {
        Page<DcCookReviewVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        hydrateDisplayNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public Boolean submit(DcCookReviewBo bo) {
        DcCookOrder order = orderMapper.selectById(bo.getOrderId());
        if (order == null || !DcCookOrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new ServiceException("only completed order can be reviewed");
        }
        if (!Objects.equals(order.getUserId(), bo.getUserId())) {
            throw new ServiceException("no permission to review this order");
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
        boolean inserted = baseMapper.insert(add) > 0;
        if (inserted) {
            chefRatingHelper.refreshRating(add.getChefId());
        }
        return inserted;
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
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), DcCookReview::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getUserId() != null, DcCookReview::getUserId, bo.getUserId());
        if (StringUtils.isNotBlank(bo.getUserKeyword())) {
            List<Long> userIds = resolveUserIds(bo.getUserKeyword());
            lqw.in(DcCookReview::getUserId, userIds.isEmpty() ? List.of(-1L) : userIds);
        }
        lqw.eq(bo.getChefId() != null, DcCookReview::getChefId, bo.getChefId());
        if (StringUtils.isNotBlank(bo.getChefName())) {
            List<Long> chefIds = resolveChefIds(bo.getChefName());
            lqw.in(DcCookReview::getChefId, chefIds.isEmpty() ? List.of(-1L) : chefIds);
        }
        lqw.eq(StringUtils.isNotBlank(bo.getDisplayStatus()), DcCookReview::getDisplayStatus, bo.getDisplayStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookReview::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookReview::getCreateTime);
        return lqw;
    }

    private List<Long> resolveUserIds(String keyword) {
        Long userId = parseLong(keyword);
        LambdaQueryWrapper<SysUser> lqw = Wrappers.lambdaQuery(SysUser.class)
            .and(wrapper -> wrapper.like(SysUser::getUserName, keyword).or().like(SysUser::getNickName, keyword));
        if (userId != null) {
            lqw.or().eq(SysUser::getUserId, userId);
        }
        return userMapper.selectList(lqw).stream().map(SysUser::getUserId).toList();
    }

    private List<Long> resolveChefIds(String keyword) {
        Long chefId = parseLong(keyword);
        LambdaQueryWrapper<DcCookChef> lqw = Wrappers.lambdaQuery(DcCookChef.class)
            .like(DcCookChef::getChefName, keyword);
        if (chefId != null) {
            lqw.or().eq(DcCookChef::getChefId, chefId);
        }
        return chefMapper.selectList(lqw).stream().map(DcCookChef::getChefId).toList();
    }

    private void hydrateDisplayNames(List<DcCookReviewVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> userIds = records.stream()
            .map(DcCookReviewVo::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() : userMapper.selectList(Wrappers.lambdaQuery(SysUser.class)
                .in(SysUser::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(SysUser::getUserId, user -> user, (left, right) -> left));
        List<Long> chefIds = records.stream()
            .map(DcCookReviewVo::getChefId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, DcCookChef> chefMap = chefIds.isEmpty() ? Map.of() : chefMapper.selectList(Wrappers.lambdaQuery(DcCookChef.class)
                .in(DcCookChef::getChefId, chefIds))
            .stream()
            .collect(Collectors.toMap(DcCookChef::getChefId, chef -> chef, (left, right) -> left));
        records.forEach(record -> {
            SysUser user = userMap.get(record.getUserId());
            if (user != null) {
                record.setUserName(user.getUserName());
                record.setNickName(user.getNickName());
            }
            DcCookChef chef = chefMap.get(record.getChefId());
            if (chef != null) {
                record.setChefName(chef.getChefName());
            }
        });
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
