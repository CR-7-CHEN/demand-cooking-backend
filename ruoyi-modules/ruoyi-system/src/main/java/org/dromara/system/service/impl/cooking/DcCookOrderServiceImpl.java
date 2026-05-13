package org.dromara.system.service.impl.cooking;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.cooking.DcCookOrderActionBo;
import org.dromara.system.domain.bo.cooking.DcCookOrderBo;
import org.dromara.system.domain.cooking.DcCookAddress;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookChefTime;
import org.dromara.system.domain.cooking.DcCookMessage;
import org.dromara.system.domain.cooking.DcCookMessageStatus;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookChefStatus;
import org.dromara.system.domain.vo.cooking.DcCookOrderCancelPreviewVo;
import org.dromara.system.domain.vo.cooking.DcCookOrderVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookAddressMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookMessageMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.cooking.IDcCookOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DcCookOrderServiceImpl implements IDcCookOrderService {

    private static final BigDecimal MIN_QUOTE_AMOUNT = new BigDecimal("50");
    private static final BigDecimal DEFAULT_CANCEL_FEE_RATE = new BigDecimal("0.20");
    private static final int DEFAULT_RESPONSE_MINUTES = 30;
    private static final int DEFAULT_PAY_MINUTES = 30;
    private static final int DEFAULT_CONFIRM_HOURS = 24;
    private static final int DEFAULT_SERVICE_HOURS = 3;
    private static final int DEFAULT_RESERVE_MIN_ADVANCE_MINUTES = 60;
    private static final int DEFAULT_RESERVE_FUTURE_DAYS = 3;
    private static final String TIME_ENABLED = "0";

    private final DcCookOrderMapper baseMapper;
    private final DcCookChefMapper chefMapper;
    private final DcCookChefTimeMapper chefTimeMapper;
    private final DcCookAddressMapper addressMapper;
    private final DcCookMessageMapper messageMapper;
    private final SysUserMapper userMapper;
    private final IDcCookConfigService configService;

    @Override
    public DcCookOrderVo queryById(Long orderId) {
        DcCookOrderVo vo = baseMapper.selectVoById(orderId);
        if (vo != null) {
            normalizeReadStatus(vo);
            hydrateDisplayNames(List.of(vo));
        }
        return vo;
    }

    @Override
    public TableDataInfo<DcCookOrderVo> queryPageList(DcCookOrderBo bo, PageQuery pageQuery) {
        Page<DcCookOrderVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        if (page.getRecords() != null) {
            page.getRecords().forEach(this::normalizeReadStatus);
        }
        hydrateDisplayNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"'dcCookOrderSubmit:' + #bo.chefId"}, acquireTimeout = 5000)
    @Override
    public DcCookOrderVo submit(DcCookOrderBo bo) {
        DcCookChef chef = chefMapper.selectById(bo.getChefId());
        if (chef == null) {
            throw new ServiceException("chef not found");
        }
        assertChefCanTakeOrder(chef);
        if (bo.getServiceStartTime() == null) {
            throw new ServiceException("serviceStartTime is required");
        }
        Date serviceStartTime = bo.getServiceStartTime();
        Date serviceEndTime = addHours(serviceStartTime, getIntConfig("cooking.service.duration.hours", DEFAULT_SERVICE_HOURS));
        DcCookChefTimeServiceImpl.validateHalfHourBoundary(serviceStartTime, serviceEndTime);
        assertReservationTimeAvailable(chef.getChefId(), serviceStartTime, serviceEndTime);
        DcCookOrder order = buildSubmitOrder(bo);
        order.setOrderNo(generateOrderNo());
        order.setStatus(DcCookOrderStatus.WAITING_RESPONSE);
        order.setServiceEndTime(serviceEndTime);
        order.setQuoteUpdateCount(0);
        order.setObjectionCount(0);
        fillAddressSnapshot(order);
        baseMapper.insert(order);
        recordMessage("ORDER_SUBMIT", "CHEF", order.getChefId(), order, "New cooking order submitted");
        return baseMapper.selectVoById(order.getOrderId());
    }

    private DcCookOrder buildSubmitOrder(DcCookOrderBo bo) {
        DcCookOrder order = new DcCookOrder();
        order.setOrderId(bo.getOrderId());
        order.setOrderNo(bo.getOrderNo());
        order.setUserId(bo.getUserId());
        order.setChefId(bo.getChefId());
        order.setAddressId(bo.getAddressId());
        order.setContactName(bo.getContactName());
        order.setContactPhone(bo.getContactPhone());
        order.setServiceArea(bo.getServiceArea());
        order.setAddressSnapshot(bo.getAddressSnapshot());
        order.setDishSnapshot(bo.getDishSnapshot());
        order.setUserRemark(bo.getUserRemark());
        order.setServiceStartTime(bo.getServiceStartTime());
        order.setServiceEndTime(bo.getServiceEndTime());
        order.setStatus(bo.getStatus());
        order.setQuoteAmount(bo.getQuoteAmount());
        order.setQuoteRemark(bo.getQuoteRemark());
        order.setPayDeadline(bo.getPayDeadline());
        order.setPayAmount(bo.getPayAmount());
        order.setRemark(bo.getRemark());
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean quote(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        if (!DcCookOrderStatus.matches(order.getStatus(), DcCookOrderStatus.WAITING_RESPONSE)
            && !DcCookOrderStatus.matches(order.getStatus(), DcCookOrderStatus.PRICE_OBJECTION)) {
            throw new ServiceException("order cannot be quoted now");
        }
        if (bo.getQuoteAmount() == null || bo.getQuoteAmount().compareTo(MIN_QUOTE_AMOUNT) < 0) {
            throw new ServiceException("quoteAmount must be at least 50");
        }
        if (DcCookOrderStatus.matches(order.getStatus(), DcCookOrderStatus.WAITING_RESPONSE)) {
            order.setPayDeadline(addMinutes(new Date(), getIntConfig("cooking.pay.timeout.minutes", DEFAULT_PAY_MINUTES)));
        } else if (order.getQuoteUpdateCount() != null && order.getQuoteUpdateCount() >= 2) {
            throw new ServiceException("quote can only be updated once after objection");
        }
        order.setStatus(DcCookOrderStatus.WAITING_PAY);
        order.setQuoteAmount(bo.getQuoteAmount());
        order.setPayAmount(bo.getQuoteAmount());
        order.setQuoteRemark(bo.getQuoteRemark());
        order.setQuoteTime(new Date());
        order.setObjectionHandleTime(new Date());
        order.setQuoteUpdateCount((order.getQuoteUpdateCount() == null ? 0 : order.getQuoteUpdateCount()) + 1);
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("ORDER_QUOTE", "USER", order.getUserId(), order, "Chef quoted cooking order");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean reject(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        assertStatus(order, DcCookOrderStatus.WAITING_RESPONSE);
        order.setStatus(DcCookOrderStatus.REJECTED_CLOSED);
        order.setCancelType("CHEF_REJECT");
        order.setCancelReason(bo.getCancelReason());
        order.setCancelTime(new Date());
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("ORDER_REJECT", "USER", order.getUserId(), order, "Chef rejected cooking order");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean objection(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        assertStatus(order, DcCookOrderStatus.WAITING_PAY);
        if (order.getObjectionCount() != null && order.getObjectionCount() >= 1) {
            throw new ServiceException("objection can only be submitted once");
        }
        order.setStatus(DcCookOrderStatus.PRICE_OBJECTION);
        order.setObjectionCount(1);
        order.setObjectionReason(bo.getObjectionReason());
        order.setObjectionRemark(bo.getObjectionRemark());
        order.setObjectionTime(new Date());
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("ORDER_OBJECTION", "CHEF", order.getChefId(), order, "User objected to quote");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean paySuccess(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        if (!DcCookOrderStatus.matches(order.getStatus(), DcCookOrderStatus.WAITING_PAY)
            && !DcCookOrderStatus.matches(order.getStatus(), DcCookOrderStatus.PRICE_OBJECTION)) {
            throw new ServiceException("order cannot be paid now");
        }
        if (order.getPayDeadline() != null && order.getPayDeadline().before(new Date())) {
            order.setStatus(DcCookOrderStatus.PAY_TIMEOUT_CLOSED);
            baseMapper.updateById(order);
            throw new ServiceException("pay deadline expired");
        }
        order.setStatus(DcCookOrderStatus.WAITING_SERVICE);
        order.setPayAmount(bo.getPayAmount() == null ? order.getQuoteAmount() : bo.getPayAmount());
        order.setPayNo(StringUtils.isBlank(bo.getPayNo()) ? "MOCK" + System.currentTimeMillis() : bo.getPayNo());
        order.setPayTime(new Date());
        order.setServiceStartedFlag("0");
        order.setServiceStartedTime(null);
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("PAY_SUCCESS", "CHEF", order.getChefId(), order, "Mock payment success");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean serviceStart(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        assertStatus(order, DcCookOrderStatus.WAITING_SERVICE);
        if (hasServiceStarted(order)) {
            throw new ServiceException("service has already started");
        }
        Date startedAt = new Date();
        if (order.getServiceStartTime() != null && startedAt.before(order.getServiceStartTime())) {
            throw new ServiceException("service start time not reached");
        }
        order.setServiceStartedFlag("1");
        order.setServiceStartedTime(startedAt);
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("SERVICE_START", "USER", order.getUserId(), order, "Chef started cooking service");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean serviceComplete(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        assertStatus(order, DcCookOrderStatus.WAITING_SERVICE);
        if (!hasServiceStarted(order)) {
            throw new ServiceException("service has not started");
        }
        Date completedAt = new Date();
        order.setStatus(DcCookOrderStatus.WAITING_CONFIRM);
        order.setServiceCompleteTime(completedAt);
        order.setServiceCompleteType(DcCookOrderStatus.COMPLETE_BY_CHEF);
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("SERVICE_COMPLETE", "USER", order.getUserId(), order, "Chef marked service complete");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean confirm(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        if (!DcCookOrderStatus.matches(order.getStatus(), DcCookOrderStatus.WAITING_CONFIRM)) {
            throw new ServiceException("当前订单还未进入待确认状态，不能确认完成");
        }
        order.setStatus(DcCookOrderStatus.COMPLETED);
        order.setConfirmTime(new Date());
        order.setCompleteTime(new Date());
        boolean ok = baseMapper.updateById(order) > 0;
        incrementChefCompleted(order.getChefId());
        recordMessage("ORDER_CONFIRM", "CHEF", order.getChefId(), order, "User confirmed order complete");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int processScheduledStatusTransitions() {
        Date now = new Date();
        int processed = 0;
        processed += closeResponseTimeoutOrders(now);
        processed += closePayTimeoutOrders(now);
        processed += closeObjectionTimeoutOrders(now);
        processed += autoServiceCompleteOrders(now);
        processed += autoConfirmCompleteOrders(now);
        return processed;
    }

    @Override
    public DcCookOrderCancelPreviewVo previewUserCancel(Long orderId) {
        DcCookOrder order = requireOrder(orderId);
        DcCookOrderCancelPreviewVo vo = new DcCookOrderCancelPreviewVo();
        vo.setOrderId(orderId);
        vo.setPayAmount(order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount());
        BigDecimal feeRate = needCancelFee(order) ? getDecimalConfig("cooking.cancel.fee.rate", DEFAULT_CANCEL_FEE_RATE) : BigDecimal.ZERO;
        BigDecimal feeAmount = vo.getPayAmount().multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        vo.setFeeRate(feeRate);
        vo.setFeeAmount(feeAmount);
        vo.setRefundAmount(vo.getPayAmount().subtract(feeAmount).max(BigDecimal.ZERO));
        vo.setRefundNotice("Mock refund result will be marked as refunded immediately.");
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean userCancel(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        if (DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.UNPAID_CANCELABLE).contains(order.getStatus())) {
            order.setStatus(DcCookOrderStatus.CANCELED);
            order.setCancelType(DcCookOrderStatus.CANCEL_USER_UNPAID);
            order.setCancelReason(bo.getCancelReason());
            order.setCancelTime(new Date());
            return baseMapper.updateById(order) > 0;
        }
        assertPaidCancelable(order);
        DcCookOrderCancelPreviewVo preview = previewUserCancel(order.getOrderId());
        order.setStatus(DcCookOrderStatus.REFUNDED);
        order.setCancelType(DcCookOrderStatus.CANCEL_USER_PAID);
        order.setCancelReason(bo.getCancelReason());
        order.setCancelTime(new Date());
        order.setRefundAmount(preview.getRefundAmount());
        order.setRefundFeeAmount(preview.getFeeAmount());
        order.setRefundFeeRate(preview.getFeeRate());
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("ORDER_REFUND", "USER", order.getUserId(), order, "Mock refund success");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean chefCancel(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        assertPaidCancelable(order);
        if (StringUtils.isBlank(bo.getCancelReason())) {
            throw new ServiceException("cancelReason is required");
        }
        order.setStatus(DcCookOrderStatus.REFUNDED);
        order.setCancelType(DcCookOrderStatus.CANCEL_CHEF);
        order.setCancelReason(bo.getCancelReason());
        order.setCancelTime(new Date());
        order.setRefundAmount(order.getPayAmount());
        order.setRefundFeeAmount(BigDecimal.ZERO);
        order.setRefundFeeRate(BigDecimal.ZERO);
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("CHEF_CANCEL_REFUND", "USER", order.getUserId(), order, "Chef canceled and mock refund success");
        return ok;
    }

    private LambdaQueryWrapper<DcCookOrder> buildQueryWrapper(DcCookOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookOrder> lqw = Wrappers.lambdaQuery();
        List<String> groupedStatuses = resolveGroupedStatuses(bo.getStatusGroup());
        lqw.eq(bo.getOrderId() != null, DcCookOrder::getOrderId, bo.getOrderId());
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), DcCookOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getUserId() != null, DcCookOrder::getUserId, bo.getUserId());
        lqw.eq(bo.getChefId() != null, DcCookOrder::getChefId, bo.getChefId());
        lqw.apply(StringUtils.isNotBlank(bo.getChefName()),
            "chef_id in (select chef_id from dc_cook_chef where chef_name like concat('%', {0}, '%'))",
            bo.getChefName());
        if (StringUtils.isNotBlank(bo.getStatusGroup())) {
            if (groupedStatuses.isEmpty()) {
                lqw.eq(DcCookOrder::getOrderId, -1L);
            } else {
                lqw.in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(groupedStatuses));
            }
            String trimmedGroup = bo.getStatusGroup().trim();
            if (DcCookOrderStatus.USER_TAB_PAID.equals(trimmedGroup)) {
                lqw.eq(DcCookOrder::getServiceStartedFlag, "0");
            } else if (DcCookOrderStatus.USER_TAB_SERVING.equals(trimmedGroup)) {
                lqw.and(w -> w
                    .notIn(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.WAITING_SERVICE))
                    .or(inner -> inner
                        .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.WAITING_SERVICE))
                        .eq(DcCookOrder::getServiceStartedFlag, "1"))
                );
            }
        } else {
            if (StringUtils.isNotBlank(bo.getStatus())) {
                lqw.in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(bo.getStatus()));
            }
        }
        if (StringUtils.isNotBlank(bo.getMonth())) {
            applySettlementMonthFilter(lqw, bo.getMonth(),
                StringUtils.isBlank(bo.getStatusGroup()) && StringUtils.isBlank(bo.getStatus()));
        }
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookOrder::getCreateTime, params.get("beginTime"), params.get("endTime"));
        if (StringUtils.isNotBlank(bo.getMonth())) {
            lqw.orderByDesc(DcCookOrder::getCompleteTime)
                .orderByDesc(DcCookOrder::getConfirmTime)
                .orderByDesc(DcCookOrder::getPayTime)
                .orderByDesc(DcCookOrder::getCreateTime);
        } else {
            lqw.orderByDesc(DcCookOrder::getCreateTime);
        }
        return lqw;
    }

    private void applySettlementMonthFilter(LambdaQueryWrapper<DcCookOrder> lqw, String month, boolean defaultCompletedOnly) {
        MonthRange range = resolveMonthRange(month);
        if (defaultCompletedOnly) {
            lqw.in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.COMPLETED));
        }
        lqw.and(wrapper -> wrapper
            .ge(DcCookOrder::getCompleteTime, range.start())
            .lt(DcCookOrder::getCompleteTime, range.end())
            .or(fallback -> fallback
                .isNull(DcCookOrder::getCompleteTime)
                .ge(DcCookOrder::getConfirmTime, range.start())
                .lt(DcCookOrder::getConfirmTime, range.end()))
            .or(fallback -> fallback
                .isNull(DcCookOrder::getCompleteTime)
                .isNull(DcCookOrder::getConfirmTime)
                .ge(DcCookOrder::getPayTime, range.start())
                .lt(DcCookOrder::getPayTime, range.end())));
    }

    private MonthRange resolveMonthRange(String month) {
        YearMonth yearMonth = parseMonth(month);
        return new MonthRange(
            Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
            Date.from(yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
        );
    }

    private YearMonth parseMonth(String month) {
        String normalized = StringUtils.trim(month);
        if (StringUtils.isNotBlank(normalized) && normalized.matches("\\d{6}")) {
            normalized = normalized.substring(0, 4) + "-" + normalized.substring(4, 6);
        }
        try {
            return YearMonth.parse(normalized);
        } catch (Exception e) {
            throw new ServiceException("invalid month, expected yyyy-MM");
        }
    }

    private List<String> resolveGroupedStatuses(String statusGroup) {
        if (StringUtils.isBlank(statusGroup)) {
            return List.of();
        }
        return DcCookOrderStatus.statusesForUserTab(statusGroup.trim());
    }

    private DcCookOrder requireOrder(Long orderId) {
        DcCookOrder order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("order not found");
        }
        return order;
    }

    private void assertStatus(DcCookOrder order, String status) {
        if (!DcCookOrderStatus.matches(order.getStatus(), status)) {
            throw new ServiceException("invalid order status");
        }
    }

    private void assertChefCanTakeOrder(DcCookChef chef) {
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!DcCookChefStatus.matchesAuditStatus(chef.getAuditStatus(), DcCookChefStatus.AUDIT_APPROVED)
            || !DcCookChefStatus.matchesChefStatus(chef.getChefStatus(), DcCookChefStatus.NORMAL)) {
            throw new ServiceException("chef cannot take order");
        }
        if (chef.getHealthCertExpireDate() != null && chef.getHealthCertExpireDate().before(today)) {
            throw new ServiceException("chef health certificate expired");
        }
    }

    private void assertReservationTimeAvailable(Long chefId, Date startTime, Date endTime) {
        Date now = new Date();
        Date earliest = addMinutes(now, getIntConfig("cooking.reserve.min.advance.minutes", DEFAULT_RESERVE_MIN_ADVANCE_MINUTES));
        if (startTime.before(earliest)) {
            throw new ServiceException("reservation time is too close");
        }
        Date latest = addDays(now, getIntConfig("cooking.reserve.future.days", DEFAULT_RESERVE_FUTURE_DAYS));
        if (startTime.after(latest)) {
            throw new ServiceException("reservation time exceeds future limit");
        }
        boolean inAvailableWindow = chefTimeMapper.exists(Wrappers.lambdaQuery(DcCookChefTime.class)
            .eq(DcCookChefTime::getChefId, chefId)
            .eq(DcCookChefTime::getStatus, TIME_ENABLED)
            .le(DcCookChefTime::getStartTime, startTime)
            .ge(DcCookChefTime::getEndTime, endTime));
        if (!inAvailableWindow) {
            throw new ServiceException("reservation time is not available for this chef");
        }
        boolean overlapped = baseMapper.exists(Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .notIn(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.TERMINAL_STATUSES))
            .and(wrapper -> wrapper.notIn(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.WAITING_CONFIRM))
                .or()
                .isNull(DcCookOrder::getServiceCompleteTime))
            .lt(DcCookOrder::getServiceStartTime, endTime)
            .gt(DcCookOrder::getServiceEndTime, startTime));
        if (overlapped) {
            throw new ServiceException("chef is locked for this time range");
        }
    }

    private void fillAddressSnapshot(DcCookOrder order) {
        if (order.getAddressId() == null) {
            return;
        }
        DcCookAddress address = addressMapper.selectById(order.getAddressId());
        if (address == null) {
            return;
        }
        order.setContactName(defaultIfBlank(order.getContactName(), address.getContactName()));
        order.setContactPhone(defaultIfBlank(order.getContactPhone(), address.getContactPhone()));
        order.setServiceArea(defaultIfBlank(order.getServiceArea(), address.getAreaName()));
        order.setAddressSnapshot(address.getAreaName() + " " + address.getDetailAddress() + " " + defaultIfBlank(address.getHouseNumber(), ""));
    }

    private String generateOrderNo() {
        String prefix = "OD" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        List<DcCookOrder> latestOrders = baseMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .select(DcCookOrder::getOrderNo)
            .likeRight(DcCookOrder::getOrderNo, prefix)
            .orderByDesc(DcCookOrder::getOrderNo)
            .last("limit 1"));
        int sequence = 1;
        if (latestOrders != null && !latestOrders.isEmpty() && StringUtils.isNotBlank(latestOrders.get(0).getOrderNo())) {
            sequence = Integer.parseInt(latestOrders.get(0).getOrderNo().substring(prefix.length())) + 1;
        }
        if (sequence > 9999) {
            throw new ServiceException("daily order number sequence exceeded");
        }
        return prefix + String.format("%04d", sequence);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private Date addHours(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    private Date addMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    private boolean needCancelFee(DcCookOrder order) {
        if (order.getPayTime() == null) {
            return false;
        }
        return addMinutes(order.getPayTime(), 10).before(new Date());
    }

    private void assertPaidCancelable(DcCookOrder order) {
        if (!DcCookOrderStatus.matches(order.getStatus(), DcCookOrderStatus.WAITING_SERVICE)) {
            throw new ServiceException("paid order cannot be canceled now");
        }
        if (hasServiceStarted(order)) {
            throw new ServiceException("service has already started");
        }
    }

    private boolean hasServiceStarted(DcCookOrder order) {
        return "1".equals(order.getServiceStartedFlag()) || order.getServiceStartedTime() != null;
    }

    private int getIntConfig(String key, int defaultValue) {
        try {
            String value = configService.selectConfigValueByKey(key);
            return StringUtils.isBlank(value) ? defaultValue : Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int closeResponseTimeoutOrders(Date now) {
        Date deadline = addMinutes(now, -getIntConfig("cooking.response.timeout.minutes", DEFAULT_RESPONSE_MINUTES));
        List<DcCookOrder> orders = baseMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.WAITING_RESPONSE))
            .le(DcCookOrder::getCreateTime, deadline));
        return transitionOrders(orders, DcCookOrderStatus.WAITING_RESPONSE, DcCookOrderStatus.RESPONSE_TIMEOUT_CLOSED, order -> {
            order.setCancelType("SYSTEM_TIMEOUT");
            order.setCancelReason("Chef response timeout");
            order.setCancelTime(now);
        }, "ORDER_RESPONSE_TIMEOUT", "USER", "Chef response timeout closed order");
    }

    private int closePayTimeoutOrders(Date now) {
        List<DcCookOrder> orders = baseMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(List.of(
                DcCookOrderStatus.WAITING_PAY, DcCookOrderStatus.PRICE_OBJECTION)))
            .isNotNull(DcCookOrder::getPayDeadline)
            .le(DcCookOrder::getPayDeadline, now));
        return transitionOrders(orders, null, DcCookOrderStatus.PAY_TIMEOUT_CLOSED, order -> {
            order.setCancelType("SYSTEM_TIMEOUT");
            order.setCancelReason("Payment timeout");
            order.setCancelTime(now);
        }, "ORDER_PAY_TIMEOUT", "USER", "Payment timeout closed order");
    }

    private int closeObjectionTimeoutOrders(Date now) {
        Date deadline = addMinutes(now, -getIntConfig("cooking.response.timeout.minutes", DEFAULT_RESPONSE_MINUTES));
        List<DcCookOrder> orders = baseMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.PRICE_OBJECTION))
            .isNotNull(DcCookOrder::getObjectionTime)
            .le(DcCookOrder::getObjectionTime, deadline)
            .and(wrapper -> wrapper.isNull(DcCookOrder::getPayDeadline).or().gt(DcCookOrder::getPayDeadline, now)));
        return transitionOrders(orders, DcCookOrderStatus.PRICE_OBJECTION, DcCookOrderStatus.OBJECTION_TIMEOUT_CLOSED, order -> {
            order.setCancelType("SYSTEM_TIMEOUT");
            order.setCancelReason("Quote objection timeout");
            order.setCancelTime(now);
        }, "ORDER_OBJECTION_TIMEOUT", "USER", "Quote objection timeout closed order");
    }

    private int autoServiceCompleteOrders(Date now) {
        List<DcCookOrder> orders = baseMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.WAITING_SERVICE))
            .isNotNull(DcCookOrder::getServiceEndTime)
            .le(DcCookOrder::getServiceEndTime, now));
        return transitionOrders(orders, DcCookOrderStatus.WAITING_SERVICE, DcCookOrderStatus.WAITING_CONFIRM, order -> {
            order.setServiceCompleteTime(now);
            order.setServiceCompleteType(DcCookOrderStatus.COMPLETE_BY_SYSTEM);
        }, "SERVICE_AUTO_COMPLETE", "USER", "System marked cooking service complete");
    }

    private int autoConfirmCompleteOrders(Date now) {
        Date deadline = addHours(now, -getIntConfig("cooking.confirm.timeout.hours", DEFAULT_CONFIRM_HOURS));
        List<DcCookOrder> orders = baseMapper.selectList(Wrappers.lambdaQuery(DcCookOrder.class)
            .in(DcCookOrder::getStatus, DcCookOrderStatus.compatibleStatuses(DcCookOrderStatus.WAITING_CONFIRM))
            .isNotNull(DcCookOrder::getServiceCompleteTime)
            .le(DcCookOrder::getServiceCompleteTime, deadline));
        return transitionOrders(orders, DcCookOrderStatus.WAITING_CONFIRM, DcCookOrderStatus.COMPLETED, order -> {
            order.setConfirmTime(now);
            order.setCompleteTime(now);
        }, "ORDER_AUTO_CONFIRM", "CHEF", "System auto confirmed order complete");
    }

    private int transitionOrders(List<DcCookOrder> orders, String expectedStatus, String nextStatus,
                                 Consumer<DcCookOrder> customizer, String messageType, String receiverType,
                                 String messageSummary) {
        int processed = 0;
        for (DcCookOrder order : orders) {
            String currentStatus = order.getStatus();
            if (expectedStatus != null && !DcCookOrderStatus.matches(currentStatus, expectedStatus)) {
                continue;
            }
            order.setStatus(nextStatus);
            customizer.accept(order);
            int updated = baseMapper.update(order, Wrappers.lambdaUpdate(DcCookOrder.class)
                .eq(DcCookOrder::getOrderId, order.getOrderId())
                .eq(DcCookOrder::getStatus, currentStatus));
            if (updated <= 0) {
                continue;
            }
            processed++;
            if (DcCookOrderStatus.matches(nextStatus, DcCookOrderStatus.COMPLETED)) {
                incrementChefCompleted(order.getChefId());
            }
            recordMessage(messageType, receiverType, receiverId(order, receiverType), order, messageSummary);
        }
        return processed;
    }

    private Long receiverId(DcCookOrder order, String receiverType) {
        return "CHEF".equals(receiverType) ? order.getChefId() : order.getUserId();
    }

    private BigDecimal getDecimalConfig(String key, BigDecimal defaultValue) {
        try {
            String value = configService.selectConfigValueByKey(key);
            return StringUtils.isBlank(value) ? defaultValue : new BigDecimal(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void incrementChefCompleted(Long chefId) {
        DcCookChef chef = chefMapper.selectById(chefId);
        if (chef == null) {
            return;
        }
        chef.setCompletedOrders((chef.getCompletedOrders() == null ? 0L : chef.getCompletedOrders()) + 1);
        chefMapper.updateById(chef);
    }

    private void recordMessage(String type, String receiverType, Long receiverId, DcCookOrder order, String summary) {
        DcCookMessage message = new DcCookMessage();
        message.setMessageType(type);
        message.setChannel("IN_APP");
        message.setReceiverType(receiverType);
        message.setReceiverId(receiverId);
        message.setRelatedOrderId(order.getOrderId());
        message.setRelatedOrderNo(order.getOrderNo());
        message.setRelatedBizType("ORDER");
        message.setRelatedBizId(order.getOrderId());
        message.setContentSummary(summary);
        message.setSendStatus(DcCookMessageStatus.SENT);
        message.setSendTime(new Date());
        messageMapper.insert(message);
    }

    private void hydrateDisplayNames(List<DcCookOrderVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> userIds = records.stream()
            .map(DcCookOrderVo::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() : userMapper.selectList(Wrappers.lambdaQuery(SysUser.class)
                .in(SysUser::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(SysUser::getUserId, user -> user, (left, right) -> left));
        List<Long> chefIds = records.stream()
            .map(DcCookOrderVo::getChefId)
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

    private DcCookOrderVo normalizeReadStatus(DcCookOrderVo vo) {
        if (vo != null) {
            vo.setStatus(DcCookOrderStatus.normalize(vo.getStatus()));
        }
        return vo;
    }

    private record MonthRange(Date start, Date end) {
    }
}
