package org.dromara.system.domain.cooking;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dc_cook_address")
public class DcCookAddress extends TenantEntity {

    @TableId(value = "address_id")
    private Long addressId;

    private Long userId;

    private String contactName;

    private String contactPhone;

    private String areaCode;

    private String areaName;

    private String detailAddress;

    private String houseNumber;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String defaultFlag;

    private Long sourceAddressId;

    private String snapshotType;

    private Date snapshotTime;

    private String remark;

    @TableLogic
    private String delFlag;
}
