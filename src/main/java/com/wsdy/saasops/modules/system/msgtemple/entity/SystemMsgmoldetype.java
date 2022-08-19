package com.wsdy.saasops.modules.system.msgtemple.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;



@Setter
@Getter
@ApiModel(value = "SystemMsgmoldetype", description = "消息模板类型")
@Table(name = "set_basic_msgmodeltype")
public class SystemMsgmoldetype implements Serializable{
    private static final long serialVersionUID=1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "名称")
    private String name;
}