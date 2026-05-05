package org.dromara.system.domain.vo.cooking;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.cooking.DcCookAddress;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcCookAddress.class)
public class DcCookAddressVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "Address ID")
    private Long addressId;

    @ExcelProperty(value = "User ID")
    private Long userId;

    private String userName;

    private String nickName;

    @ExcelProperty(value = "Contact")
    private String contactName;

    @ExcelProperty(value = "Phone")
    private String contactPhone;

    private String areaCode;

    @ExcelProperty(value = "Area")
    private String areaName;

    @ExcelProperty(value = "Detail Address")
    private String detailAddress;

    private String houseNumber;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @ExcelProperty(value = "Default")
    private String defaultFlag;

    private Long sourceAddressId;

    private String snapshotType;

    private Date snapshotTime;

    private String remark;

    private Date createTime;
}
