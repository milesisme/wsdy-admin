package com.wsdy.saasops.modules.system.msgtemple.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;



@Setter
@Getter
@ApiModel(value = "MsgModel", description = "信息模板")
@Table(name = "set_basic_msgmodel")
public class MsgModel implements Serializable{
private static final long serialVersionUID=1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "信息类型")
    private String name;
    @ApiModelProperty(value = "信息类型 set_basic_msgModelType id")
    private Integer msgType;
    @ApiModelProperty(value = "站内信")
    private String inMail;
    @ApiModelProperty(value = "邮件")
    private String email;
    @ApiModelProperty(value = "短信")
    private String phoneMail;
    @ApiModelProperty(value = "状态 1启用 2停用")
    private Integer state;
    @ApiModelProperty(value = "创建者")
    private String creater;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "是否是发送站内信 1 开启 0不开启")
    private Integer inMailDef ;
    @ApiModelProperty(value = "是否是发送邮件 1 开启 0不开启")
    private Integer emailDef ;
    @ApiModelProperty(value = "是否是发送短信 1 开启 0不开启")
    private Integer phoneMailDef;
    @ApiModelProperty(value = "最后一次更新时间")
    private String modifyTime;

    @Transient
    @ApiModelProperty(value = "状态 查询条件")
    private List<Integer> states;
    @Transient
    private String msgName;
    @Transient
    private String ids;
    @Transient
    private List<Integer> msgTypes;

    public static Integer getStateByStates(String states){
        if(states.length() ==1){
            return Integer.parseInt(states);
        }
        return null;
    }


    @Override
    public String toString() {
        return "MsgModel{" +
                "id=" + id +'\'' +
                ", name='" + name + '\'' +
                ", msgType=" + msgType +
                ", inMail='" + inMail + '\'' +
                ", email='" + email + '\'' +
                ", phoneMail='" + phoneMail + '\'' +
                ", state=" + state +'\'' +
                ", creater='" + creater + '\'' +
                ", createTime=" + createTime +'\'' +
                ", inMailDef=" + inMailDef +'\'' +
                ", emailDef=" + emailDef +'\'' +
                ", phoneMailDef=" + phoneMailDef +'\'' +
                ", modifyTime=" + modifyTime +'\'' +
                ", states='" + states + '\'' +
                ", msgName='" + msgName + '\'' +
                '}';
    }
}