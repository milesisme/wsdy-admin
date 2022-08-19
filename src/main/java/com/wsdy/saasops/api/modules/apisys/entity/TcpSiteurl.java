package com.wsdy.saasops.api.modules.apisys.entity;

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
@ApiModel(value = "站点url信息表", description = "站点url信息表")
@Table(name = "t_cp_siteurl")
public class TcpSiteurl implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "站点Id")
	private Integer siteId;

	@ApiModelProperty(value = "站点代码")
	private String siteCode;

	@ApiModelProperty(value = "站点地址")
	private String siteUrl;

	@ApiModelProperty(value = "是否开启 1开启 0关闭")
	private Integer available;

	@ApiModelProperty(value = "客户端类型 1PC前台 2PC后台 3PC代理 4APP前台 5H5前台")
	private Integer clientType;

	@ApiModelProperty(value = "精准站点地址")
	private String preciseSiteUrl;

	@ApiModelProperty(value = "主域名id,不为空则是子域名")
	private Integer pid;

}