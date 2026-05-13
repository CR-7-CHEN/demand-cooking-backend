package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.domain.bo.cooking.DcCookChefTimeBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookChefStatus;
import org.dromara.system.domain.cooking.DcCookChefTime;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.domain.cooking.DcCookSettlement;
import org.dromara.system.domain.vo.cooking.DcCookChefCommissionOrdersVo;
import org.dromara.system.domain.vo.cooking.DcCookChefWorkbenchVo;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;
import org.dromara.system.domain.vo.cooking.DcCookChefTimeVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.mapper.cooking.DcCookSettlementMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DcCookChefServiceImpl implements IDcCookChefService {

    private static final BigDecimal DEFAULT_PLATFORM_RATE = new BigDecimal("0.20");
    private static final String MEAL_PERIOD_BREAKFAST = "breakfast";
    private static final String MEAL_PERIOD_LUNCH = "lunch";
    private static final String MEAL_PERIOD_DINNER = "dinner";
    private static final String AUDIT_APPROVED = DcCookChefStatus.AUDIT_APPROVED;
    private static final String AUDIT_PENDING = DcCookChefStatus.AUDIT_PENDING;
    private static final String AUDIT_REJECTED = DcCookChefStatus.AUDIT_REJECTED;
    private static final String STATUS_NORMAL = DcCookChefStatus.NORMAL;
    private static final String STATUS_PAUSED = DcCookChefStatus.PAUSED;
    private static final String STATUS_DISABLED = DcCookChefStatus.DISABLED;
    private static final String STATUS_RESIGNED = DcCookChefStatus.RESIGNED;

    private final DcCookChefMapper baseMapper;
    private final DcCookChefTimeMapper chefTimeMapper;
    private final DcCookOrderMapper orderMapper;
    private final DcCookReviewMapper reviewMapper;
    private final DcCookSettlementMapper settlementMapper;
    private final IDcCookConfigService configService;
    private final SysUserMapper userMapper;

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
    public DcCookChefWorkbenchVo queryWorkbench(Long userId) {
        DcCookChef chef = requireAvailableWorkbenchChef(userId);

        LocalDate today = LocalDate.now();
        Date todayStart = toDate(today);
        Date tomorrowStart = toDate(today.plusDays(1));
        Date monthStart = toDate(today.withDayOfMonth(1));
        Date nextMonthStart = toDate(today.withDayOfMonth(1).plusMonths(1));

        DcCookChefWorkbenchVo vo = new DcCookChefWorkbenchVo();
        vo.setChefStatus(chef.getChefStatus());
        vo.setAuditStatus(chef.getAuditStatus());
        vo.setTakingOrders(DcCookChefStatus.matchesChefStatus(chef.getChefStatus(), STATUS_NORMAL));
        vo.setRevenueOverview(buildRevenueOverview(chef, todayStart, tomorrowStart, monthStart, nextMonthStart));
        vo.setOrderReminderCount(buildOrderReminderCount(chef.getChefId()));
        vo.setOrderTotalCount(buildOrderTotalCount(chef.getChefId()));
        vo.setAlerts(buildWorkbenchAlerts(chef, todayStart, tomorrowStart));
        vo.setRevenueTrend(buildRevenueTrend(chef.getChefId(), today));
        return vo;
    }

    @Override
    public DcCookChefCommissionOrdersVo queryCommissionOrders(Long userId, String month) {
        DcCookChef chef = requireAvailableWorkbenchChef(userId);
        YearMonth yearMonth = parseMonth(month);
        Date monthStart = toDate(yearMonth.atDay(1));
        Date nextMonthStart = toDate(yearMonth.plusMonths(1).atDay(1));
        BigDecimal chefRate = BigDecimal.ONE.subtract(getDecimalConfig("dc.cooking.platform.rate", DEFAULT_PLATFORM_RATE));

        List<DcCookOrder> orders = selectCompletedOrdersInMonth(chef.getChefId(), monthStart, nextMonthStart);
        List<Long> orderIds = orders.stream().map(DcCookOrder::getOrderId).filter(Objects::nonNull).toList();
        Map<Long, DcCookReview> reviewByOrderId = orderIds.isEmpty() ? Map.of() : reviewMapper.selectList(Wrappers.lambdaQuery(DcCookReview.class)
                .in(DcCookReview::getOrderId, orderIds)
                .orderByDesc(DcCookReview::getReviewTime)
                .orderByDesc(DcCookReview::getReviewId))
            .stream()
            .collect(Collectors.toMap(DcCookReview::getOrderId, item -> item, (left, right) -> left));

        List<DcCookChefCommissionOrdersVo.Row> rows = orders.stream()
            .sorted((left, right) -> Objects.compare(resolveCompleteTime(right), resolveCompleteTime(left),
                Comparator.nullsLast(Date::compareTo)))
            .map(order -> buildCommissionRow(order, reviewByOrderId.get(order.getOrderId()), chefRate))
            .toList();
        BigDecimal commissionTotal = rows.stream()
            .map(DcCookChefCommissionOrdersVo.Row::getCommissionAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        DcCookChefCommissionOrdersVo.Summary summary = new DcCookChefCommissionOrdersVo.Summary();
        summary.setMonth(yearMonth.toString());
        summary.setOrderCount(rows.size());
        summary.setCommissionTotal(commissionTotal);

        DcCookChefCommissionOrdersVo vo = new DcCookChefCommissionOrdersVo();
        vo.setSummary(summary);
        vo.setRows(rows);
        return vo;
    }

    @Override
    public TableDataInfo<DcCookChefVo> queryPageList(DcCookChefBo bo, PageQuery pageQuery) {
        Page<DcCookChefVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        hydrateAvailableTimes(page.getRecords());
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
        List<DcCookChefVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        hydrateAvailableTimes(list);
        return list;
    }

    private LambdaQueryWrapper<DcCookChef> buildQueryWrapper(DcCookChefBo bo) {
        Map<String, Object> params = bo.getParams();
        String keyword = StringUtils.trim(bo.getKeyword());
        LambdaQueryWrapper<DcCookChef> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getChefId() != null, DcCookChef::getChefId, bo.getChefId());
        lqw.eq(bo.getUserId() != null, DcCookChef::getUserId, bo.getUserId());
        lqw.eq(bo.getAreaId() != null, DcCookChef::getAreaId, bo.getAreaId());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), DcCookChef::getAreaName, bo.getAreaName());
        lqw.like(StringUtils.isNotBlank(bo.getChefName()), DcCookChef::getChefName, bo.getChefName());
        lqw.and(StringUtils.isNotBlank(keyword),
            wrapper -> wrapper.like(DcCookChef::getChefName, keyword).or().like(DcCookChef::getSkillTags, keyword));
        lqw.eq(StringUtils.isNotBlank(bo.getMobile()), DcCookChef::getMobile, bo.getMobile());
        lqw.in(StringUtils.isNotBlank(bo.getAuditStatus()), DcCookChef::getAuditStatus,
            DcCookChefStatus.compatibleAuditStatuses(bo.getAuditStatus()));
        lqw.in(StringUtils.isNotBlank(bo.getChefStatus()), DcCookChef::getChefStatus,
            DcCookChefStatus.compatibleChefStatuses(bo.getChefStatus()));
        lqw.like(StringUtils.isNotBlank(bo.getSkillTags()), DcCookChef::getSkillTags, bo.getSkillTags());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookChef::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookChef::getCreateTime);
        return lqw;
    }

    private LambdaQueryWrapper<DcCookChef> buildAppWrapper(DcCookChefBo bo) {
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        LambdaQueryWrapper<DcCookChef> lqw = buildQueryWrapper(bo);
        lqw.in(DcCookChef::getAuditStatus, DcCookChefStatus.compatibleAuditStatuses(AUDIT_APPROVED));
        lqw.in(DcCookChef::getChefStatus, DcCookChefStatus.compatibleChefStatuses(STATUS_NORMAL));
        lqw.ge(DcCookChef::getHealthCertExpireDate, today);
        applyMealPeriodFilter(lqw, bo.getMealPeriod());
        lqw.orderByDesc(DcCookChef::getCompletedOrders)
            .orderByDesc(DcCookChef::getRating)
            .orderByDesc(DcCookChef::getRecommendFlag);
        return lqw;
    }

    private void applyMealPeriodFilter(LambdaQueryWrapper<DcCookChef> lqw, String mealPeriod) {
        List<String> mealRemarks = resolveMealRemarks(mealPeriod);
        if (mealRemarks.isEmpty()) {
            return;
        }
        lqw.apply("chef_id in ("
                + "select chef_id "
                + "from dc_cook_chef_time "
                + "where del_flag = '0' "
                + "and status = '0' "
                + "and end_time >= now() "
                + "and remark in ({0}, {1})"
                + ")",
            mealRemarks.get(0), mealRemarks.get(1));
    }

    private List<String> resolveMealRemarks(String mealPeriod) {
        if (StringUtils.isBlank(mealPeriod)) {
            return List.of();
        }
        switch (mealPeriod.trim().toLowerCase()) {
            case MEAL_PERIOD_BREAKFAST:
                return List.of("早餐", "早饭");
            case MEAL_PERIOD_LUNCH:
                return List.of("午餐", "午饭");
            case MEAL_PERIOD_DINNER:
                return List.of("晚餐", "晚饭");
            default:
                return List.of();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(DcCookChefBo bo) {
        assertNoPendingApply(bo.getUserId());
        if (!checkMobileUnique(bo)) {
            throw new ServiceException("Chef mobile already exists");
        }
        validateUserPhoneAvailable(bo);
        DcCookChef add = MapstructUtils.convert(bo, DcCookChef.class);
        if (StringUtils.isNotBlank(add.getAuditStatus())) {
            add.setAuditStatus(DcCookChefStatus.normalizeAuditStatus(add.getAuditStatus()));
        }
        if (StringUtils.isNotBlank(add.getChefStatus())) {
            add.setChefStatus(DcCookChefStatus.normalizeChefStatus(add.getChefStatus()));
        }
        if (StringUtils.isBlank(add.getAuditStatus())) {
            add.setAuditStatus(AUDIT_PENDING);
        }
        if (StringUtils.isBlank(add.getChefStatus())) {
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
        boolean inserted = baseMapper.insert(add) > 0;
        if (inserted) {
            saveAvailableTimes(add.getChefId(), bo.getAvailableTimes());
            syncUserPhone(bo);
        }
        return inserted;
    }

    private void assertNoPendingApply(Long userId) {
        if (userId == null) {
            return;
        }
        DcCookChef existing = baseMapper.selectOne(Wrappers.lambdaQuery(DcCookChef.class)
            .eq(DcCookChef::getUserId, userId)
            .orderByDesc(DcCookChef::getCreateTime)
            .last("limit 1"), false);
        if (existing != null && DcCookChefStatus.matchesAuditStatus(existing.getAuditStatus(), AUDIT_PENDING)) {
            throw new ServiceException("当前入驻申请正在审核中，请勿重复提交");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(DcCookChefBo bo) {
        if (!checkMobileUnique(bo)) {
            throw new ServiceException("Chef mobile already exists");
        }
        validateUserPhoneAvailable(bo);
        String resignReason = normalizeResignReason(bo.getChefStatus(), bo.getResignReason());
        DcCookChef update = MapstructUtils.convert(bo, DcCookChef.class);
        if (StringUtils.isNotBlank(update.getAuditStatus())) {
            update.setAuditStatus(DcCookChefStatus.normalizeAuditStatus(update.getAuditStatus()));
        }
        if (StringUtils.isNotBlank(update.getChefStatus())) {
            update.setChefStatus(DcCookChefStatus.normalizeChefStatus(update.getChefStatus()));
        }
        if (DcCookChefStatus.matchesChefStatus(bo.getChefStatus(), STATUS_RESIGNED)) {
            update.setResignReason(resignReason);
        }
        boolean updated = baseMapper.updateById(update) > 0;
        if (updated) {
            saveAvailableTimes(bo.getChefId(), bo.getAvailableTimes());
            syncUserPhone(bo);
        }
        return updated;
    }

    private void saveAvailableTimes(Long chefId, List<DcCookChefTimeBo> availableTimes) {
        if (availableTimes == null) {
            return;
        }
        if (chefId == null) {
            throw new ServiceException("chefId is required");
        }
        availableTimes.forEach(time -> validateAvailableTime(chefId, time));
        chefTimeMapper.delete(Wrappers.lambdaQuery(DcCookChefTime.class)
            .eq(DcCookChefTime::getChefId, chefId));
        availableTimes.forEach(time -> {
            DcCookChefTime add = MapstructUtils.convert(time, DcCookChefTime.class);
            add.setTimeId(null);
            add.setChefId(chefId);
            if (StringUtils.isBlank(add.getStatus())) {
                add.setStatus("0");
            }
            chefTimeMapper.insert(add);
        });
    }

    private void validateAvailableTime(Long chefId, DcCookChefTimeBo time) {
        if (time == null) {
            throw new ServiceException("availableTime is required");
        }
        time.setChefId(chefId);
        if (time.getStartTime() == null || time.getEndTime() == null) {
            throw new ServiceException("startTime and endTime are required");
        }
        if (!time.getStartTime().before(time.getEndTime())) {
            throw new ServiceException("startTime must be before endTime");
        }
        DcCookChefTimeServiceImpl.validateHalfHourBoundary(time.getStartTime(), time.getEndTime());
        DcCookChefTimeServiceImpl.validateMinimumDuration(time.getStartTime(), time.getEndTime());
        if (!DcCookChefTimeServiceImpl.isValidMealRemark(time.getRemark())) {
            throw new ServiceException("remark must be one of 早餐/午餐/晚餐");
        }
    }

    @Override
    public Boolean audit(DcCookChefBo bo) {
        if (bo.getChefId() == null) {
            throw new ServiceException("chefId is required");
        }
        String auditStatus = DcCookChefStatus.normalizeAuditStatus(bo.getAuditStatus());
        if (!DcCookChefStatus.isValidAuditStatus(auditStatus)) {
            throw new ServiceException("invalid auditStatus");
        }
        if (AUDIT_REJECTED.equals(auditStatus) && StringUtils.isBlank(bo.getAuditReason())) {
            throw new ServiceException("auditReason is required");
        }
        Long auditBy = resolveAuditBy(bo);
        Date auditTime = new Date();
        var update = Wrappers.lambdaUpdate(DcCookChef.class)
            .eq(DcCookChef::getChefId, bo.getChefId())
            .set(DcCookChef::getAuditStatus, auditStatus)
            .set(DcCookChef::getAuditBy, auditBy)
            .set(DcCookChef::getAuditTime, auditTime);
        if (AUDIT_REJECTED.equals(auditStatus)) {
            update.set(DcCookChef::getAuditReason, bo.getAuditReason().trim());
        } else {
            update.set(DcCookChef::getAuditReason, null);
        }
        if (AUDIT_APPROVED.equals(auditStatus) && StringUtils.isBlank(bo.getChefStatus())) {
            update.set(DcCookChef::getChefStatus, STATUS_NORMAL);
        }
        return baseMapper.update(null, update) > 0;
    }

    private Long resolveAuditBy(DcCookChefBo bo) {
        Long userId = LoginHelper.getUserId();
        if (userId != null) {
            return userId;
        }
        return bo.getUpdateBy();
    }

    @Override
    public Boolean changeStatus(DcCookChefBo bo) {
        if (bo.getChefId() == null) {
            throw new ServiceException("chefId is required");
        }
        String status = DcCookChefStatus.normalizeChefStatus(bo.getChefStatus());
        if (!DcCookChefStatus.isValidChefStatus(status)) {
            throw new ServiceException("invalid chefStatus");
        }
        if ((STATUS_PAUSED.equals(status) || STATUS_RESIGNED.equals(status)) && hasUnfinishedOrder(bo.getChefId())) {
            throw new ServiceException("chef has unfinished orders");
        }
        String resignReason = normalizeResignReason(status, bo.getResignReason());
        DcCookChef update = new DcCookChef();
        update.setChefId(bo.getChefId());
        update.setChefStatus(status);
        if (STATUS_RESIGNED.equals(status)) {
            update.setResignReason(resignReason);
        }
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
        if (!DcCookChefStatus.matchesAuditStatus(chef.getAuditStatus(), AUDIT_APPROVED)) {
            throw new ServiceException("chef audit is not approved");
        }
        chef.setChefStatus(STATUS_NORMAL);
        return baseMapper.updateById(chef) > 0;
    }

    @Override
    public Boolean resign(Long userId, String resignReason) {
        String reason = normalizeResignReason(STATUS_RESIGNED, resignReason);
        DcCookChef chef = requireChefByUserId(userId);
        if (hasUnfinishedOrder(chef.getChefId())) {
            throw new ServiceException("chef has unfinished orders");
        }
        chef.setChefStatus(STATUS_RESIGNED);
        chef.setResignReason(reason);
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

    private void validateUserPhoneAvailable(DcCookChefBo bo) {
        if (bo == null || bo.getUserId() == null || StringUtils.isBlank(bo.getMobile())) {
            return;
        }
        boolean exists = userMapper.exists(Wrappers.lambdaQuery(SysUser.class)
            .eq(SysUser::getPhonenumber, bo.getMobile())
            .ne(SysUser::getUserId, bo.getUserId()));
        if (exists) {
            throw new ServiceException("User phone already exists");
        }
    }

    private void syncUserPhone(DcCookChefBo bo) {
        if (bo == null || bo.getUserId() == null || StringUtils.isBlank(bo.getMobile())) {
            return;
        }
        userMapper.update(null, Wrappers.lambdaUpdate(SysUser.class)
            .set(SysUser::getPhonenumber, bo.getMobile())
            .eq(SysUser::getUserId, bo.getUserId()));
    }

    private String normalizeResignReason(String status, String resignReason) {
        if (!DcCookChefStatus.matchesChefStatus(status, STATUS_RESIGNED)) {
            return resignReason;
        }
        String reason = resignReason == null ? null : resignReason.trim();
        if (StringUtils.isBlank(reason)) {
            throw new ServiceException("resignReason is required");
        }
        if (reason.length() > 500) {
            throw new ServiceException("resignReason max length is 500");
        }
        return reason;
    }

    private DcCookChefWorkbenchVo.RevenueOverview buildRevenueOverview(DcCookChef chef, Date todayStart, Date tomorrowStart,
                                                                       Date monthStart, Date nextMonthStart) {
        DcCookChefWorkbenchVo.RevenueOverview overview = new DcCookChefWorkbenchVo.RevenueOverview();
        Long chefId = chef.getChefId();
        BigDecimal todayCommission = sumCommissionAmount(chefId, todayStart, tomorrowStart);
        BigDecimal monthCommission = sumCommissionAmount(chefId, monthStart, nextMonthStart);
        overview.setTodayIncome(todayCommission);
        overview.setMonthIncome(monthCommission);
        overview.setMonthCompletedOrders(countMonthCompletedOrders(chefId, monthStart, nextMonthStart));
        overview.setMonthCommissionAmount(monthCommission);

        DcCookSettlement settlement = settlementMapper.selectOne(Wrappers.lambdaQuery(DcCookSettlement.class)
            .eq(DcCookSettlement::getChefId, chefId)
            .eq(DcCookSettlement::getSettlementMonth, YearMonth.now().toString())
            .orderByDesc(DcCookSettlement::getGeneratedTime)
            .orderByDesc(DcCookSettlement::getSettlementId)
            .last("limit 1"), false);
        BigDecimal monthBaseSalary = defaultAmount(settlement == null ? chef.getBaseSalary() : settlement.getBaseSalary());
        overview.setMonthBaseSalary(monthBaseSalary);
        overview.setMonthDeduction(defaultAmount(settlement == null ? null : settlement.getViolationDeduction()));
        overview.setMonthPayableAmount(monthBaseSalary.add(monthCommission));
        return overview;
    }

    private List<DcCookChefWorkbenchVo.AlertItem> buildWorkbenchAlerts(DcCookChef chef, Date todayStart, Date tomorrowStart) {
        List<DcCookChefWorkbenchVo.AlertItem> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate healthExpireDate = toLocalDate(chef.getHealthCertExpireDate());
        if (healthExpireDate == null || healthExpireDate.isBefore(today)) {
            alerts.add(alert("health_cert_expired", "健康证已过期",
                "请尽快更新健康证后再接单。", "danger", 1L));
        } else if (!healthExpireDate.isAfter(today.plusDays(30))) {
            alerts.add(alert("health_cert_expiring", "健康证即将到期",
                "健康证将在 30 天内到期，请提前更新。", "warning", 1L));
        }

        Long waitingResponseCount = countOrdersByStatus(chef.getChefId(), DcCookOrderStatus.WAITING_RESPONSE);
        if (waitingResponseCount > 0) {
            alerts.add(alert("waiting_response", "待报价订单",
                "有新的预约订单等待报价。", "warning", waitingResponseCount));
        }

        Long priceObjectionCount = countOrdersByStatus(chef.getChefId(), DcCookOrderStatus.PRICE_OBJECTION);
        if (priceObjectionCount > 0) {
            alerts.add(alert("price_objection", "报价异议待处理",
                "用户对报价有异议，请尽快处理。", "danger", priceObjectionCount));
        }

        Long todayServiceCount = orderMapper.selectCount(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chef.getChefId())
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.WAITING_SERVICE))
            .ge(DcCookOrder::getServiceStartTime, todayStart)
            .lt(DcCookOrder::getServiceStartTime, tomorrowStart));
        if (todayServiceCount > 0) {
            alerts.add(alert("today_service", "今日待服务",
                "今天有待上门服务订单，请留意时间安排。", "info", todayServiceCount));
        }

        return alerts;
    }

    private List<DcCookChefWorkbenchVo.TrendItem> buildRevenueTrend(Long chefId, LocalDate today) {
        LocalDate startDate = today.minusDays(6);
        Date start = toDate(startDate);
        Date end = toDate(today.plusDays(1));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM.dd");
        BigDecimal chefRate = BigDecimal.ONE.subtract(getDecimalConfig("dc.cooking.platform.rate", DEFAULT_PLATFORM_RATE));

        Map<LocalDate, BigDecimal> amountByDate = selectCompletedOrdersInRange(chefId, start, end)
            .stream()
            .map(order -> Map.entry(toLocalDate(resolveCompleteTime(order)), calculateCommission(order.getPayAmount(), chefRate)))
            .filter(entry -> entry.getKey() != null)
            .collect(Collectors.groupingBy(Map.Entry::getKey,
                Collectors.reducing(BigDecimal.ZERO, Map.Entry::getValue, BigDecimal::add)));

        List<DcCookChefWorkbenchVo.TrendItem> trend = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            LocalDate date = startDate.plusDays(index);
            DcCookChefWorkbenchVo.TrendItem item = new DcCookChefWorkbenchVo.TrendItem();
            item.setDate(date.format(dateFormatter));
            item.setLabel(date.format(labelFormatter));
            item.setAmount(defaultAmount(amountByDate.get(date)));
            trend.add(item);
        }
        return trend;
    }

    private Long buildOrderReminderCount(Long chefId) {
        return countOrdersByStatus(chefId, DcCookOrderStatus.WAITING_RESPONSE)
            + countOrdersByStatus(chefId, DcCookOrderStatus.WAITING_SERVICE)
            + countOrdersByStatus(chefId, DcCookOrderStatus.PRICE_OBJECTION);
    }

    private Long buildOrderTotalCount(Long chefId) {
        return orderMapper.selectCount(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId));
    }

    private Long countMonthCompletedOrders(Long chefId, Date monthStart, Date nextMonthStart) {
        return (long) selectCompletedOrdersInMonth(chefId, monthStart, nextMonthStart).size();
    }

    private Long countOrdersByStatus(Long chefId, String status) {
        return orderMapper.selectCount(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(status)));
    }

    private DcCookChefWorkbenchVo.AlertItem alert(String key, String title, String content, String tone, Long count) {
        DcCookChefWorkbenchVo.AlertItem item = new DcCookChefWorkbenchVo.AlertItem();
        item.setKey(key);
        item.setTitle(title);
        item.setContent(content);
        item.setTone(tone);
        item.setCount(count);
        return item;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private BigDecimal sumCommissionAmount(Long chefId, Date monthStart, Date nextMonthStart) {
        BigDecimal chefRate = BigDecimal.ONE.subtract(getDecimalConfig("dc.cooking.platform.rate", DEFAULT_PLATFORM_RATE));
        return selectCompletedOrdersInRange(chefId, monthStart, nextMonthStart).stream()
            .map(order -> calculateCommission(order.getPayAmount(), chefRate))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<DcCookOrder> selectCompletedOrdersInMonth(Long chefId, Date monthStart, Date nextMonthStart) {
        return selectCompletedOrdersInRange(chefId, monthStart, nextMonthStart);
    }

    private List<DcCookOrder> selectCompletedOrdersInRange(Long chefId, Date start, Date end) {
        return orderMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.COMPLETED))
            .and(wrapper -> wrapper
                .ge(DcCookOrder::getCompleteTime, start)
                .lt(DcCookOrder::getCompleteTime, end)
                .or(fallback -> fallback
                    .isNull(DcCookOrder::getCompleteTime)
                    .ge(DcCookOrder::getConfirmTime, start)
                    .lt(DcCookOrder::getConfirmTime, end))
                .or(fallback -> fallback
                    .isNull(DcCookOrder::getCompleteTime)
                    .isNull(DcCookOrder::getConfirmTime)
                    .ge(DcCookOrder::getPayTime, start)
                    .lt(DcCookOrder::getPayTime, end))));
    }

    private DcCookChefCommissionOrdersVo.Row buildCommissionRow(DcCookOrder order, DcCookReview review, BigDecimal chefRate) {
        DcCookChefCommissionOrdersVo.Row row = new DcCookChefCommissionOrdersVo.Row();
        row.setOrderId(order.getOrderId());
        row.setOrderNo(order.getOrderNo());
        row.setPayAmount(defaultAmount(order.getPayAmount()));
        row.setCommissionAmount(calculateCommission(order.getPayAmount(), chefRate));
        row.setCompleteTime(resolveCompleteTime(order));
        if (review != null) {
            row.setRating(review.getRating());
            row.setReviewContent(review.getContent());
            row.setReviewTime(review.getReviewTime());
        }
        return row;
    }

    private BigDecimal calculateCommission(BigDecimal payAmount, BigDecimal chefRate) {
        return defaultAmount(payAmount).multiply(chefRate).setScale(2, RoundingMode.HALF_UP);
    }

    private Date resolveCompleteTime(DcCookOrder order) {
        if (order.getCompleteTime() != null) {
            return order.getCompleteTime();
        }
        if (order.getConfirmTime() != null) {
            return order.getConfirmTime();
        }
        return order.getPayTime();
    }

    private YearMonth parseMonth(String month) {
        if (StringUtils.isBlank(month)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (Exception e) {
            throw new ServiceException("invalid month, expected yyyy-MM");
        }
    }

    private BigDecimal getDecimalConfig(String key, BigDecimal defaultValue) {
        try {
            String value = configService.selectConfigValueByKey(key);
            return StringUtils.isBlank(value) ? defaultValue : new BigDecimal(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private DcCookChef requireAvailableWorkbenchChef(Long userId) {
        DcCookChef chef = requireChefByUserId(userId);
        if (!DcCookChefStatus.matchesAuditStatus(chef.getAuditStatus(), AUDIT_APPROVED)
            || (!DcCookChefStatus.matchesChefStatus(chef.getChefStatus(), STATUS_NORMAL)
            && !DcCookChefStatus.matchesChefStatus(chef.getChefStatus(), STATUS_PAUSED))) {
            throw new ServiceException("服务厨师审核通过后可访问该功能", HttpStatus.FORBIDDEN);
        }
        return chef;
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private DcCookChef requireChefByUserId(Long userId) {
        DcCookChef chef = baseMapper.selectOne(Wrappers.lambdaQuery(DcCookChef.class)
            .eq(DcCookChef::getUserId, userId)
            .orderByDesc(DcCookChef::getCreateTime)
            .last("limit 1"), false);
        if (chef == null) {
            throw new ServiceException("请先申请成为服务厨师", HttpStatus.FORBIDDEN);
        }
        return chef;
    }

    private boolean hasUnfinishedOrder(Long chefId) {
        return orderMapper.exists(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .notIn(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.TERMINAL_STATUSES)));
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
            .map(item -> format.format(item.getStartTime()) + " - " + format.format(item.getEndTime()))
            .collect(Collectors.joining("; "));
    }
}
