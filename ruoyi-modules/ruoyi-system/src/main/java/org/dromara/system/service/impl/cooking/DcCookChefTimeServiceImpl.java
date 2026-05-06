package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.domain.bo.cooking.DcCookChefTimeBo;
import org.dromara.system.domain.cooking.DcCookChefTime;
import org.dromara.system.domain.vo.cooking.DcCookChefTimeVo;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.service.cooking.IDcCookChefTimeService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DcCookChefTimeServiceImpl implements IDcCookChefTimeService {

    public static final String STATUS_ENABLED = "0";

    private final DcCookChefTimeMapper baseMapper;

    @Override
    public DcCookChefTimeVo queryById(Long timeId) {
        return baseMapper.selectVoById(timeId);
    }

    @Override
    public List<DcCookChefTimeVo> queryList(DcCookChefTimeBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public Boolean insertByBo(DcCookChefTimeBo bo) {
        validateTimeWindow(bo);
        DcCookChefTime add = MapstructUtils.convert(bo, DcCookChefTime.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(STATUS_ENABLED);
        }
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(DcCookChefTimeBo bo) {
        if (bo.getTimeId() == null) {
            throw new ServiceException("timeId is required");
        }
        validateTimeWindow(bo);
        DcCookChefTime update = MapstructUtils.convert(bo, DcCookChefTime.class);
        return baseMapper.update(update, Wrappers.lambdaUpdate(DcCookChefTime.class)
            .eq(DcCookChefTime::getTimeId, bo.getTimeId())
            .eq(DcCookChefTime::getChefId, bo.getChefId())) > 0;
    }

    @Override
    public Boolean deleteById(Long timeId, Long chefId) {
        if (timeId == null) {
            throw new ServiceException("timeId is required");
        }
        return baseMapper.delete(Wrappers.lambdaQuery(DcCookChefTime.class)
            .eq(DcCookChefTime::getTimeId, timeId)
            .eq(DcCookChefTime::getChefId, chefId)) > 0;
    }

    private LambdaQueryWrapper<DcCookChefTime> buildQueryWrapper(DcCookChefTimeBo bo) {
        LambdaQueryWrapper<DcCookChefTime> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getTimeId() != null, DcCookChefTime::getTimeId, bo.getTimeId());
        lqw.eq(bo.getChefId() != null, DcCookChefTime::getChefId, bo.getChefId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), DcCookChefTime::getStatus, bo.getStatus());
        lqw.ge(bo.getStartTime() != null, DcCookChefTime::getEndTime, bo.getStartTime());
        lqw.le(bo.getEndTime() != null, DcCookChefTime::getStartTime, bo.getEndTime());
        lqw.orderByAsc(DcCookChefTime::getStartTime);
        return lqw;
    }

    private void validateTimeWindow(DcCookChefTimeBo bo) {
        if (bo.getChefId() == null) {
            throw new ServiceException("chefId is required");
        }
        if (bo.getStartTime() == null || bo.getEndTime() == null) {
            throw new ServiceException("startTime and endTime are required");
        }
        if (!bo.getStartTime().before(bo.getEndTime())) {
            throw new ServiceException("startTime must be before endTime");
        }
    }
}
