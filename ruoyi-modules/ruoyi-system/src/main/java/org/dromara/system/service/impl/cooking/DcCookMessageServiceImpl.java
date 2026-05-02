package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookMessageBo;
import org.dromara.system.domain.cooking.DcCookMessage;
import org.dromara.system.domain.vo.cooking.DcCookMessageVo;
import org.dromara.system.mapper.cooking.DcCookMessageMapper;
import org.dromara.system.service.cooking.IDcCookMessageService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookMessageServiceImpl implements IDcCookMessageService {

    private final DcCookMessageMapper baseMapper;

    @Override
    public DcCookMessageVo queryById(Long messageId) {
        return baseMapper.selectVoById(messageId);
    }

    @Override
    public TableDataInfo<DcCookMessageVo> queryPageList(DcCookMessageBo bo, PageQuery pageQuery) {
        Page<DcCookMessageVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<DcCookMessageVo> queryList(DcCookMessageBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public Boolean insertByBo(DcCookMessageBo bo) {
        DcCookMessage add = MapstructUtils.convert(bo, DcCookMessage.class);
        if (StringUtils.isBlank(add.getSendStatus())) {
            add.setSendStatus("SENT");
        }
        if (add.getSendTime() == null) {
            add.setSendTime(new Date());
        }
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(DcCookMessageBo bo) {
        DcCookMessage update = MapstructUtils.convert(bo, DcCookMessage.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    private LambdaQueryWrapper<DcCookMessage> buildQueryWrapper(DcCookMessageBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookMessage> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getMessageId() != null, DcCookMessage::getMessageId, bo.getMessageId());
        lqw.eq(StringUtils.isNotBlank(bo.getMessageType()), DcCookMessage::getMessageType, bo.getMessageType());
        lqw.eq(StringUtils.isNotBlank(bo.getChannel()), DcCookMessage::getChannel, bo.getChannel());
        lqw.eq(StringUtils.isNotBlank(bo.getReceiverType()), DcCookMessage::getReceiverType, bo.getReceiverType());
        lqw.eq(bo.getReceiverId() != null, DcCookMessage::getReceiverId, bo.getReceiverId());
        lqw.eq(bo.getRelatedOrderId() != null, DcCookMessage::getRelatedOrderId, bo.getRelatedOrderId());
        lqw.eq(StringUtils.isNotBlank(bo.getRelatedOrderNo()), DcCookMessage::getRelatedOrderNo, bo.getRelatedOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getSendStatus()), DcCookMessage::getSendStatus, bo.getSendStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookMessage::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookMessage::getCreateTime);
        return lqw;
    }
}
