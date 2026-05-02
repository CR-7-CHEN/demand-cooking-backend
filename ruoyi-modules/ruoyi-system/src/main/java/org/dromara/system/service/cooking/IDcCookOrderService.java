package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookOrderActionBo;
import org.dromara.system.domain.bo.cooking.DcCookOrderBo;
import org.dromara.system.domain.vo.cooking.DcCookOrderCancelPreviewVo;
import org.dromara.system.domain.vo.cooking.DcCookOrderVo;

/**
 * Cooking order service.
 */
public interface IDcCookOrderService extends IDcCookOrderStatusService {

    DcCookOrderVo queryById(Long orderId);

    TableDataInfo<DcCookOrderVo> queryPageList(DcCookOrderBo bo, PageQuery pageQuery);

    DcCookOrderVo submit(DcCookOrderBo bo);

    Boolean quote(DcCookOrderActionBo bo);

    Boolean reject(DcCookOrderActionBo bo);

    Boolean objection(DcCookOrderActionBo bo);

    Boolean paySuccess(DcCookOrderActionBo bo);

    Boolean serviceComplete(DcCookOrderActionBo bo);

    Boolean confirm(DcCookOrderActionBo bo);

    DcCookOrderCancelPreviewVo previewUserCancel(Long orderId);

    Boolean userCancel(DcCookOrderActionBo bo);

    Boolean chefCancel(DcCookOrderActionBo bo);
}
