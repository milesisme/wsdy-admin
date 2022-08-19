package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@Table(name = "agy_sub_account")
public class AgentSubAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "代理密码")
    private String agyPwd;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "salt")
    private String salt;

    @ApiModelProperty(value = "代理id")
    private Integer agentId;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "备注")
    private String memo;

    @Transient
    private List<AgySubMenu> subMenus;

    @Transient
    @ApiModelProperty(value = "代理账号")
    private String subAgyAccount;
}