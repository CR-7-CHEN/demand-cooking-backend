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
import org.dromara.system.domain.bo.cooking.DcCookConfigBo;
import org.dromara.system.domain.cooking.DcCookConfig;
import org.dromara.system.domain.vo.cooking.DcCookConfigVo;
import org.dromara.system.mapper.cooking.DcCookConfigMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookConfigServiceImpl implements IDcCookConfigService {

    private final DcCookConfigMapper baseMapper;

    @Override
    public DcCookConfigVo queryById(Long configId) {
        return baseMapper.selectVoById(configId);
    }

    @Override
    public String selectConfigValueByKey(String configKey) {
        DcCookConfig config = baseMapper.selectOne(Wrappers.lambdaQuery(DcCookConfig.class)
            .eq(DcCookConfig::getConfigKey, configKey)
            .last("limit 1"), false);
        return config == null ? "" : config.getConfigValue();
    }

    @Override
    public TableDataInfo<DcCookConfigVo> queryPageList(DcCookConfigBo bo, PageQuery pageQuery) {
        Page<DcCookConfigVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<DcCookConfigVo> queryList(DcCookConfigBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public Boolean insertByBo(DcCookConfigBo bo) {
        validateConfigType(bo);
        if (!checkConfigKeyUnique(bo)) {
            throw new ServiceException("configKey already exists");
        }
        DcCookConfig add = MapstructUtils.convert(bo, DcCookConfig.class);
        if (StringUtils.isBlank(add.getPublishStatus())) {
            add.setPublishStatus("0");
        }
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(DcCookConfigBo bo) {
        validateConfigType(bo);
        if (!checkConfigKeyUnique(bo)) {
            throw new ServiceException("configKey already exists");
        }
        DcCookConfig update = MapstructUtils.convert(bo, DcCookConfig.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean publishCommissionNotice(Long configId) {
        DcCookConfig update = new DcCookConfig();
        update.setConfigId(configId);
        update.setPublishStatus("1");
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public boolean checkConfigKeyUnique(DcCookConfigBo bo) {
        return !baseMapper.exists(Wrappers.lambdaQuery(DcCookConfig.class)
            .eq(DcCookConfig::getConfigKey, bo.getConfigKey())
            .ne(bo.getConfigId() != null, DcCookConfig::getConfigId, bo.getConfigId()));
    }

    private LambdaQueryWrapper<DcCookConfig> buildQueryWrapper(DcCookConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getConfigId() != null, DcCookConfig::getConfigId, bo.getConfigId());
        lqw.ne(DcCookConfig::getConfigType, "ANNOUNCEMENT");
        lqw.like(StringUtils.isNotBlank(bo.getConfigName()), DcCookConfig::getConfigName, bo.getConfigName());
        lqw.like(StringUtils.isNotBlank(bo.getConfigKey()), DcCookConfig::getConfigKey, bo.getConfigKey());
        lqw.eq(StringUtils.isNotBlank(bo.getConfigType()), DcCookConfig::getConfigType, bo.getConfigType());
        lqw.eq(StringUtils.isNotBlank(bo.getRuleFlag()), DcCookConfig::getRuleFlag, bo.getRuleFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getPublishStatus()), DcCookConfig::getPublishStatus, bo.getPublishStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookConfig::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(DcCookConfig::getCreateTime);
        return lqw;
    }

    private void validateConfigType(DcCookConfigBo bo) {
        if ("ANNOUNCEMENT".equalsIgnoreCase(bo.getConfigType())) {
            throw new ServiceException("announcement should be maintained in system notice");
        }
    }
}
