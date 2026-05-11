package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookSettlementBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookSettlement;
import org.dromara.system.domain.cooking.DcCookSettlementStatus;
import org.dromara.system.domain.vo.cooking.DcCookSettlementVo;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookSettlementMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.cooking.IDcCookSettlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookSettlementServiceImpl implements IDcCookSettlementService {

    private static final BigDecimal DEFAULT_PLATFORM_RATE = new BigDecimal("0.20");
    private static final BigDecimal VIOLATION_DEDUCTION = new BigDecimal("200");
    private static final String AUDIT_APPROVED = "1";

    private final DcCookSettlementMapper baseMapper;
    private final DcCookOrderMapper orderMapper;
    private final DcCookChefMapper chefMapper;
    private final IDcCookConfigService configService;

    @Override
    public DcCookSettlementVo queryById(Long settlementId) {
        return normalizeReadStatus(baseMapper.selectVoById(settlementId));
    }

    @Override
    public TableDataInfo<DcCookSettlementVo> queryPageList(DcCookSettlementBo bo, PageQuery pageQuery) {
        Page<DcCookSettlementVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        if (page.getRecords() != null) {
            page.getRecords().forEach(this::normalizeReadStatus);
        }
        return TableDataInfo.build(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DcCookSettlementVo generateMonth(DcCookSettlementBo bo) {
        Long chefId = requireSettlementChefId(bo);
        String month = resolveSettlementMonth(bo.getSettlementMonth());
        boolean exists = baseMapper.exists(Wrappers.lambdaQuery(DcCookSettlement.class)
            .eq(DcCookSettlement::getChefId, chefId)
            .eq(DcCookSettlement::getSettlementMonth, month));
        if (exists) {
            throw new ServiceException("settlement already exists");
        }
        DcCookSettlement settlement = new DcCookSettlement();
        settlement.setChefId(chefId);
        settlement.setSettlementMonth(month);
        settlement.setManualFlag(defaultManualFlag(bo.getManualFlag()));
        rebuildSettlement(settlement);
        settlement.setStatus(DcCookSettlementStatus.GENERATED);
        baseMapper.insert(settlement);
        return queryById(settlement.getSettlementId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int generatePreviousMonthSettlements() {
        return generateMonthlySettlements(YearMonth.now().minusMonths(1).toString());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int generateMonthlySettlements(String settlementMonth) {
        String month = resolveSettlementMonth(settlementMonth);
        List<DcCookChef> chefs = chefMapper.selectList(Wrappers.lambdaQuery(DcCookChef.class)
            .eq(DcCookChef::getAuditStatus, AUDIT_APPROVED));
        int generated = 0;
        for (DcCookChef chef : chefs) {
            if (chef == null || chef.getChefId() == null) {
                continue;
            }
            Long chefId = chef.getChefId();
            boolean exists = baseMapper.exists(Wrappers.lambdaQuery(DcCookSettlement.class)
                .eq(DcCookSettlement::getChefId, chefId)
                .eq(DcCookSettlement::getSettlementMonth, month));
            if (exists) {
                continue;
            }
            List<DcCookOrder> orders = orderMapper.selectList(buildCompletedOrderMonthWrapper(chefId, month));
            if (orders == null || orders.isEmpty()) {
                continue;
            }

            DcCookSettlement settlement = new DcCookSettlement();
            settlement.setChefId(chefId);
            settlement.setSettlementMonth(month);
            settlement.setManualFlag("N");
            rebuildSettlement(settlement, chef, orders);
            settlement.setStatus(DcCookSettlementStatus.GENERATED);
            if (baseMapper.insert(settlement) > 0) {
                generated++;
            }
        }
        return generated;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean applyReview(DcCookSettlementBo bo) {
        DcCookSettlement settlement = requireSettlement(bo.getSettlementId());
        ensureStatus(settlement, DcCookSettlementStatus.GENERATED, "only generated settlement can apply review");
        if (StringUtils.isBlank(bo.getReviewReasonType()) && StringUtils.isBlank(bo.getReviewRemark())) {
            throw new ServiceException("review reason is required");
        }
        settlement.setStatus(DcCookSettlementStatus.REVIEWING);
        settlement.setReviewReasonType(trimToNull(bo.getReviewReasonType()));
        settlement.setReviewRemark(trimToNull(bo.getReviewRemark()));
        settlement.setReviewResult(null);
        settlement.setReviewReply(null);
        settlement.setReviewApplyTime(new Date());
        settlement.setReviewHandleTime(null);
        settlement.setConfirmTime(null);
        settlement.setPayTime(null);
        settlement.setPayRemark(null);
        return baseMapper.updateById(settlement) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean handleReview(DcCookSettlementBo bo) {
        DcCookSettlement settlement = requireSettlement(bo.getSettlementId());
        ensureStatus(settlement, DcCookSettlementStatus.REVIEWING, "only reviewing settlement can be handled");
        String reviewResult = normalizeReviewResult(bo.getReviewResult());
        String reviewReply = trimToNull(bo.getReviewReply());
        if (reviewReply == null) {
            throw new ServiceException("reviewReply is required");
        }
        if (DcCookSettlementStatus.REVIEW_RESULT_REGENERATE.equals(reviewResult)) {
            rebuildSettlement(settlement);
        }
        settlement.setStatus(DcCookSettlementStatus.GENERATED);
        settlement.setReviewResult(reviewResult);
        settlement.setReviewReply(reviewReply);
        settlement.setReviewHandleTime(new Date());
        settlement.setConfirmTime(null);
        settlement.setPayTime(null);
        settlement.setPayRemark(null);
        return baseMapper.updateById(settlement) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean confirm(DcCookSettlementBo bo) {
        DcCookSettlement settlement = requireSettlement(bo.getSettlementId());
        ensureStatus(settlement, DcCookSettlementStatus.GENERATED, "only generated settlement can be confirmed");
        settlement.setStatus(DcCookSettlementStatus.CONFIRMED);
        settlement.setConfirmTime(new Date());
        return baseMapper.updateById(settlement) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean pay(DcCookSettlementBo bo) {
        DcCookSettlement settlement = requireSettlement(bo.getSettlementId());
        ensureStatus(settlement, DcCookSettlementStatus.CONFIRMED, "only confirmed settlement can be paid");
        settlement.setStatus(DcCookSettlementStatus.PAID);
        settlement.setPayTime(new Date());
        settlement.setPayRemark(trimToNull(bo.getPayRemark()));
        return baseMapper.updateById(settlement) > 0;
    }

    private void rebuildSettlement(DcCookSettlement settlement) {
        Long chefId = settlement.getChefId();
        if (chefId == null) {
            throw new ServiceException("chefId is required");
        }
        String month = resolveSettlementMonth(settlement.getSettlementMonth());
        DcCookChef chef = chefMapper.selectById(chefId);
        if (chef == null) {
            throw new ServiceException("chef not found");
        }

        List<DcCookOrder> orders = orderMapper.selectList(buildCompletedOrderMonthWrapper(chefId, month));
        rebuildSettlement(settlement, chef, orders);
    }

    private void rebuildSettlement(DcCookSettlement settlement, DcCookChef chef, List<DcCookOrder> orders) {
        Long chefId = settlement.getChefId();
        String month = resolveSettlementMonth(settlement.getSettlementMonth());
        BigDecimal orderAmount = sumOrderAmount(orders);
        BigDecimal platformRate = getDecimalConfig("dc.cooking.platform.rate", DEFAULT_PLATFORM_RATE);
        BigDecimal chefRate = BigDecimal.ONE.subtract(platformRate);
        BigDecimal platformCommission = orderAmount.multiply(platformRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal chefCommission = orderAmount.multiply(chefRate).setScale(2, RoundingMode.HALF_UP);
        int violationCount = countChefCancel(chefId, month);
        BigDecimal violationDeduction = violationCount >= 5 && chefCommission.compareTo(VIOLATION_DEDUCTION) >= 0
            ? VIOLATION_DEDUCTION : BigDecimal.ZERO;
        BigDecimal finalCommission = chefCommission.subtract(violationDeduction).max(BigDecimal.ZERO);
        BigDecimal baseSalary = chef.getBaseSalary() == null ? BigDecimal.ZERO : chef.getBaseSalary();

        settlement.setChefId(chefId);
        settlement.setSettlementMonth(month);
        settlement.setBaseSalary(baseSalary);
        settlement.setOrderCount(orders.size());
        settlement.setOrderAmount(orderAmount);
        settlement.setChefRate(chefRate);
        settlement.setChefCommission(chefCommission);
        settlement.setPlatformRate(platformRate);
        settlement.setPlatformCommission(platformCommission);
        settlement.setViolationCount(violationCount);
        settlement.setViolationDeduction(violationDeduction);
        settlement.setFinalCommission(finalCommission);
        settlement.setPayableAmount(baseSalary.add(chefCommission).subtract(violationDeduction));
        settlement.setGeneratedTime(new Date());
        settlement.setManualFlag(defaultManualFlag(settlement.getManualFlag()));
    }

    private LambdaQueryWrapper<DcCookSettlement> buildQueryWrapper(DcCookSettlementBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookSettlement> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getSettlementId() != null, DcCookSettlement::getSettlementId, bo.getSettlementId());
        lqw.eq(bo.getChefId() != null, DcCookSettlement::getChefId, bo.getChefId());
        lqw.eq(StringUtils.isNotBlank(bo.getSettlementMonth()), DcCookSettlement::getSettlementMonth, bo.getSettlementMonth());
        if (StringUtils.isNotBlank(bo.getStatus())) {
            if (DcCookSettlementStatus.PAID.equals(bo.getStatus())) {
                lqw.in(DcCookSettlement::getStatus, DcCookSettlementStatus.PAID, DcCookSettlementStatus.PAID_OFFLINE);
            } else {
                lqw.eq(DcCookSettlement::getStatus, bo.getStatus());
            }
        }
        lqw.between(params != null && params.get("beginTime") != null && params.get("endTime") != null,
            DcCookSettlement::getCreateTime, params == null ? null : params.get("beginTime"), params == null ? null : params.get("endTime"));
        lqw.orderByDesc(DcCookSettlement::getSettlementMonth).orderByDesc(DcCookSettlement::getCreateTime);
        return lqw;
    }

    private int countChefCancel(Long chefId, String month) {
        Long count = orderMapper.selectCount(buildChefCancelMonthWrapper(chefId, month));
        return count == null ? 0 : count.intValue();
    }

    private LambdaQueryWrapper<DcCookOrder> buildCompletedOrderMonthWrapper(Long chefId, String month) {
        MonthRange range = resolveMonthRange(month);
        return Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .eq(DcCookOrder::getStatus, DcCookOrderStatus.COMPLETED)
            .and(wrapper -> wrapper
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

    private LambdaQueryWrapper<DcCookOrder> buildChefCancelMonthWrapper(Long chefId, String month) {
        MonthRange range = resolveMonthRange(month);
        return Wrappers.lambdaQuery(DcCookOrder.class)
            .eq(DcCookOrder::getChefId, chefId)
            .eq(DcCookOrder::getCancelType, DcCookOrderStatus.CANCEL_CHEF)
            .isNotNull(DcCookOrder::getCancelTime)
            .ge(DcCookOrder::getCancelTime, range.start())
            .lt(DcCookOrder::getCancelTime, range.end());
    }

    private MonthRange resolveMonthRange(String month) {
        YearMonth yearMonth = parseMonth(month);
        return new MonthRange(
            toDate(yearMonth.atDay(1)),
            toDate(yearMonth.plusMonths(1).atDay(1))
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
            throw new ServiceException("invalid settlementMonth, expected yyyy-MM");
        }
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private BigDecimal getDecimalConfig(String key, BigDecimal defaultValue) {
        try {
            String value = configService.selectConfigValueByKey(key);
            return StringUtils.isBlank(value) ? defaultValue : new BigDecimal(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Long requireSettlementChefId(DcCookSettlementBo bo) {
        if (bo.getChefId() == null) {
            throw new ServiceException("chefId is required");
        }
        return bo.getChefId();
    }

    private DcCookSettlement requireSettlement(Long settlementId) {
        if (settlementId == null) {
            throw new ServiceException("settlementId is required");
        }
        DcCookSettlement settlement = baseMapper.selectById(settlementId);
        if (settlement == null) {
            throw new ServiceException("settlement not found");
        }
        settlement.setStatus(normalizeStatus(settlement.getStatus()));
        return settlement;
    }

    private void ensureStatus(DcCookSettlement settlement, String expectedStatus, String errorMessage) {
        String status = normalizeStatus(settlement.getStatus());
        if (!expectedStatus.equals(status)) {
            throw new ServiceException(errorMessage);
        }
    }

    private String resolveSettlementMonth(String settlementMonth) {
        if (StringUtils.isBlank(settlementMonth)) {
            return new SimpleDateFormat("yyyy-MM").format(new Date());
        }
        return parseMonth(settlementMonth).toString();
    }

    private BigDecimal sumOrderAmount(List<DcCookOrder> orders) {
        return orders.stream()
            .map(item -> item.getPayAmount() == null ? BigDecimal.ZERO : item.getPayAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String normalizeReviewResult(String reviewResult) {
        String normalized = trimToNull(reviewResult);
        if (normalized == null) {
            throw new ServiceException("reviewResult is required");
        }
        normalized = normalized.toUpperCase();
        if (!DcCookSettlementStatus.REVIEW_RESULT_KEEP.equals(normalized)
            && !DcCookSettlementStatus.REVIEW_RESULT_REGENERATE.equals(normalized)) {
            throw new ServiceException("reviewResult must be KEEP or REGENERATE");
        }
        return normalized;
    }

    private String defaultManualFlag(String manualFlag) {
        return StringUtils.isBlank(manualFlag) ? "N" : manualFlag;
    }

    private String trimToNull(String value) {
        String trimmed = StringUtils.trim(value);
        return StringUtils.isBlank(trimmed) ? null : trimmed;
    }

    private DcCookSettlementVo normalizeReadStatus(DcCookSettlementVo vo) {
        if (vo != null) {
            vo.setStatus(normalizeStatus(vo.getStatus()));
        }
        return vo;
    }

    private String normalizeStatus(String status) {
        String normalized = StringUtils.trim(status);
        if (StringUtils.isBlank(normalized)) {
            return DcCookSettlementStatus.GENERATED;
        }
        normalized = normalized.toUpperCase();
        if (DcCookSettlementStatus.PAID_OFFLINE.equals(normalized)) {
            return DcCookSettlementStatus.PAID;
        }
        if (DcCookSettlementStatus.MANUAL.equals(normalized)) {
            return DcCookSettlementStatus.GENERATED;
        }
        return normalized;
    }

    private record MonthRange(Date start, Date end) {
    }
}
