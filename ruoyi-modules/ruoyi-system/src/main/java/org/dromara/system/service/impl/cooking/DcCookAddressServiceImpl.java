package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookAddressBo;
import org.dromara.system.domain.cooking.DcCookAddress;
import org.dromara.system.domain.vo.cooking.DcCookAddressVo;
import org.dromara.system.mapper.cooking.DcCookAddressMapper;
import org.dromara.system.service.cooking.IDcCookAddressService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookAddressServiceImpl implements IDcCookAddressService {

    private static final String YES = "Y";
    private static final String NO = "N";

    private final DcCookAddressMapper baseMapper;

    @Override
    public DcCookAddressVo queryById(Long addressId) {
        return baseMapper.selectVoById(addressId);
    }

    @Override
    public TableDataInfo<DcCookAddressVo> queryPageList(DcCookAddressBo bo, PageQuery pageQuery) {
        Page<DcCookAddressVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<DcCookAddressVo> queryList(DcCookAddressBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public List<DcCookAddressVo> queryByUserId(Long userId) {
        return baseMapper.selectVoList(Wrappers.lambdaQuery(DcCookAddress.class)
            .eq(DcCookAddress::getUserId, userId)
            .orderByDesc(DcCookAddress::getDefaultFlag)
            .orderByDesc(DcCookAddress::getCreateTime));
    }

    @Override
    public Boolean insertByBo(DcCookAddressBo bo) {
        DcCookAddress add = MapstructUtils.convert(bo, DcCookAddress.class);
        if (StringUtils.isBlank(add.getDefaultFlag())) {
            add.setDefaultFlag(NO);
        }
        boolean inserted = baseMapper.insert(add) > 0;
        if (inserted && YES.equals(add.getDefaultFlag())) {
            setDefault(add.getAddressId(), add.getUserId());
        }
        return inserted;
    }

    @Override
    public Boolean updateByBo(DcCookAddressBo bo) {
        DcCookAddress update = MapstructUtils.convert(bo, DcCookAddress.class);
        boolean updated = baseMapper.updateById(update) > 0;
        if (updated && YES.equals(bo.getDefaultFlag())) {
            setDefault(bo.getAddressId(), bo.getUserId());
        }
        return updated;
    }

    @Override
    public Boolean setDefault(Long addressId, Long userId) {
        DcCookAddress reset = new DcCookAddress();
        reset.setDefaultFlag(NO);
        baseMapper.update(reset, Wrappers.lambdaUpdate(DcCookAddress.class)
            .eq(DcCookAddress::getUserId, userId));
        DcCookAddress update = new DcCookAddress();
        update.setAddressId(addressId);
        update.setDefaultFlag(YES);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    private LambdaQueryWrapper<DcCookAddress> buildQueryWrapper(DcCookAddressBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookAddress> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getAddressId() != null, DcCookAddress::getAddressId, bo.getAddressId());
        lqw.eq(bo.getUserId() != null, DcCookAddress::getUserId, bo.getUserId());
        lqw.like(StringUtils.isNotBlank(bo.getContactName()), DcCookAddress::getContactName, bo.getContactName());
        lqw.eq(StringUtils.isNotBlank(bo.getContactPhone()), DcCookAddress::getContactPhone, bo.getContactPhone());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), DcCookAddress::getAreaName, bo.getAreaName());
        lqw.eq(StringUtils.isNotBlank(bo.getDefaultFlag()), DcCookAddress::getDefaultFlag, bo.getDefaultFlag());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookAddress::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookAddress::getDefaultFlag).orderByDesc(DcCookAddress::getCreateTime);
        return lqw;
    }
}
