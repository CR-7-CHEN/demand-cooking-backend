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
import org.dromara.system.domain.bo.cooking.DcCookComplaintBo;
import org.dromara.system.domain.cooking.DcCookComplaint;
import org.dromara.system.domain.cooking.DcCookComplaintStatus;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.domain.vo.cooking.DcCookComplaintVo;
import org.dromara.system.mapper.cooking.DcCookComplaintMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.service.cooking.IDcCookComplaintService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookComplaintServiceImpl implements IDcCookComplaintService {

    private final DcCookComplaintMapper baseMapper;
    private final DcCookOrderMapper orderMapper;
    private final DcCookReviewMapper reviewMapper;

    @Override
    public DcCookComplaintVo queryById(Long complaintId) {
        return baseMapper.selectVoById(complaintId);
    }

    @Override
    public TableDataInfo<DcCookComplaintVo> queryPageList(DcCookComplaintBo bo, PageQuery pageQuery) {
        Page<DcCookComplaintVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public Boolean submit(DcCookComplaintBo bo) {
        DcCookOrder order = orderMapper.selectById(bo.getOrderId());
        if (order == null || !DcCookOrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new ServiceException("only completed order can be complained");
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
        DcCookComplaint complaint = baseMapper.selectById(bo.getComplaintId());
        if (complaint == null) {
            throw new ServiceException("complaint not found");
        }
        complaint.setStatus(Boolean.TRUE.equals(bo.getEstablished()) ? DcCookComplaintStatus.ESTABLISHED : DcCookComplaintStatus.REJECTED);
        complaint.setHandleResult(bo.getHandleResult());
        complaint.setHandlerId(bo.getHandlerId());
        complaint.setHandleTime(new Date());
        boolean ok = baseMapper.updateById(complaint) > 0;
        if (DcCookComplaintStatus.ESTABLISHED.equals(complaint.getStatus())) {
            DcCookReview review = reviewMapper.selectOne(Wrappers.lambdaQuery(DcCookReview.class)
                .eq(DcCookReview::getOrderId, complaint.getOrderId())
                .last("limit 1"), false);
            if (review != null) {
                review.setRating(BigDecimal.ZERO);
                review.setComplaintAdjusted("Y");
                reviewMapper.updateById(review);
            }
        }
        return ok;
    }

    private LambdaQueryWrapper<DcCookComplaint> buildQueryWrapper(DcCookComplaintBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookComplaint> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getComplaintId() != null, DcCookComplaint::getComplaintId, bo.getComplaintId());
        lqw.eq(bo.getOrderId() != null, DcCookComplaint::getOrderId, bo.getOrderId());
        lqw.eq(bo.getUserId() != null, DcCookComplaint::getUserId, bo.getUserId());
        lqw.eq(bo.getChefId() != null, DcCookComplaint::getChefId, bo.getChefId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), DcCookComplaint::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getComplaintType()), DcCookComplaint::getComplaintType, bo.getComplaintType());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookComplaint::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookComplaint::getCreateTime);
        return lqw;
    }
}
