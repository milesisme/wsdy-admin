package com.wsdy.saasops.api.modules.transfer.dto;

import com.wsdy.saasops.modules.member.dto.DepotFailDto;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DepotFailDtosDto {

	@ApiModelProperty(value="会员ID")
    private Integer userId;

    @ApiModelProperty(value="会员名称")
    private String loginName;

    @ApiModelProperty(value="IP")
    private String ip;

    @ApiModelProperty(value="客户端")
    private String dev;

    @ApiModelProperty(value="客户端")
    private Byte transferSource;

    @ApiModelProperty(value="siteCode")
    private String siteCode;

    @ApiModelProperty(value="回收平台账号信息")
    private List<MbrDepotWallet> depotWallets;

    @ApiModelProperty(value="返回的信息")
    private List<DepotFailDto> recoverBalanceList;
}
