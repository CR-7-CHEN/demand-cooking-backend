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

    private final DcCookSettlementMapper baseMapper;
    private final DcCookOrderMapper orderMapper;
    private final DcCookChefMapper chefMapper;
    private final IDcCookConfigService configService;

    @Override
    public DcCookSettlementVo queryById(Long settlementId) {
        return baseMapper.selectVoById(settlementId);
    }

    @Override
    public TableDataInfo<DcCookSettlementVo> queryPageList(DcCookSettlementBo bo, PageQuery pageQuery) {
        Page<DcCookSettlementVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public DcCookSettlementVo generateMonth(DcCookSettlementBo bo) {
        if (bo.getChefId() == null) {
            throw new ServiceException("chefId is required");
        }
        String month = StringUtils.isBlank(bo.getSettlementMonth())
            ? new SimpleDateFormat("yyyy-MM").format(new Date()) : bo.getSettlementMonth();
        DcCookChef chef = chefMapper.selectById(bo.getChefId());
        if (chef == null) {
            throw new ServiceException("chef not found");
        }
        boolean exists = baseMapper.exists(Wrappers.lambdaQuery(DcCookSettlement.class)
            .eq(DcCookSettlement::getChefId, bo.getChefId())
            .eq(DcCookSettlement::getSettlementMonth, month));
        if (exists) {
            throw new ServiceException("settlement already exists");
        }
        List<DcCookOrder> orders = orderMapper.selectList(buildCompletedOrderMonthWrapper(bo.getChefId(), month));
        BigDecimal orderAmount = orders.stream()
            .map(item -> item.getPayAmount() == null ? BigDecimal.ZERO : item.getPayAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal platformRate = getDecimalConfig("dc.cooking.platform.rate", DEFAULT_PLATFORM_RATE);
        BigDecimal chefRate = BigDecimal.ONE.subtract(platformRate);
        BigDecimal platformCommission = orderAmount.multiply(platformRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal chefCommission = orderAmount.multiply(chefRate).setScale(2, RoundingMode.HALF_UP);
        int violationCount = countChefCancel(bo.getChefId(), month);
        BigDecimal violationDeduction = violationCount >= 5 && chefCommission.compareTo(new BigDecimal("200")) >= 0
            ? new BigDecimal("200") : BigDecimal.ZERO;
        BigDecimal finalCommission = chefCommission.subtract(violationDeduction).max(BigDecimal.ZERO);
        BigDecimal baseSalary = chef.getBaseSalary() == null ? BigDecimal.ZERO : chef.getBaseSalary();

        DcCookSettlement settlement = new DcCookSettlement();
        settlement.setChefId(bo.getChefId());
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
        settlement.setPayableAmount(baseSalary.add(finalCommission));
        settlement.setStatus(StringUtils.isBlank(bo.getStatus()) ? DcCookSettlementStatus.GENERATED : bo.getStatus());
        settlement.setManualFlag(StringUtils.isBlank(bo.getManualFlag()) ? "N" : bo.getManualFlag());
        settlement.setGeneratedTime(new Date());
        baseMapper.insert(settlement);
        return baseMapper.selectVoById(settlement.getSettlementId());
    }

    private LambdaQueryWrapper<DcCookSettlement> buildQueryWrapper(DcCookSettlementBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookSettlement> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getSettlementId() != null, DcCookSettlement::getSettlementId, bo.getSettlementId());
        lqw.eq(bo.getChefId() != null, DcCookSettlement::getChefId, bo.getChefId());
        lqw.eq(StringUtils.isNotBlank(bo.getSettlementMonth()), DcCookSettlement::getSettlementMonth, bo.getSettlementMonth());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), DcCookSettlement::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookSettlement::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookSettlement::getSettlementMonth).orderByDesc(DcCookSettlement::getCreateTime);
        return lqw;
    }

    private int countChefCancel(Long chefId, String month) {
        Long count = orderMapper.selectCount(buildChefCancelMonthWrapper(chefId, month));
        return count.intValue();
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

    private record MonthRange(Date start, Date end) {
    }
}
