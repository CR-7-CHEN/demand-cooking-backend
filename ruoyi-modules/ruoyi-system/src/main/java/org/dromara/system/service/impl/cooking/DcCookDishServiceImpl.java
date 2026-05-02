package org.dromara.system.service.impl.cooking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookDishBo;
import org.dromara.system.domain.cooking.DcCookDish;
import org.dromara.system.domain.vo.cooking.DcCookDishVo;
import org.dromara.system.mapper.cooking.DcCookDishMapper;
import org.dromara.system.service.cooking.IDcCookDishService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DcCookDishServiceImpl implements IDcCookDishService {

    private final DcCookDishMapper baseMapper;

    @Override
    public DcCookDishVo queryById(Long dishId) {
        return baseMapper.selectVoById(dishId);
    }

    @Override
    public TableDataInfo<DcCookDishVo> queryPageList(DcCookDishBo bo, PageQuery pageQuery) {
        Page<DcCookDishVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<DcCookDishVo> queryList(DcCookDishBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public List<DcCookDishVo> queryEnabledList(DcCookDishBo bo) {
        bo.setStatus("0");
        return queryList(bo);
    }

    @Override
    public Boolean insertByBo(DcCookDishBo bo) {
        DcCookDish add = MapstructUtils.convert(bo, DcCookDish.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus("0");
        }
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(DcCookDishBo bo) {
        DcCookDish update = MapstructUtils.convert(bo, DcCookDish.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    private LambdaQueryWrapper<DcCookDish> buildQueryWrapper(DcCookDishBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DcCookDish> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getDishId() != null, DcCookDish::getDishId, bo.getDishId());
        lqw.like(StringUtils.isNotBlank(bo.getDishName()), DcCookDish::getDishName, bo.getDishName());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), DcCookDish::getCategory, bo.getCategory());
        lqw.like(StringUtils.isNotBlank(bo.getCuisine()), DcCookDish::getCuisine, bo.getCuisine());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), DcCookDish::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            DcCookDish::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(DcCookDish::getSort).orderByDesc(DcCookDish::getCreateTime);
        return lqw;
    }
}
