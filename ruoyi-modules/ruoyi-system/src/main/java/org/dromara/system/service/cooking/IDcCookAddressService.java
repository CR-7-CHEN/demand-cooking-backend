package org.dromara.system.service.cooking;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.cooking.DcCookAddressBo;
import org.dromara.system.domain.vo.cooking.DcCookAddressVo;

import java.util.Collection;
import java.util.List;

public interface IDcCookAddressService {

    DcCookAddressVo queryById(Long addressId);

    TableDataInfo<DcCookAddressVo> queryPageList(DcCookAddressBo bo, PageQuery pageQuery);

    List<DcCookAddressVo> queryList(DcCookAddressBo bo);

    List<DcCookAddressVo> queryByUserId(Long userId);

    Boolean insertByBo(DcCookAddressBo bo);

    Boolean updateByBo(DcCookAddressBo bo);

    Boolean setDefault(Long addressId, Long userId);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
