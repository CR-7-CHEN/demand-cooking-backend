package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookReviewBo;
import org.dromara.system.domain.vo.cooking.DcCookReviewVo;

/**
 * Cooking review service.
 */
public interface IDcCookReviewService {

    DcCookReviewVo queryById(Long reviewId);

    TableDataInfo<DcCookReviewVo> queryPageList(DcCookReviewBo bo, PageQuery pageQuery);

    Boolean submit(DcCookReviewBo bo);

    Boolean toggleDisplayStatus(Long reviewId);
}
