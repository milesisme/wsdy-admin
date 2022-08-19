package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "temp_AccountId", description = "")
@Table(name = "temp_AccountId")
public class TempAccountId implements Serializable {
	@ApiModelProperty(value = "会员ID")
	private Integer accountId;
	
	@ApiModelProperty(value = "UUid")
	private Long accUuid;
}