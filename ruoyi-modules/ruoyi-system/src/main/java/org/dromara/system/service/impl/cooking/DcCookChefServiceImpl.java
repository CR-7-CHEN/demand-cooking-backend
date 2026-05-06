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
import org.dromara.system.domain.cooking.DcCookReview;
import org.dromara.system.domain.cooking.DcCookSettlement;
import org.dromara.system.domain.vo.cooking.DcCookChefCommissionOrdersVo;
import org.dromara.system.domain.vo.cooking.DcCookChefWorkbenchVo;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;
import org.dromara.system.domain.vo.cooking.DcCookChefTimeVo;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.mapper.cooking.DcCookSettlementMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.springframework.stereotype.Service;

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
    private final DcCookReviewMapper reviewMapper;
    private final DcCookSettlementMapper settlementMapper;
    private final IDcCookConfigService configService;

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
        vo.setTakingOrders(STATUS_NORMAL.equals(chef.getChefStatus()));
        vo.setRevenueOverview(buildRevenueOverview(chef.getChefId(), todayStart, tomorrowStart, monthStart, nextMonthStart));
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

    private DcCookChefWorkbenchVo.RevenueOverview buildRevenueOverview(Long chefId, Date todayStart, Date tomorrowStart,
                                                                       Date monthStart, Date nextMonthStart) {
        DcCookChefWorkbenchVo.RevenueOverview overview = new DcCookChefWorkbenchVo.RevenueOverview();
        overview.setTodayIncome(sumPaidAmount(chefId, todayStart, tomorrowStart));
        overview.setMonthIncome(sumPaidAmount(chefId, monthStart, nextMonthStart));
        overview.setMonthCompletedOrders(countMonthCompletedOrders(chefId, monthStart, nextMonthStart));
        overview.setMonthCommissionAmount(sumCommissionAmount(chefId, monthStart, nextMonthStart));

        DcCookSettlement settlement = settlementMapper.selectOne(Wrappers.lambdaQuery(DcCookSettlement.class)
            .eq(DcCookSettlement::getChefId, chefId)
            .eq(DcCookSettlement::getSettlementMonth, YearMonth.now().toString())
            .orderByDesc(DcCookSettlement::getGeneratedTime)
            .orderByDesc(DcCookSettlement::getSettlementId)
            .last("limit 1"), false);
        overview.setMonthDeduction(defaultAmount(settlement == null ? null : settlement.getViolationDeduction()));
        overview.setMonthPayableAmount(defaultAmount(settlement == null ? null : settlement.getPayableAmount()));
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
            .eq(DcCookOrder::getStatus, DcCookOrderStatus.WAITING_SERVICE)
            .ge(DcCookOrder::getServiceStartTime, todayStart)
            .lt(DcCookOrder::getServiceStartTime, tomorrowStart));
        if (todayServiceCount > 0) {
            alerts.add(alert("today_service", "今日待服务",
                "今天有待上门服务订单，请留意时间安排。", "info", todayServiceCount));
        }

        if (STATUS_PAUSED.equals(chef.getChefStatus())) {
            alerts.add(alert("paused", "已暂停接单",
                "准备好后可在首页恢复接单。", "info", 1L));
        }
        return alerts;
    }

    private List<DcCookChefWorkbenchVo.TrendItem> buildRevenueTrend(Long chefId, LocalDate today) {
        LocalDate startDate = today.minusDays(6);
        Date start = toDate(startDate);
        Date end = toDate(today.plusDays(1));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM.dd");

        Map<LocalDate, BigDecimal> amountByDate = orderMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
                .eq(DcCookOrder::getChefId, chefId)
                .in(DcCookOrder::getStatus, paidStatuses())
                .ge(DcCookOrder::getPayTime, start)
                .lt(DcCookOrder::getPayTime, end))
            .stream()
            .filter(order -> order.getPayTime() != null)
            .collect(Collectors.groupingBy(order -> toLocalDate(order.getPayTime()),
                Collectors.reducing(BigDecimal.ZERO, order -> defaultAmount(order.getPayAmount()), BigDecimal::add)));

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

    private BigDecimal sumPaidAmount(Long chefId, Date start, Date end) {
        return orderMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
                .eq(DcCookOrder::getChefId, chefId)
                .in(DcCookOrder::getStatus, paidStatuses())
                .ge(DcCookOrder::getPayTime, start)
                .lt(DcCookOrder::getPayTime, end))
            .stream()
            .map(DcCookOrder::getPayAmount)
            .map(this::defaultAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long countMonthCompletedOrders(Long chefId, Date monthStart, Date nextMonthStart) {
        return (long) selectCompletedOrdersInMonth(chefId, monthStart, nextMonthStart).size();
    }

    private Long countOrdersByStatus(Long chefId, String status) {
        return orderMapper.selectCount(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .eq(DcCookOrder::getStatus, status));
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

    private List<String> paidStatuses() {
        return List.of(DcCookOrderStatus.WAITING_SERVICE, DcCookOrderStatus.WAITING_CONFIRM, DcCookOrderStatus.COMPLETED);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private BigDecimal sumCommissionAmount(Long chefId, Date monthStart, Date nextMonthStart) {
        BigDecimal chefRate = BigDecimal.ONE.subtract(getDecimalConfig("dc.cooking.platform.rate", DEFAULT_PLATFORM_RATE));
        return selectCompletedOrdersInMonth(chefId, monthStart, nextMonthStart).stream()
            .map(order -> calculateCommission(order.getPayAmount(), chefRate))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<DcCookOrder> selectCompletedOrdersInMonth(Long chefId, Date monthStart, Date nextMonthStart) {
        return orderMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .eq(DcCookOrder::getStatus, DcCookOrderStatus.COMPLETED)
            .and(wrapper -> wrapper
                .ge(DcCookOrder::getCompleteTime, monthStart)
                .lt(DcCookOrder::getCompleteTime, nextMonthStart)
                .or(fallback -> fallback
                    .isNull(DcCookOrder::getCompleteTime)
                    .ge(DcCookOrder::getConfirmTime, monthStart)
                    .lt(DcCookOrder::getConfirmTime, nextMonthStart))
                .or(fallback -> fallback
                    .isNull(DcCookOrder::getCompleteTime)
                    .isNull(DcCookOrder::getConfirmTime)
                    .ge(DcCookOrder::getPayTime, monthStart)
                    .lt(DcCookOrder::getPayTime, nextMonthStart))));
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
        if (!AUDIT_APPROVED.equals(chef.getAuditStatus())
            || (!STATUS_NORMAL.equals(chef.getChefStatus()) && !STATUS_PAUSED.equals(chef.getChefStatus()))) {
            throw new ServiceException("chef workbench unavailable");
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
