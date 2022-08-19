package com.wsdy.saasops.modules.operate.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Setter
@Getter
@ApiModel(value = "OprRec", description = "")
@Table(name = "opr_msgRec")
public class OprRec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "消息id")
	private Integer msgId;

	@ApiModelProperty(value = "代理")
	private Integer agtId;

	@ApiModelProperty(value = "总代id")
	private Integer genAgtId;

	@ApiModelProperty(value = "会员id")
	private Integer mbrId;

}