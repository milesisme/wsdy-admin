package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "OprActCatActivity", description = "OprActCatActivity")
@Table(name = "opr_act_catActivity")
public class OprActCatActivity implements Serializable {

    @ApiModelProperty(value = "activityId")
    private Integer activityId;

    @ApiModelProperty(value = "catId")
    private Integer catId;
}