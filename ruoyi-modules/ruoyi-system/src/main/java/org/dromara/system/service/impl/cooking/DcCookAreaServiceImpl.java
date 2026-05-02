package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookAreaBo;
import org.dromara.system.domain.cooking.DcCookArea;
import org.dromara.system.domain.vo.cooking.DcCookAreaVo;
import org.dromara.system.mapper.cooking.DcCookAreaMapper;
import org.dromara.system.service.cooking.IDcCookAreaService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookAreaServiceImpl implements IDcCookAreaService {

    private final DcCookAreaMapper baseMapper;

    @Override
    public DcCookAreaVo queryById(Long areaId) {
        return baseMapper.selectVoById(areaId);
    }

    @Override
    public TableDataInfo<DcCookAreaVo> queryPageList(DcCookAreaBo bo, PageQuery pageQuery) {
        Page<DcCookAreaVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<DcCookAreaVo> queryList(DcCookAreaBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public Boolean insertByBo(DcCookAreaBo bo) {
        DcCookArea add = MapstructUtils.convert(bo, DcCookArea.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus("0");
        }
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(DcCookAreaBo bo) {
        DcCookArea update = MapstructUtils.convert(bo, DcCookArea.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    private LambdaQueryWrapper<DcCookArea> buildQueryWrapper(DcCookAreaBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookArea> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getAreaId() != null, DcCookArea::getAreaId, bo.getAreaId());
        lqw.like(StringUtils.isNotBlank(bo.getAreaCode()), DcCookArea::getAreaCode, bo.getAreaCode());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), DcCookArea::getAreaName, bo.getAreaName());
        lqw.eq(StringUtils.isNotBlank(bo.getParentCode()), DcCookArea::getParentCode, bo.getParentCode());
        lqw.eq(StringUtils.isNotBlank(bo.getAreaLevel()), DcCookArea::getAreaLevel, bo.getAreaLevel());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), DcCookArea::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookArea::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(DcCookArea::getSort).orderByDesc(DcCookArea::getCreateTime);
        return lqw;
    }
}
