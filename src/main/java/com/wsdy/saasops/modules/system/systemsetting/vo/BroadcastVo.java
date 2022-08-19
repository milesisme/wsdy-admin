package com.wsdy.saasops.modules.system.systemsetting.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@Data
public class BroadcastVo implements Serializable {
    private static final long serialVersionUID = 8244737699378595042L;

    @ApiModelProperty("关键字")
    @NotBlank(message = "关键字不能为空")
    String key;

    @ApiModelProperty("状态值：0，关闭；1，开启")
    Integer status;
}
