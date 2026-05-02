package org.dromara.system.controller.cooking;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.bo.cooking.DcCookAddressBo;
import org.dromara.system.domain.vo.cooking.DcCookAddressVo;
import org.dromara.system.service.cooking.IDcCookAddressService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

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
        bo.setUserId(LoginHelper.getUserId());
        return addressService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/cooking/app/address/default/{addressId}")
    public R<Void> setDefault(@PathVariable Long addressId) {
        return addressService.setDefault(addressId, LoginHelper.getUserId()) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/app/address/{addressIds}")
    public R<Void> appRemove(@PathVariable Long[] addressIds) {
        return addressService.deleteWithValidByIds(Arrays.asList(addressIds), true) ? R.ok() : R.fail();
    }
}
