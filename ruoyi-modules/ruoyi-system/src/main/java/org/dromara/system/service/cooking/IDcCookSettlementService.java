package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookSettlementBo;
import org.dromara.system.domain.vo.cooking.DcCookSettlementVo;

/**
 * Cooking settlement service.
 */
public interface IDcCookSettlementService {

    DcCookSettlementVo queryById(Long settlementId);

    TableDataInfo<DcCookSettlementVo> queryPageList(DcCookSettlementBo bo, PageQuery pageQuery);

    DcCookSettlementVo generateMonth(DcCookSettlementBo bo);

    Boolean applyReview(DcCookSettlementBo bo);

    Boolean handleReview(DcCookSettlementBo bo);

    Boolean confirm(DcCookSettlementBo bo);

    Boolean pay(DcCookSettlementBo bo);
}
