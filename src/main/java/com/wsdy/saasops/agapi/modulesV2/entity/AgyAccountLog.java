package com.wsdy.saasops.agapi.modulesV2.entity;

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
@ApiModel(value = "agy_account_log", description = "AgyAccountLog")
@Table(name = "agy_account_log")
public class AgyAccountLog implements Serializable {

    private static final long serialVersionUID = 1L;

    // moduleName
    public static final String AGENT_V2_AGYACCOUNTSAVE = "agyAccountSave";              // 开户
    public static final String AGENT_V2_UPDATEAGENTPASSOWRD = "updateAgentPassword";    // 密码
    public static final String AGENT_V2_UPDATEAVAILABLE = "updateAvailable";            // 账号状态
    public static final String AGENT_V2_UPDATEBETTINGSTATUS = "updateBettingStatus";    // 投注状态
    public static final String AGENT_V2_BINDAGENTMOBILE = "bindAgentMobile";            // 电话号码
    public static final String AGENT_V2_REALPEOPLE = "realpeople";                      // 真人占成
    public static final String AGENT_V2_REALPEOPLEPARENT = "realpeopleParent";          // 真人代理分成比
    public static final String AGENT_V2_REALPEOPLEWASH = "realpeoplewash";              // 真人洗码佣金
    public static final String AGENT_V2_ELECTRONIC = "electronic";                      // 电子占成
    public static final String AGENT_V2_ELECTRONICPARENT = "electronicParent";          // 电子代理分成比
    public static final String AGENT_V2_ELECTRONICWASH = "electronicwash";              // 电子洗码佣金
    public static final String AGENT_V2_BALANCE = "balance";                            // 点数

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "执行代理id")
    private Integer agyId;
    @ApiModelProperty(value = "执行代理名")
    private String agyAccount;
    @ApiModelProperty(value = "被执行用户名")
    private String operatorUser;
    @ApiModelProperty(value = "createTime")
    private String createTime;
    @ApiModelProperty(value = "变更前")
    private String beforeChange;
    @ApiModelProperty(value = "变更后")
    private String afterChange;
    @ApiModelProperty(value = "模块名")
    private String moduleName;
    @ApiModelProperty(value = "备注")
    private String memo;
    @ApiModelProperty(value = "操作ip")
    private String ip;
}