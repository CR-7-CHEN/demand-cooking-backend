package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.cooking.DcCookAddressBo;
import org.dromara.system.domain.cooking.DcCookAddress;
import org.dromara.system.domain.vo.cooking.DcCookAddressVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookAddressMapper;
import org.dromara.system.service.cooking.IDcCookAddressService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DcCookAddressServiceImpl implements IDcCookAddressService {

    private static final String YES = "Y";
    private static final String NO = "N";

    private final DcCookAddressMapper baseMapper;
    private final SysUserMapper userMapper;

    @Override
    public DcCookAddressVo queryById(Long addressId) {
        DcCookAddressVo vo = baseMapper.selectVoById(addressId);
        if (vo != null) {
            hydrateUserInfo(List.of(vo));
        }
        return vo;
    }

    @Override
    public TableDataInfo<DcCookAddressVo> queryPageList(DcCookAddressBo bo, PageQuery pageQuery) {
        Page<DcCookAddressVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        hydrateUserInfo(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public List<DcCookAddressVo> queryList(DcCookAddressBo bo) {
        List<DcCookAddressVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        hydrateUserInfo(list);
        return list;
    }

    @Override
    public List<DcCookAddressVo> queryByUserId(Long userId) {
        List<DcCookAddressVo> list = baseMapper.selectVoList(Wrappers.lambdaQuery(DcCookAddress.class)
            .eq(DcCookAddress::getUserId, userId)
            .orderByDesc(DcCookAddress::getDefaultFlag)
            .orderByDesc(DcCookAddress::getCreateTime));
        hydrateUserInfo(list);
        return list;
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
        if (StringUtils.isNotBlank(bo.getUserKeyword())) {
            List<Long> userIds = resolveUserIds(bo.getUserKeyword());
            lqw.in(DcCookAddress::getUserId, userIds.isEmpty() ? List.of(-1L) : userIds);
        }
        lqw.like(StringUtils.isNotBlank(bo.getContactName()), DcCookAddress::getContactName, bo.getContactName());
        lqw.eq(StringUtils.isNotBlank(bo.getContactPhone()), DcCookAddress::getContactPhone, bo.getContactPhone());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), DcCookAddress::getAreaName, bo.getAreaName());
        lqw.eq(StringUtils.isNotBlank(bo.getDefaultFlag()), DcCookAddress::getDefaultFlag, bo.getDefaultFlag());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookAddress::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookAddress::getDefaultFlag).orderByDesc(DcCookAddress::getCreateTime);
        return lqw;
    }

    private List<Long> resolveUserIds(String keyword) {
        Long userId = parseLong(keyword);
        LambdaQueryWrapper<SysUser> lqw = Wrappers.lambdaQuery(SysUser.class)
            .and(wrapper -> wrapper.like(SysUser::getUserName, keyword).or().like(SysUser::getNickName, keyword));
        if (userId != null) {
            lqw.or().eq(SysUser::getUserId, userId);
        }
        return userMapper.selectList(lqw).stream().map(SysUser::getUserId).toList();
    }

    private void hydrateUserInfo(List<DcCookAddressVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> userIds = records.stream()
            .map(DcCookAddressVo::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, SysUser> userMap = userMapper.selectList(Wrappers.lambdaQuery(SysUser.class)
                .in(SysUser::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(SysUser::getUserId, user -> user, (left, right) -> left));
        records.forEach(record -> {
            SysUser user = userMap.get(record.getUserId());
            if (user != null) {
                record.setUserName(user.getUserName());
                record.setNickName(user.getNickName());
            }
        });
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
