package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookDishBo;
import org.dromara.system.domain.vo.cooking.DcCookDishVo;

import java.util.Collection;
import java.util.List;

public interface IDcCookDishService {

    DcCookDishVo queryById(Long dishId);

    TableDataInfo<DcCookDishVo> queryPageList(DcCookDishBo bo, PageQuery pageQuery);

    List<DcCookDishVo> queryList(DcCookDishBo bo);

    List<DcCookDishVo> queryEnabledList(DcCookDishBo bo);

    Boolean insertByBo(DcCookDishBo bo);

    Boolean updateByBo(DcCookDishBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
