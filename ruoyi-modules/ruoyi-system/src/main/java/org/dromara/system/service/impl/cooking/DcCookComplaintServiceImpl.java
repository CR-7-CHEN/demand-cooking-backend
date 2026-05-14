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
import org.dromara.system.domain.bo.cooking.DcCookComplaintBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookComplaint;
import org.dromara.system.domain.cooking.DcCookComplaintStatus;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.domain.vo.cooking.DcCookComplaintVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookComplaintMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.service.cooking.IDcCookComplaintService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DcCookComplaintServiceImpl implements IDcCookComplaintService {

    private final DcCookComplaintMapper baseMapper;
    private final DcCookOrderMapper orderMapper;
    private final DcCookReviewMapper reviewMapper;
    private final DcCookChefRatingHelper chefRatingHelper;
    private final SysUserMapper userMapper;
    private final DcCookChefMapper chefMapper;

    @Override
    public DcCookComplaintVo queryById(Long complaintId) {
        DcCookComplaintVo vo = baseMapper.selectVoById(complaintId);
        if (vo != null) {
            normalizeReadStatus(vo);
            hydrateDisplayNames(List.of(vo));
        }
        return vo;
    }

    @Override
    public TableDataInfo<DcCookComplaintVo> queryPageList(DcCookComplaintBo bo, PageQuery pageQuery) {
        Page<DcCookComplaintVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        if (page.getRecords() != null) {
            page.getRecords().forEach(this::normalizeReadStatus);
        }
        hydrateDisplayNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public Boolean submit(DcCookComplaintBo bo) {
        DcCookOrder order = orderMapper.selectById(bo.getOrderId());
        if (order == null || !DcCookOrderStatus.matches(order.getStatus(), DcCookOrderStatus.COMPLETED)) {
            throw new ServiceException("仅已完成订单可投诉");
        }
        if (!Objects.equals(order.getUserId(), bo.getUserId())) {
            throw new ServiceException("无权投诉该订单");
        }
        DcCookComplaint add = MapstructUtils.convert(bo, DcCookComplaint.class);
        add.setOrderNo(order.getOrderNo());
        add.setUserId(order.getUserId());
        add.setChefId(order.getChefId());
        add.setStatus(DcCookComplaintStatus.PENDING);
        add.setSubmitTime(new Date());
        return baseMapper.insert(add) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean handle(DcCookComplaintBo bo) {
        if (StringUtils.isBlank(bo.getHandleResult())) {
            throw new ServiceException("处理说明不能为空");
        }
        DcCookComplaint complaint = baseMapper.selectById(bo.getComplaintId());
        if (complaint == null) {
            throw new ServiceException("投诉记录不存在");
        }
        complaint.setStatus(Boolean.TRUE.equals(bo.getEstablished()) ? DcCookComplaintStatus.ESTABLISHED : DcCookComplaintStatus.REJECTED);
        complaint.setHandleResult(bo.getHandleResult());
        complaint.setHandlerId(bo.getHandlerId());
        complaint.setHandleTime(new Date());
        boolean ok = baseMapper.updateById(complaint) > 0;
        if (DcCookComplaintStatus.matches(complaint.getStatus(), DcCookComplaintStatus.ESTABLISHED)) {
            DcCookReview review = reviewMapper.selectOne(Wrappers.lambdaQuery(DcCookReview.class)
                .eq(DcCookReview::getOrderId, complaint.getOrderId())
                .last("limit 1"), false);
            if (review != null) {
                review.setRating(BigDecimal.ZERO);
                review.setComplaintAdjusted("Y");
                reviewMapper.updateById(review);
            }
            chefRatingHelper.refreshRating(complaint.getChefId());
        }
        return ok;
    }

    private LambdaQueryWrapper<DcCookComplaint> buildQueryWrapper(DcCookComplaintBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookComplaint> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getComplaintId() != null, DcCookComplaint::getComplaintId, bo.getComplaintId());
        lqw.eq(bo.getOrderId() != null, DcCookComplaint::getOrderId, bo.getOrderId());
        lqw.eq(bo.getUserId() != null, DcCookComplaint::getUserId, bo.getUserId());
        if (StringUtils.isNotBlank(bo.getUserKeyword())) {
            List<Long> userIds = resolveUserIds(bo.getUserKeyword());
            lqw.in(DcCookComplaint::getUserId, userIds.isEmpty() ? List.of(-1L) : userIds);
        }
        lqw.eq(bo.getChefId() != null, DcCookComplaint::getChefId, bo.getChefId());
        if (StringUtils.isNotBlank(bo.getChefName())) {
            List<Long> chefIds = resolveChefIds(bo.getChefName());
            lqw.in(DcCookComplaint::getChefId, chefIds.isEmpty() ? List.of(-1L) : chefIds);
        }
        lqw.in(StringUtils.isNotBlank(bo.getStatus()), DcCookComplaint::getStatus, DcCookComplaintStatus.compatibleStatuses(bo.getStatus()));
        lqw.eq(StringUtils.isNotBlank(bo.getComplaintType()), DcCookComplaint::getComplaintType, bo.getComplaintType());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookComplaint::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookComplaint::getCreateTime);
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

    private void hydrateDisplayNames(List<DcCookComplaintVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> userIds = records.stream()
            .map(DcCookComplaintVo::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() : userMapper.selectList(Wrappers.lambdaQuery(SysUser.class)
                .in(SysUser::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(SysUser::getUserId, user -> user, (left, right) -> left));
        List<Long> chefIds = records.stream()
            .map(DcCookComplaintVo::getChefId)
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

    private DcCookComplaintVo normalizeReadStatus(DcCookComplaintVo vo) {
        if (vo != null) {
            vo.setStatus(DcCookComplaintStatus.normalize(vo.getStatus()));
        }
        return vo;
    }
}
