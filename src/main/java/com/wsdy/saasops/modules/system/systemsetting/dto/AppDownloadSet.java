package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppDownloadSet {
    @ApiModelProperty("安卓下载链接")
    private String androidDownloadUrl;
    @ApiModelProperty("IOS下载链接")
    private String iosDownloadUrl;
}
