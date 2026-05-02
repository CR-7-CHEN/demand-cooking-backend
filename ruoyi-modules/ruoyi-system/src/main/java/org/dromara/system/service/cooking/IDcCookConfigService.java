package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookConfigBo;
import org.dromara.system.domain.vo.cooking.DcCookConfigVo;

import java.util.Collection;
import java.util.List;

public interface IDcCookConfigService {

    DcCookConfigVo queryById(Long configId);

    String selectConfigValueByKey(String configKey);

    TableDataInfo<DcCookConfigVo> queryPageList(DcCookConfigBo bo, PageQuery pageQuery);

    List<DcCookConfigVo> queryList(DcCookConfigBo bo);

    Boolean insertByBo(DcCookConfigBo bo);

    Boolean updateByBo(DcCookConfigBo bo);

    Boolean publishCommissionNotice(Long configId);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    boolean checkConfigKeyUnique(DcCookConfigBo bo);
}
