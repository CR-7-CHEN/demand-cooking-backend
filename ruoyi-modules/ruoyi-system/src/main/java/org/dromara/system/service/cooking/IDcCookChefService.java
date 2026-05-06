package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.domain.vo.cooking.DcCookChefCommissionOrdersVo;
import org.dromara.system.domain.vo.cooking.DcCookChefWorkbenchVo;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;

import java.util.Collection;
import java.util.List;

public interface IDcCookChefService {

    DcCookChefVo queryById(Long chefId);

    DcCookChefVo queryDisplayById(Long chefId);

    DcCookChefVo queryByUserId(Long userId);

    DcCookChefWorkbenchVo queryWorkbench(Long userId);

    DcCookChefCommissionOrdersVo queryCommissionOrders(Long userId, String month);

    TableDataInfo<DcCookChefVo> queryPageList(DcCookChefBo bo, PageQuery pageQuery);

    TableDataInfo<DcCookChefVo> queryAppPageList(DcCookChefBo bo, PageQuery pageQuery);

    List<DcCookChefVo> queryList(DcCookChefBo bo);

    Boolean insertByBo(DcCookChefBo bo);

    Boolean updateByBo(DcCookChefBo bo);

    Boolean audit(DcCookChefBo bo);

    Boolean changeStatus(DcCookChefBo bo);

    Boolean pause(Long userId);

    Boolean resume(Long userId);

    Boolean resign(Long userId);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    boolean checkMobileUnique(DcCookChefBo bo);
}
