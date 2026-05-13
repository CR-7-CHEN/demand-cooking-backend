package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookChefStatus;
import org.dromara.system.domain.cooking.DcCookComplaint;
import org.dromara.system.domain.cooking.DcCookComplaintStatus;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.vo.cooking.DcCookDashboardOverviewVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookComplaintMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.service.cooking.IDcCookDashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookDashboardServiceImpl implements IDcCookDashboardService {

    private static final String USER_TYPE_APP = "app_user";
    private static final String TREND_MODE_WEEK = "week";
    private static final DateTimeFormatter TREND_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TREND_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM.dd");
    private static final List<String> PAID_ORDER_STATUSES = Arrays.asList(
        DcCookOrderStatus.WAITING_SERVICE,
        DcCookOrderStatus.WAITING_CONFIRM,
        DcCookOrderStatus.COMPLETED
    );
    private static final String[] WEEK_LABELS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    private final DcCookOrderMapper orderMapper;
    private final DcCookChefMapper chefMapper;
    private final DcCookComplaintMapper complaintMapper;
    private final SysUserMapper userMapper;

    @Override
    public DcCookDashboardOverviewVo overview(String trendMode) {
        LocalDate today = LocalDate.now();
        Date todayStart = toDate(today);
        Date tomorrowStart = toDate(today.plusDays(1));
        String normalizedTrendMode = normalizeTrendMode(trendMode);

        DcCookDashboardOverviewVo vo = new DcCookDashboardOverviewVo();
        vo.setTrendMode(normalizedTrendMode);
        vo.setTodayOrders(countTodayOrders(todayStart, tomorrowStart));
        vo.setTodayRevenue(sumRevenue(todayStart, tomorrowStart));
        vo.setServiceChefCount(countServiceChefs());
        vo.setResignedChefCount(countResignedChefs());
        vo.setRegisteredUserCount(countAppUsers(null, null));
        vo.setTodayNewUserCount(countAppUsers(todayStart, tomorrowStart));
        vo.setRevenueTrend(buildRevenueTrend(today));
        vo.setPendingItems(buildPendingItems());
        vo.setRecentOrders(buildRecentOrders());
        vo.setTopRatedChefs(buildTopRatedChefs());
        return vo;
    }

    private Long countTodayOrders(Date start, Date end) {
        return orderMapper.selectCount(Wrappers.lambdaQuery(DcCookOrder.class)
            .ge(DcCookOrder::getCreateTime, start)
            .lt(DcCookOrder::getCreateTime, end));
    }

    private BigDecimal sumRevenue(Date start, Date end) {
        List<DcCookOrder> orders = orderMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .select(DcCookOrder::getPayAmount)
            .isNotNull(DcCookOrder::getPayAmount)
            .ge(DcCookOrder::getPayTime, start)
            .lt(DcCookOrder::getPayTime, end)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(PAID_ORDER_STATUSES)));
        return sumPayAmount(orders);
    }

    private Long countServiceChefs() {
        return chefMapper.selectCount(Wrappers.lambdaQuery(DcCookChef.class)
            .and(wrapper -> wrapper
                .in(DcCookChef::getChefStatus, DcCookChefStatus.compatibleChefStatuses(DcCookChefStatus.NORMAL))
                .or(inner -> inner
                    .in(DcCookChef::getAuditStatus, DcCookChefStatus.compatibleAuditStatuses(DcCookChefStatus.AUDIT_APPROVED))
                    .in(DcCookChef::getChefStatus, DcCookChefStatus.compatibleChefStatuses(DcCookChefStatus.NORMAL)))));
    }

    private Long countResignedChefs() {
        return chefMapper.selectCount(Wrappers.lambdaQuery(DcCookChef.class)
            .in(DcCookChef::getChefStatus, DcCookChefStatus.compatibleChefStatuses(DcCookChefStatus.RESIGNED)));
    }

    private Long countAppUsers(Date start, Date end) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery(SysUser.class)
            .eq(SysUser::getUserType, USER_TYPE_APP);
        if (start != null && end != null) {
            wrapper.ge(SysUser::getCreateTime, start)
                .lt(SysUser::getCreateTime, end);
        }
        return userMapper.selectCount(wrapper);
    }

    private List<DcCookDashboardOverviewVo.TrendItem> buildRevenueTrend(LocalDate today) {
        LocalDate firstDate = today.minusDays(6);
        Date start = toDate(firstDate);
        Date end = toDate(today.plusDays(1));
        List<DcCookOrder> orders = orderMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .select(DcCookOrder::getPayAmount, DcCookOrder::getPayTime)
            .isNotNull(DcCookOrder::getPayAmount)
            .isNotNull(DcCookOrder::getPayTime)
            .ge(DcCookOrder::getPayTime, start)
            .lt(DcCookOrder::getPayTime, end)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(PAID_ORDER_STATUSES)));

        Map<LocalDate, BigDecimal> amountMap = new HashMap<>();
        for (DcCookOrder order : orders) {
            LocalDate payDate = toLocalDate(order.getPayTime());
            amountMap.merge(payDate, safeAmount(order.getPayAmount()), BigDecimal::add);
        }

        List<DcCookDashboardOverviewVo.TrendItem> trend = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate date = firstDate.plusDays(i);
            DcCookDashboardOverviewVo.TrendItem item = new DcCookDashboardOverviewVo.TrendItem();
            item.setLabel(TREND_LABEL_FORMAT.format(date));
            item.setDate(TREND_DATE_FORMAT.format(date));
            item.setAmount(amountMap.getOrDefault(date, BigDecimal.ZERO));
            trend.add(item);
        }
        return trend;
    }

    private List<DcCookDashboardOverviewVo.PendingItem> buildPendingItems() {
        List<DcCookDashboardOverviewVo.PendingItem> items = new ArrayList<>(3);
        items.add(pendingItem("chefAudit", "厨师审核待处理", DcCookChefStatus.AUDIT_PENDING, countPendingChefAudits(), "danger"));
        items.add(pendingItem("complaintReply", "用户投诉待处理", DcCookComplaintStatus.PENDING, countPendingComplaints(), "warning"));
        items.add(pendingItem("chefService", "厨师待服务", DcCookOrderStatus.WAITING_SERVICE, countWaitingServiceOrders(), "success"));
        return items;
    }

    private Long countPendingChefAudits() {
        return chefMapper.selectCount(Wrappers.lambdaQuery(DcCookChef.class)
            .in(DcCookChef::getAuditStatus, DcCookChefStatus.compatibleAuditStatuses(DcCookChefStatus.AUDIT_PENDING)));
    }

    private Long countPendingComplaints() {
        return complaintMapper.selectCount(Wrappers.lambdaQuery(DcCookComplaint.class)
            .in(DcCookComplaint::getStatus, DcCookComplaintStatus.compatibleStatuses(DcCookComplaintStatus.PENDING)));
    }

    private Long countWaitingServiceOrders() {
        return orderMapper.selectCount(Wrappers.lambdaQuery(DcCookOrder.class)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.WAITING_SERVICE)));
    }

    private DcCookDashboardOverviewVo.PendingItem pendingItem(String key, String label, String status, Long count, String tone) {
        DcCookDashboardOverviewVo.PendingItem item = new DcCookDashboardOverviewVo.PendingItem();
        item.setKey(key);
        item.setLabel(label);
        item.setStatus(status);
        item.setCount(count == null ? 0L : count);
        item.setTone(tone);
        return item;
    }

    private List<DcCookDashboardOverviewVo.RecentOrderItem> buildRecentOrders() {
        Page<DcCookOrder> page = orderMapper.selectPage(new Page<>(1, 10), Wrappers.lambdaQuery(DcCookOrder.class)
            .select(DcCookOrder::getOrderNo, DcCookOrder::getStatus, DcCookOrder::getCreateTime)
            .orderByDesc(DcCookOrder::getCreateTime));
        List<DcCookDashboardOverviewVo.RecentOrderItem> recentOrders = new ArrayList<>();
        for (DcCookOrder order : page.getRecords()) {
            DcCookDashboardOverviewVo.RecentOrderItem item = new DcCookDashboardOverviewVo.RecentOrderItem();
            item.setOrderNo(order.getOrderNo());
            item.setStatus(DcCookOrderStatus.normalize(order.getStatus()));
            item.setStatusLabel(statusLabel(order.getStatus()));
            item.setCreateTime(order.getCreateTime());
            recentOrders.add(item);
        }
        return recentOrders;
    }

    private List<DcCookDashboardOverviewVo.TopChefItem> buildTopRatedChefs() {
        Page<DcCookChef> page = chefMapper.selectPage(new Page<>(1, 5), Wrappers.lambdaQuery(DcCookChef.class)
            .select(DcCookChef::getChefId, DcCookChef::getChefName, DcCookChef::getRating,
                DcCookChef::getCompletedOrders, DcCookChef::getAvatarUrl)
            .and(wrapper -> wrapper
                .in(DcCookChef::getChefStatus, DcCookChefStatus.compatibleChefStatuses(DcCookChefStatus.NORMAL))
                .or(inner -> inner
                    .in(DcCookChef::getAuditStatus, DcCookChefStatus.compatibleAuditStatuses(DcCookChefStatus.AUDIT_APPROVED))
                    .in(DcCookChef::getChefStatus, DcCookChefStatus.compatibleChefStatuses(DcCookChefStatus.NORMAL))))
            .orderByDesc(DcCookChef::getRating)
            .orderByDesc(DcCookChef::getCompletedOrders));
        List<DcCookDashboardOverviewVo.TopChefItem> result = new ArrayList<>(5);
        for (DcCookChef chef : page.getRecords()) {
            DcCookDashboardOverviewVo.TopChefItem item = new DcCookDashboardOverviewVo.TopChefItem();
            item.setChefId(chef.getChefId());
            item.setChefName(chef.getChefName());
            item.setRating(chef.getRating());
            item.setCompletedOrders(chef.getCompletedOrders());
            item.setAvatarUrl(chef.getAvatarUrl());
            result.add(item);
        }
        return result;
    }

    private String statusLabel(String status) {
        if (DcCookOrderStatus.matches(status, DcCookOrderStatus.WAITING_RESPONSE)) {
            return "待派单";
        }
        if (DcCookOrderStatus.matches(status, DcCookOrderStatus.WAITING_SERVICE)
            || DcCookOrderStatus.matches(status, DcCookOrderStatus.WAITING_CONFIRM)) {
            return "进行中";
        }
        if (DcCookOrderStatus.matches(status, DcCookOrderStatus.COMPLETED)) {
            return "已完成";
        }
        return status;
    }

    private BigDecimal sumPayAmount(List<DcCookOrder> orders) {
        BigDecimal amount = BigDecimal.ZERO;
        for (DcCookOrder order : orders) {
            amount = amount.add(safeAmount(order.getPayAmount()));
        }
        return amount;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String normalizeTrendMode(String trendMode) {
        return TREND_MODE_WEEK;
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
