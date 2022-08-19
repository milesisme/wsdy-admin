package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "MbrDepotWallet", description = "")
@Table(name = "mbr_depot_wallet")
public class MbrDepotWallet implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "")
    private String pwd;

    @ApiModelProperty(value = "平台ID号 平台号ID为0是本平台")
    private Integer depotId;

    @ApiModelProperty(value = "平台名称")
    private String depotName;

    @ApiModelProperty(value = "会员资金余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "0未登陆,1已登陆")
    private Byte isLogin;

    @ApiModelProperty(value = "0未转账,1已转账 或查询余额(1 成功,0失败)")
    private Byte isTransfer;

    @ApiModelProperty(value = "NT 唯一ID")
    private String uuid;

    @ApiModelProperty(value = "平博登陆Id")
    private String loginId;

    @Transient
    @ApiModelProperty(value = "账号是否建立 false 未建立，true 已建立")
    private Boolean isBuild;

    @Transient
    @ApiModelProperty(value = "回收平台账号余额")
    private Integer[] depotIds;

    @Transient
    @ApiModelProperty(value = "登陆之后返回TOKEN 或SEESION ID")
    private String token;

    @Transient
    @ApiModelProperty(value = "Nt使用")
    private Integer partyId;

    @ApiModelProperty(value = "更新余额最后时间")
    private String time;

    public interface IsTransFer {
        byte yes = 1;// 已转账
        byte no = 0;// 未转账
    }

    @Transient
    @ApiModelProperty(value = "平台名字 不能用钱包里面depotName")
    private String lastDepotName;
}