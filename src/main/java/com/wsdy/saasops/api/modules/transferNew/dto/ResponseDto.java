package com.wsdy.saasops.api.modules.transferNew.dto;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResponseDto {

    @ApiModelProperty(value = "是否成功")
    private Boolean isSucceed = Boolean.TRUE;

    @ApiModelProperty(value = "错误信息")
    private String error;

    private MbrDepotWallet depotWallet;

    private TGmApi gmApi;

    private MbrBillManage mbrBillManage;
}
