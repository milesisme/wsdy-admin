package com.wsdy.saasops.modules.operate.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.entity.BaseAuth;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@ApiModel(value = "OprRecMbr", description = "")
@Table(name = "opr_msgRecMbr")
public class OprRecMbr implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    @JsonIgnore
    private Integer id;

    @ApiModelProperty(value = "站内信id")
    private Integer msgId;

    @ApiModelProperty(value = "会员id")
    private Integer mbrId;

    @ApiModelProperty(value = "是否已读，0未读，1已读，2删除")
    private Integer isRead;

    @ApiModelProperty(value = "阅读时间")
    private String readDate;

    @Transient
    @ApiModelProperty(value = "会员名")
    private String mbrName;

    @Transient
    @ApiModelProperty(value = "标题")
    private String title;

    @Transient
    @ApiModelProperty(value = "开始发送时间")
    private String sendTimeFrom;

    @Transient
    @ApiModelProperty(value = "结束发送时间")
    private String sendTimeTo;

    @Transient
    @ApiModelProperty(value = "接收者类型")
    private Integer recType;

    @Transient
    @ApiModelProperty(value = "开始阅读时间")
    private String readFrom;

    @Transient
    @ApiModelProperty(value = "结束阅读时间")
    private String readTo;

    @Transient
    @ApiModelProperty(value = "站内信内容")
    private String context;

    @Transient
    @ApiModelProperty(value = "类型")
    private Integer mbrType;

    @Transient
    @ApiModelProperty(value = "发送时间")
    private String createTime;

    @Transient
    @ApiModelProperty(value = "发送人")
    private String sender;

    @Transient
    @ApiModelProperty(value = "会员名")
    private String realName;


    @Transient
    @ApiModelProperty(value = "总代ids")
    private List<Integer> genAgtIds;

    @Transient
    @ApiModelProperty(value = "代理ids")
    private List<Integer> agtIds;

    @Transient
    @ApiModelProperty(value = "会员组ids")
    private List<Integer> mbrGpIds;

    @Transient
    @ApiModelProperty(value = "会员集合")
    private List<MbrAccount> mbrList;
    
    @Transient
    @ApiModelProperty(value = "代理集合")
    private List<AgentAccount> agyList;

    @Transient
    @ApiModelProperty(value = "会员组id")
    private Integer groupId;
    @Transient
    private BaseAuth baseAuth;

    @Transient
    @ApiModelProperty(value = "类型 查询使用")
    private List<Integer> mbrTypes;

    @Transient
    @ApiModelProperty(value = "是否已读，0未读，1已读，2删除 查询使用")
    private  List<Integer> isReads;

    @Transient
    @ApiModelProperty(value = "保存是否已读为0的值")
    private List<Integer> isReadsY;

    @Transient
    @ApiModelProperty(value = "保存是否已读不为0的值")
    private List<Integer> isReadsN;
}