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
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookChefTime;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;
import org.dromara.system.domain.vo.cooking.DcCookChefTimeVo;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DcCookChefServiceImpl implements IDcCookChefService {

    private static final String AUDIT_APPROVED = "1";
    private static final String AUDIT_PENDING = "0";
    private static final String AUDIT_REJECTED = "2";
    private static final String STATUS_NORMAL = "0";
    private static final String STATUS_PAUSED = "1";
    private static final String STATUS_DISABLED = "2";
    private static final String STATUS_RESIGNED = "3";

    private final DcCookChefMapper baseMapper;
    private final DcCookChefTimeMapper chefTimeMapper;
    private final DcCookOrderMapper orderMapper;

    @Override
    public DcCookChefVo queryById(Long chefId) {
        DcCookChefVo vo = baseMapper.selectVoById(chefId);
        if (vo != null) {
            hydrateAvailableTimes(List.of(vo));
        }
        return vo;
    }

    @Override
    public DcCookChefVo queryDisplayById(Long chefId) {
        DcCookChefVo vo = baseMapper.selectVoOne(buildAppWrapper(new DcCookChefBo())
            .eq(DcCookChef::getChefId, chefId), false);
        if (vo != null) {
            hydrateAvailableTimes(List.of(vo));
        }
        return vo;
    }

    @Override
    public DcCookChefVo queryByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return baseMapper.selectVoOne(Wrappers.lambdaQuery(DcCookChef.class)
            .eq(DcCookChef::getUserId, userId)
            .orderByDesc(DcCookChef::getCreateTime)
            .last("limit 1"), false);
    }

    @Override
    public TableDataInfo<DcCookChefVo> queryPageList(DcCookChefBo bo, PageQuery pageQuery) {
        Page<DcCookChefVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<DcCookChefVo> queryAppPageList(DcCookChefBo bo, PageQuery pageQuery) {
        Page<DcCookChefVo> page = baseMapper.selectVoPage(pageQuery.build(), buildAppWrapper(bo));
        hydrateAvailableTimes(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public List<DcCookChefVo> queryList(DcCookChefBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<DcCookChef> buildQueryWrapper(DcCookChefBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookChef> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getChefId() != null, DcCookChef::getChefId, bo.getChefId());
        lqw.eq(bo.getUserId() != null, DcCookChef::getUserId, bo.getUserId());
        lqw.eq(bo.getAreaId() != null, DcCookChef::getAreaId, bo.getAreaId());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), DcCookChef::getAreaName, bo.getAreaName());
        lqw.like(StringUtils.isNotBlank(bo.getChefName()), DcCookChef::getChefName, bo.getChefName());
        lqw.eq(StringUtils.isNotBlank(bo.getMobile()), DcCookChef::getMobile, bo.getMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), DcCookChef::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getChefStatus()), DcCookChef::getChefStatus, bo.getChefStatus());
        lqw.like(StringUtils.isNotBlank(bo.getSkillTags()), DcCookChef::getSkillTags, bo.getSkillTags());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookChef::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookChef::getCreateTime);
        return lqw;
    }

    private LambdaQueryWrapper<DcCookChef> buildAppWrapper(DcCookChefBo bo) {
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        LambdaQueryWrapper<DcCookChef> lqw = buildQueryWrapper(bo);
        lqw.eq(DcCookChef::getAuditStatus, AUDIT_APPROVED);
        lqw.eq(DcCookChef::getChefStatus, STATUS_NORMAL);
        lqw.ge(DcCookChef::getHealthCertExpireDate, today);
        lqw.orderByDesc(DcCookChef::getCompletedOrders)
            .orderByDesc(DcCookChef::getRating)
            .orderByDesc(DcCookChef::getRecommendFlag);
        return lqw;
    }

    @Override
    public Boolean insertByBo(DcCookChefBo bo) {
        if (!checkMobileUnique(bo)) {
            throw new ServiceException("Chef mobile already exists");
        }
        DcCookChef add = MapstructUtils.convert(bo, DcCookChef.class);
        if (add.getAuditStatus() == null) {
            add.setAuditStatus(AUDIT_PENDING);
        }
        if (add.getChefStatus() == null) {
            add.setChefStatus(STATUS_NORMAL);
        }
        if (add.getBaseSalary() == null) {
            add.setBaseSalary(BigDecimal.ZERO);
        }
        if (add.getRating() == null) {
            add.setRating(BigDecimal.ZERO);
        }
        if (add.getCompletedOrders() == null) {
            add.setCompletedOrders(0L);
        }
        if (add.getRecommendFlag() == null) {
            add.setRecommendFlag("N");
        }
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(DcCookChefBo bo) {
        if (!checkMobileUnique(bo)) {
            throw new ServiceException("Chef mobile already exists");
        }
        DcCookChef update = MapstructUtils.convert(bo, DcCookChef.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean audit(DcCookChefBo bo) {
        if (bo.getChefId() == null) {
            throw new ServiceException("chefId is required");
        }
        if (!AUDIT_APPROVED.equals(bo.getAuditStatus()) && !AUDIT_REJECTED.equals(bo.getAuditStatus())
            && !AUDIT_PENDING.equals(bo.getAuditStatus())) {
            throw new ServiceException("invalid auditStatus");
        }
        DcCookChef update = new DcCookChef();
        update.setChefId(bo.getChefId());
        update.setAuditStatus(bo.getAuditStatus());
        update.setAuditReason(bo.getAuditReason());
        if (AUDIT_APPROVED.equals(bo.getAuditStatus()) && StringUtils.isBlank(bo.getChefStatus())) {
            update.setChefStatus(STATUS_NORMAL);
        }
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean changeStatus(DcCookChefBo bo) {
        if (bo.getChefId() == null) {
            throw new ServiceException("chefId is required");
        }
        String status = bo.getChefStatus();
        if (!STATUS_NORMAL.equals(status) && !STATUS_PAUSED.equals(status)
            && !STATUS_DISABLED.equals(status) && !STATUS_RESIGNED.equals(status)) {
            throw new ServiceException("invalid chefStatus");
        }
        if ((STATUS_PAUSED.equals(status) || STATUS_RESIGNED.equals(status)) && hasUnfinishedOrder(bo.getChefId())) {
            throw new ServiceException("chef has unfinished orders");
        }
        DcCookChef update = new DcCookChef();
        update.setChefId(bo.getChefId());
        update.setChefStatus(status);
        update.setRemark(bo.getRemark());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean pause(Long userId) {
        DcCookChef chef = requireChefByUserId(userId);
        if (hasUnfinishedOrder(chef.getChefId())) {
            throw new ServiceException("chef has unfinished orders");
        }
        chef.setChefStatus(STATUS_PAUSED);
        return baseMapper.updateById(chef) > 0;
    }

    @Override
    public Boolean resume(Long userId) {
        DcCookChef chef = requireChefByUserId(userId);
        if (!AUDIT_APPROVED.equals(chef.getAuditStatus())) {
            throw new ServiceException("chef audit is not approved");
        }
        chef.setChefStatus(STATUS_NORMAL);
        return baseMapper.updateById(chef) > 0;
    }

    @Override
    public Boolean resign(Long userId) {
        DcCookChef chef = requireChefByUserId(userId);
        if (hasUnfinishedOrder(chef.getChefId())) {
            throw new ServiceException("chef has unfinished orders");
        }
        chef.setChefStatus(STATUS_RESIGNED);
        return baseMapper.updateById(chef) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public boolean checkMobileUnique(DcCookChefBo bo) {
        if (StringUtils.isBlank(bo.getMobile())) {
            return true;
        }
        return !baseMapper.exists(Wrappers.lambdaQuery(DcCookChef.class)
            .eq(DcCookChef::getMobile, bo.getMobile())
            .ne(bo.getChefId() != null, DcCookChef::getChefId, bo.getChefId()));
    }

    private DcCookChef requireChefByUserId(Long userId) {
        DcCookChef chef = baseMapper.selectOne(Wrappers.lambdaQuery(DcCookChef.class)
            .eq(DcCookChef::getUserId, userId)
            .orderByDesc(DcCookChef::getCreateTime)
            .last("limit 1"), false);
        if (chef == null) {
            throw new ServiceException("chef profile not found");
        }
        return chef;
    }

    private boolean hasUnfinishedOrder(Long chefId) {
        return orderMapper.exists(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .notIn(DcCookOrder::getStatus, DcCookOrderStatus.TERMINAL_STATUSES));
    }

    private void hydrateAvailableTimes(List<DcCookChefVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> chefIds = records.stream()
            .filter(Objects::nonNull)
            .map(DcCookChefVo::getChefId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (chefIds.isEmpty()) {
            return;
        }
        List<DcCookChefTimeVo> times = chefTimeMapper.selectVoList(Wrappers.lambdaQuery(DcCookChefTime.class)
            .in(DcCookChefTime::getChefId, chefIds)
            .eq(DcCookChefTime::getStatus, "0")
            .ge(DcCookChefTime::getEndTime, new Date())
            .orderByAsc(DcCookChefTime::getStartTime));
        Map<Long, List<DcCookChefTimeVo>> timeMap = times.stream()
            .collect(Collectors.groupingBy(DcCookChefTimeVo::getChefId));
        records.stream()
            .filter(Objects::nonNull)
            .forEach(record -> {
                List<DcCookChefTimeVo> chefTimes = timeMap.getOrDefault(record.getChefId(), List.of());
                record.setAvailableTimes(chefTimes);
                record.setAvailableTimeText(formatAvailableTimeText(chefTimes));
            });
    }

    private String formatAvailableTimeText(List<DcCookChefTimeVo> times) {
        if (times == null || times.isEmpty()) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return times.stream()
            .limit(3)
            .map(item -> format.format(item.getStartTime()) + " - " + format.format(item.getEndTime()))
            .collect(Collectors.joining("; "));
    }
}
