package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Table(name = "mbr_message")
public class MbrMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "按查询条件的最后回复或者提交时间")
    private String time;

    @ApiModelProperty(value = "0 待处理 1回复")
    private Integer isRevert;

    @Transient
    @ApiModelProperty(value = "会员组id")
    private Integer groupId;

    @Transient
    @ApiModelProperty(value = "groupName")
    private String groupName;

    @Transient
    @ApiModelProperty(value = "内容")
    private String textContent;

    @Transient
    @ApiModelProperty(value = "总代")
    private Integer tagencyId;

    @Transient
    @ApiModelProperty(value = "实际的最后回复或者提交时间")
    private String lastTime;

    @Transient
    @ApiModelProperty(value = "查询：消息类型  0全部消息，1.人工消息，2.管家消息")
    private Integer msgType;

    @Transient
    private Integer infoId;

    @Transient
    private Integer isRealName;

    @Transient
    private String realName;

    @Transient
    @ApiModelProperty(value ="批量更新的messageid")
    private List<Integer> groups;

}