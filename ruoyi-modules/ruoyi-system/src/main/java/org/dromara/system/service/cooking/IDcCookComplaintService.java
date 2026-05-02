package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookComplaintBo;
import org.dromara.system.domain.vo.cooking.DcCookComplaintVo;

/**
 * Cooking complaint service.
 */
public interface IDcCookComplaintService {

    DcCookComplaintVo queryById(Long complaintId);

    TableDataInfo<DcCookComplaintVo> queryPageList(DcCookComplaintBo bo, PageQuery pageQuery);

    Boolean submit(DcCookComplaintBo bo);

    Boolean handle(DcCookComplaintBo bo);
}
