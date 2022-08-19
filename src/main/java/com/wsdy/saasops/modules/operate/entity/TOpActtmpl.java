package com.wsdy.saasops.modules.operate.entity;

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
@ApiModel(value = "TOpActtmpl", description = "运营管理-活动模板")
@Table(name = "t_op_acttmpl")
public class TOpActtmpl implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String preferentialCode = "AQ0000001";  // 首存送
    public static final String registerCode = "AQ0000002";      // 注册送(旧)
    public static final String depositSentCode = "AQ0000003";   // 存就送
    public static final String rescueCode = "AQ0000004";        // 救援金
    public static final String waterRebatesCode = "AQ0000005";  // 返水优惠
    public static final String validCode = "AQ0000006";         // 有效投注
    public static final String recommendBonusCode = "AQ0000007";// 推荐送
    public static final String signInCode = "AQ0000008";        // 签到
    public static final String redPacketCode = "AQ0000009";     // 红包
    public static final String contentCode = "AQ0000010";       // 活动内容
    public static final String mbrRebateCode = "AQ0000011";     // 返利活动
    public static final String bettingGiftCode = "AQ0000012";   // 投就送活动
    public static final String registerGiftCode = "AQ0000013";  // 注册送活动
    public static final String memDayGiftCode = "AQ0000014";    // 会员日活动
    public static final String otherCode = "AQ0000015";         // 其它活动
    public static final String upgradeBonusCode = "AQ0000016";  // 升级礼金
    public static final String birthdayCode = "AQ0000017";      // 生日礼金
    public static final String vipPrivilegesCode = "AQ0000018"; // VIP特权存就送
    public static final String vipRedenvelopeCode = "AQ0000019";// VIP红包
    public static final String lotteryCode = "AQ0000021";       // 抽奖
    public static final String redPacketRainCode = "AQ0000022"; // 红包雨
    public static final String mbrRebateAgentCode = "AQ0000023"; // 全民代理
    public static final String mixActivityCode = "AQ0000024";   // 混合规则
    public static final String appDownloadGiftCode = "AQ0000025";   // app下载礼金

    public static final String affActivityCode = "AQ0000020";   // aff代理区分
    public static final String redEnvelopeActivityCode = "AQ0000026";   // 包赔红包记录
    public static final String mbrRebateHuPengCode = "AQ0000027";     // 呼朋唤友活动

    public static final String allActivityCode = "AQALL";// 全部活动
    public static final String firstChargeCode = "AQ0000028";  // 首存送返上级
    public static final String m8EnvelopeCode = "AQ0000029";  // 体验派彩

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "活动分类编号")
    private String tmplCode;
    @ApiModelProperty(value = "活动分类名称")
    private String tmplName;
    @ApiModelProperty(value = "活动分类描述")
    private String tmplNameTag;
    @ApiModelProperty(value = "活动分类状态　1开启，0禁用")
    private Byte available;
    @ApiModelProperty(value = "活动分类排序号")
    private Integer sortId;
    @ApiModelProperty(value = "备注")
    private String memo;
    @ApiModelProperty(value = "创建人")
    private String createUser;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;
    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;
}