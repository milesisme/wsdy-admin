package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@ApiModel(value = "AuditCat", description = "流水范围")
public class AuditCat {

    /*@ApiModelProperty(value = "是否全部 true是 false否")
    private Boolean isAll;*/

    @ApiModelProperty(value = "分类")
    private Integer catId;

    @ApiModelProperty(value = "平台")
    private List<AuditDepot> depots;
}
