package com.wsdy.saasops.modules.operate.entity;

import com.wsdy.saasops.modules.api.dto.DepotCatDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "OprActActivity", description = "活动设置")
@Table(name = "opr_act_activity")
public class OprActActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "活动类别ID")
    @Transient
    private Integer catId;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "生效开始日期")
    private String useStart;

    @ApiModelProperty(value = "生效结束日期")
    private String useEnd;

    @ApiModelProperty(value = "0未开始 、1进行中、2已失效")
    private Integer useState;

    @ApiModelProperty(value = "前端展示 1展示 0不展示")
    private Integer isShow;

    @ApiModelProperty(value = "开启PC端 1开启 0禁用")
    private Integer enablePc;

    @ApiModelProperty(value = "开启手机端 1开启 0禁用")
    private Integer enableMb;

    @ApiModelProperty(value = "PC端活动图片")
    private String pcLogoUrl;

    @ApiModelProperty(value = "pc file Name")
    private String pcRemoteFileName;

    @ApiModelProperty(value = "手机端活动图片")
    private String mbLogoUrl;

    @ApiModelProperty(value = "手机 file Name")
    private String mbRemoteFileName;

    @ApiModelProperty(value = "pc活动内容")
    private String content;

    @ApiModelProperty(value = "手机活动内容")
    private String mbContent;

    @ApiModelProperty(value = "建立时间")
    private String createTime;

    @ApiModelProperty(value = "createUser")
    private String createUser;

    @ApiModelProperty(value = "modifyUser")
    private String modifyUser;

    @ApiModelProperty(value = "modifyTime")
    private String modifyTime;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @ApiModelProperty(value = "是删除  0否 1是")
    private Integer isdel;

    @ApiModelProperty(value = "活动标签")
    private Integer labelId;

    @Transient
    @ApiModelProperty(value = "标签名称")
    private String labelName;

    @ApiModelProperty(value = "模板名称")
    @Transient
    private String tmplName;
    
    @ApiModelProperty(value = "规则名称")
    @Transient
    private String rulename;

    @ApiModelProperty(value = "活动分类描述")
    @Transient
    private String tmplNameTag;

    @ApiModelProperty(value = "分类名称")
    @Transient
    private String catName;

    @ApiModelProperty(value = "申请未审核数")
    @Transient
    private Integer applyNum;

    @ApiModelProperty(value = "规则字符串")
    @Transient
    private String rule;

    @ApiModelProperty(value = "tmplCode")
    @Transient
    private String tmplCode;

    @ApiModelProperty(value = "路径")
    @Transient
    private String fastDfsUrl;

    @ApiModelProperty(value = "ruleId")
    private Integer ruleId;

    @Transient
    @ApiModelProperty(value = "分类id")
    private List<Integer> actIds;

    @ApiModelProperty(value = "0已失效, 1立即领取，2立即存款，3已领取，4不显示")
    @Transient
    private Byte buttonShow;

    @ApiModelProperty(value = "最小存款金额")
    @Transient
    private BigDecimal amountMin = BigDecimal.ZERO;
    
    @Transient
    @ApiModelProperty(value = "赠送最低金额")
    private BigDecimal bonusAmountMin = BigDecimal.ZERO;

    @Transient
    @ApiModelProperty(value = "赠送最高金额")
    private BigDecimal amountMax = BigDecimal.ZERO;

    @Transient
    @ApiModelProperty(value = "赠送最高比例")
    private BigDecimal donateAmount = BigDecimal.ZERO;

    @Transient
    @ApiModelProperty(value = "有效投注 投就送时为投注金额，救援金时为负盈利")
    private BigDecimal validBet = BigDecimal.ZERO;

    @Transient
    @ApiModelProperty(value = "赠送类型 0按比例 1按金额")
    private Integer donateType;

    @Transient
    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;

    @Transient
    @ApiModelProperty(value = "领取期限 自注册成功起drawType天内领取  * 活动期间注册方可领取，0 天代表不限制领取时限")
    private Integer drawType;


    /**
     * 查询使用
     **/
    @Transient
    private String actCatIdList;
    @Transient
    private String actTmplIdList;
    @Transient
    private String useStateList;
    @Transient
    @ApiModelProperty(value = "1审核，0否")
    private Integer isAudit;
    @Transient
    private List<OprActCat> actCatList;
    @Transient
    @ApiModelProperty(value = "活动模版id")
    private Integer actTmplId;

    @Override
    public String toString() {
        return "活动名称:" + activityName + "生效开始日期:" + useStart + "生效结束日期:" + useEnd + "前端展示 1展示 0不展示:" + isShow
                + "开启PC端 1开启 0禁用:" + enablePc + "开启手机端 1开启 0禁用" + enableMb + "PC端活动图片:" + pcLogoUrl + "活动内容" + content + "是否开启:" + available;
    }

    @ApiModelProperty(value = "返水统计区间 start")
    @Transient
    private String waterStart;

    @ApiModelProperty(value = "返水统计区间 end")
    @Transient
    private String waterEnd;

    @ApiModelProperty(value = "未审核数量")
    @Transient
    private Integer noaudit;

    @ApiModelProperty(value = "通过数量")
    @Transient
    private Integer pass;

    @ApiModelProperty(value = "拒绝数量")
    @Transient
    private Integer reject;

    @ApiModelProperty(value = "自助洗码状态 1开启，0关闭")
    @Transient
    private Integer isSelfHelp;

    @ApiModelProperty(value = "自助申请状态 1开启，0关闭")
    @Transient
    private Integer isSelfHelpShow;

    @ApiModelProperty(value = "自助洗码限制 1开启，0关闭")
    @Transient
    private Integer isLimit;

    @ApiModelProperty(value = "限制金额")
    @Transient
    private BigDecimal minAmount;

    @ApiModelProperty(value = "游戏分类数据")
    @Transient
    private List<DepotCatDto> depotCatDtoList;

    @ApiModelProperty(value = "领取时间")
    @Transient
    private String claimedTime;

    @Transient
    @ApiModelProperty(value = "层级id")
    private int actLevelId;

    @Transient
    @ApiModelProperty(value = "红利列表查询： 会员名")
    private String loginName;
    @Transient
    @ApiModelProperty(value = "红利列表查询： 审核状态  0 拒绝 1通过 2待审核 -->对应opr_act_bonus的status")
    private Integer status;

    @Transient
    @ApiModelProperty(value = "混合活动，已领取子规则个数")
    private Integer subCount;

    @Transient
    @ApiModelProperty(value = "已领取活动中的会员ID")
    private Integer accountId;

    @Transient
    @ApiModelProperty(value = "混合活动子规则是否都已领取 true false")
    private Boolean isAllSubClaime;
    
    @ApiModelProperty(value = "是否线上活动，true：线上 false 线下 ")
    private Boolean isOnline;

    @Transient
    @ApiModelProperty(value = "优惠ID")
    private Integer bonusId;

    @Transient
    @ApiModelProperty(value = "当前用户是否能领取该活动 0不可以 1可以")
    private Integer canApply;
    @Transient
    @ApiModelProperty(value = "当前用户能领取的活动金额")
    private BigDecimal canApplyBonus;
    @Transient
    @ApiModelProperty(value = "活动可参与金额")
    private BigDecimal activityAlready;
}