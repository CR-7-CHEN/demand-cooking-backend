package org.dromara.system.service.cooking;

import org.dromara.system.domain.bo.cooking.DcCookChefTimeBo;
import org.dromara.system.domain.vo.cooking.DcCookChefTimeVo;

import java.util.List;

public interface IDcCookChefTimeService {

    DcCookChefTimeVo queryById(Long timeId);

    List<DcCookChefTimeVo> queryList(DcCookChefTimeBo bo);

    Boolean insertByBo(DcCookChefTimeBo bo);

    Boolean updateByBo(DcCookChefTimeBo bo);

    Boolean deleteById(Long timeId, Long chefId);
}
