package com.wsdy.saasops.modules.operate.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "OprMes", description = "")
@Table(name = "opr_msg")
public class OprMes implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "站内信标题")
	private String title;

	@ApiModelProperty(value = "站内信内容")
	private String context;

	@ApiModelProperty(value = "")
	private String createTime;

	@ApiModelProperty(value = "发送人")
	private String sender;

}