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
@ApiModel(value = "OprMsgtmpl", description = "运营管理-站内信消息模板")
@Table(name = "opr_msgtmpl")
public class OprMsgtmpl implements Serializable{
private static final long serialVersionUID=1L;
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@ApiModelProperty(value = "id")
private Integer id;

@ApiModelProperty(value = "模板名称")
private String tmplName;

@ApiModelProperty(value = "模板内容")
private String tmplContent;

}