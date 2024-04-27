package com.itheima.mp.domain.vo;

import com.itheima.mp.domain.po.UserInfo;
import com.itheima.mp.enums.UserStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "用户VO实体")
public class UserVO {

    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("详细信息")
    private UserInfo info;

    @ApiModelProperty("使用状态（1正常 2冻结）")
    private UserStatus status;

    @ApiModelProperty("账户余额")
    private Integer balance;


    @ApiModelProperty("用户收货地址")
    private List<AddressVO> addressVOList;

    public void setAddresses(List<AddressVO> addressVOList) {
        // 在这里将收货地址列表设置到用户VO中
        // 例如，可以直接将地址列表赋值给用户VO中的地址属性
        this.addressVOList = addressVOList;
    }

}