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
import org.dromara.system.domain.bo.cooking.DcCookOrderActionBo;
import org.dromara.system.domain.bo.cooking.DcCookOrderBo;
import org.dromara.system.domain.cooking.DcCookAddress;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookMessage;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.vo.cooking.DcCookOrderCancelPreviewVo;
import org.dromara.system.domain.vo.cooking.DcCookOrderVo;
import org.dromara.system.mapper.cooking.DcCookAddressMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
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
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookOrderServiceImpl implements IDcCookOrderService {

    private static final BigDecimal MIN_QUOTE_AMOUNT = new BigDecimal("50");
    private static final BigDecimal DEFAULT_CANCEL_FEE_RATE = new BigDecimal("0.20");
    private static final int DEFAULT_PAY_MINUTES = 30;

    private final DcCookOrderMapper baseMapper;
    private final DcCookChefMapper chefMapper;
    private final DcCookAddressMapper addressMapper;
    private final DcCookMessageMapper messageMapper;
    private final IDcCookConfigService configService;

    @Override
    public DcCookOrderVo queryById(Long orderId) {
        return baseMapper.selectVoById(orderId);
    }

    @Override
    public TableDataInfo<DcCookOrderVo> queryPageList(DcCookOrderBo bo, PageQuery pageQuery) {
        Page<DcCookOrderVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Transactional(rollbackFor = Exception.class)
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
        DcCookOrder order = MapstructUtils.convert(bo, DcCookOrder.class);
        order.setOrderNo(generateOrderNo());
        order.setStatus(DcCookOrderStatus.WAITING_RESPONSE);
        order.setServiceEndTime(addHours(bo.getServiceStartTime(), 3));
        order.setQuoteUpdateCount(0);
        order.setObjectionCount(0);
        fillAddressSnapshot(order);
        baseMapper.insert(order);
        recordMessage("ORDER_SUBMIT", "CHEF", order.getChefId(), order, "New cooking order submitted");
        return baseMapper.selectVoById(order.getOrderId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean quote(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        if (!DcCookOrderStatus.WAITING_RESPONSE.equals(order.getStatus())
            && !DcCookOrderStatus.PRICE_OBJECTION.equals(order.getStatus())) {
            throw new ServiceException("order cannot be quoted now");
        }
        if (bo.getQuoteAmount() == null || bo.getQuoteAmount().compareTo(MIN_QUOTE_AMOUNT) < 0) {
            throw new ServiceException("quoteAmount must be at least 50");
        }
        if (DcCookOrderStatus.WAITING_RESPONSE.equals(order.getStatus())) {
            order.setPayDeadline(addMinutes(new Date(), getIntConfig("cooking.pay.timeout.minutes", DEFAULT_PAY_MINUTES)));
        } else if (order.getQuoteUpdateCount() != null && order.getQuoteUpdateCount() >= 1) {
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
        if (!DcCookOrderStatus.WAITING_PAY.equals(order.getStatus())
            && !DcCookOrderStatus.PRICE_OBJECTION.equals(order.getStatus())) {
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
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("PAY_SUCCESS", "CHEF", order.getChefId(), order, "Mock payment success");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean serviceComplete(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        assertStatus(order, DcCookOrderStatus.WAITING_SERVICE);
        order.setStatus(DcCookOrderStatus.WAITING_CONFIRM);
        order.setServiceCompleteTime(new Date());
        order.setServiceCompleteType(DcCookOrderStatus.COMPLETE_BY_CHEF);
        boolean ok = baseMapper.updateById(order) > 0;
        recordMessage("SERVICE_COMPLETE", "USER", order.getUserId(), order, "Chef marked service complete");
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean confirm(DcCookOrderActionBo bo) {
        DcCookOrder order = requireOrder(bo.getOrderId());
        assertStatus(order, DcCookOrderStatus.WAITING_CONFIRM);
        order.setStatus(DcCookOrderStatus.COMPLETED);
        order.setConfirmTime(new Date());
        order.setCompleteTime(new Date());
        boolean ok = baseMapper.updateById(order) > 0;
        incrementChefCompleted(order.getChefId());
        recordMessage("ORDER_CONFIRM", "CHEF", order.getChefId(), order, "User confirmed order complete");
        return ok;
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
        if (DcCookOrderStatus.UNPAID_CANCELABLE.contains(order.getStatus())) {
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
        lqw.eq(bo.getOrderId() != null, DcCookOrder::getOrderId, bo.getOrderId());
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), DcCookOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getUserId() != null, DcCookOrder::getUserId, bo.getUserId());
        lqw.eq(bo.getChefId() != null, DcCookOrder::getChefId, bo.getChefId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), DcCookOrder::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookOrder::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookOrder::getCreateTime);
        return lqw;
    }

    private DcCookOrder requireOrder(Long orderId) {
        DcCookOrder order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("order not found");
        }
        return order;
    }

    private void assertStatus(DcCookOrder order, String status) {
        if (!status.equals(order.getStatus())) {
            throw new ServiceException("invalid order status");
        }
    }

    private void assertChefCanTakeOrder(DcCookChef chef) {
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!"1".equals(chef.getAuditStatus()) || !"0".equals(chef.getChefStatus())) {
            throw new ServiceException("chef cannot take order");
        }
        if (chef.getHealthCertExpireDate() != null && chef.getHealthCertExpireDate().before(today)) {
            throw new ServiceException("chef health certificate expired");
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
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Long count = baseMapper.selectCount(Wrappers.lambdaQuery(DcCookOrder.class)
            .likeRight(DcCookOrder::getOrderNo, "OD" + day));
        return "OD" + day + String.format("%04d", count + 1);
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

    private boolean needCancelFee(DcCookOrder order) {
        if (order.getPayTime() == null) {
            return false;
        }
        return addMinutes(order.getPayTime(), 10).before(new Date());
    }

    private void assertPaidCancelable(DcCookOrder order) {
        if (!DcCookOrderStatus.WAITING_SERVICE.equals(order.getStatus())) {
            throw new ServiceException("paid order cannot be canceled now");
        }
        if (order.getServiceStartTime() != null && !order.getServiceStartTime().after(new Date())) {
            throw new ServiceException("service has already started");
        }
    }

    private int getIntConfig(String key, int defaultValue) {
        try {
            String value = configService.selectConfigValueByKey(key);
            return StringUtils.isBlank(value) ? defaultValue : Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
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
        message.setSendStatus("SENT");
        message.setSendTime(new Date());
        messageMapper.insert(message);
    }
}
