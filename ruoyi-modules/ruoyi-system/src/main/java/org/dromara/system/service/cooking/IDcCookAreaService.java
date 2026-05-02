package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookAreaBo;
import org.dromara.system.domain.vo.cooking.DcCookAreaVo;

import java.util.Collection;
import java.util.List;

public interface IDcCookAreaService {

    DcCookAreaVo queryById(Long areaId);

    TableDataInfo<DcCookAreaVo> queryPageList(DcCookAreaBo bo, PageQuery pageQuery);

    List<DcCookAreaVo> queryList(DcCookAreaBo bo);

    Boolean insertByBo(DcCookAreaBo bo);

    Boolean updateByBo(DcCookAreaBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
