package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookAddressBo;
import org.dromara.system.domain.vo.cooking.DcCookAddressVo;
import org.dromara.system.service.cooking.IDcCookAddressService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class DcCookAddressController {

    private final IDcCookAddressService addressService;

    @GetMapping("/cooking/address/list")
    public TableDataInfo<DcCookAddressVo> list(DcCookAddressBo bo, PageQuery pageQuery) {
        return addressService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/cooking/address/{addressId}")
    public R<DcCookAddressVo> getInfo(@PathVariable Long addressId) {
        return R.ok(addressService.queryById(addressId));
    }

    @PostMapping("/cooking/address")
    public R<Void> add(@RequestBody DcCookAddressBo bo) {
        return addressService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/address")
    public R<Void> edit(@RequestBody DcCookAddressBo bo) {
        return addressService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/address/{addressIds}")
    public R<Void> remove(@PathVariable Long[] addressIds) {
        return addressService.deleteWithValidByIds(Arrays.asList(addressIds), true) ? R.ok() : R.fail();
    }

    @GetMapping("/cooking/app/address")
    public R<List<DcCookAddressVo>> appList() {
        return R.ok(addressService.queryByUserId(LoginHelper.getUserId()));
    }

    @PostMapping("/cooking/app/address")
    public R<Void> appAdd(@RequestBody DcCookAddressBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        return addressService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/app/address")
    public R<Void> appEdit(@RequestBody DcCookAddressBo bo) {
        Long userId = LoginHelper.getUserId();
        assertAddressOwner(bo.getAddressId(), userId);
        bo.setUserId(userId);
        return addressService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/cooking/app/address/default/{addressId}")
    public R<Void> setDefault(@PathVariable Long addressId) {
        Long userId = LoginHelper.getUserId();
        assertAddressOwner(addressId, userId);
        return addressService.setDefault(addressId, userId) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/app/address/{addressIds}")
    public R<Void> appRemove(@PathVariable Long[] addressIds) {
        return removeAppAddresses(addressIds);
    }

    @DeleteMapping("/cooking/app/address")
    public R<Void> appRemove(@RequestBody Map<String, Long> body) {
        Long addressId = body == null ? null : body.get("addressId");
        if (addressId == null) {
            addressId = body == null ? null : body.get("id");
        }
        return removeAppAddresses(new Long[]{addressId});
    }

    private R<Void> removeAppAddresses(Long[] addressIds) {
        Long userId = LoginHelper.getUserId();
        for (Long addressId : addressIds) {
            assertAddressOwner(addressId, userId);
        }
        return addressService.deleteWithValidByIds(Arrays.asList(addressIds), true) ? R.ok() : R.fail();
    }

    private void assertAddressOwner(Long addressId, Long userId) {
        if (addressId == null) {
            throw new ServiceException("addressId is required");
        }
        DcCookAddressVo address = addressService.queryById(addressId);
        if (address == null || !DcCookPermissionHelper.ownsOrder(userId, address.getUserId())) {
            throw new ServiceException("no permission to operate this address");
        }
    }
}
