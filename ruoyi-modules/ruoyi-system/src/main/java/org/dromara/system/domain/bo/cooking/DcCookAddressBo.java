package org.dromara.system.domain.bo.cooking;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.cooking.DcCookAddress;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcCookAddress.class, reverseConvertGenerate = false)
public class DcCookAddressBo extends BaseEntity {

    private Long addressId;

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "contactName is required")
    @Size(max = 64, message = "contactName max length is 64")
    private String contactName;

    @NotBlank(message = "contactPhone is required")
    @Size(max = 20, message = "contactPhone max length is 20")
    private String contactPhone;

    private String areaCode;

    @NotBlank(message = "areaName is required")
    private String areaName;

    @NotBlank(message = "detailAddress is required")
    private String detailAddress;

    private String houseNumber;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String defaultFlag;

    private Long sourceAddressId;

    private String snapshotType;

    private Date snapshotTime;

    private String remark;
}
