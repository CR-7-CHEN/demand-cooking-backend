package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookMessageBo;
import org.dromara.system.domain.vo.cooking.DcCookMessageVo;

import java.util.Collection;
import java.util.List;

public interface IDcCookMessageService {

    DcCookMessageVo queryById(Long messageId);

    TableDataInfo<DcCookMessageVo> queryPageList(DcCookMessageBo bo, PageQuery pageQuery);

    List<DcCookMessageVo> queryList(DcCookMessageBo bo);

    Boolean insertByBo(DcCookMessageBo bo);

    Boolean updateByBo(DcCookMessageBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
