package com.wsdy.saasops.api.modules.unity.dto;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PlayGameModel {

    private Integer depotId;
    private String depotName;
    private String siteCode;
    private String userName;
    private String gameId;
    private String gameType;
    private String gamecode;
    private String password;
    private TGmApi tGmApi;
    private String timeStamp;
    /**设备类型：PC、H5、APP**/
    private String origin;
    private String ip;
    private String domain;
    @ApiModelProperty(value = "平台代码")
    private String depotCode;
}
