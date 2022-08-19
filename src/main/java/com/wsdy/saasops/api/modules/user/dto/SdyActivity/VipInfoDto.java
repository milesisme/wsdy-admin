package com.wsdy.saasops.api.modules.user.dto.SdyActivity;

import com.wsdy.saasops.modules.operate.dto.JUpgradeBonusLevelDto;
import com.wsdy.saasops.modules.operate.dto.JbirthdayInfoDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "活动申请DTO",description = "活动申请DTO")
public class VipInfoDto {

	@ApiModelProperty(value = "会员账号")
	private String loginName;

	@ApiModelProperty(value = "生日")
	private String birthday;

	@ApiModelProperty(value = "层级名称")
	private String tierName;

	@ApiModelProperty(value = "等级")
	private Integer accountLevel;

	@ApiModelProperty(value = "累计存款")
	private BigDecimal depositAmount;

	@ApiModelProperty(value = "累计投注")
	private BigDecimal validbet;

	@ApiModelProperty(value = "是否开启自动降级 0否 1是")
	private int downgradePromotion;

	@ApiModelProperty(value = "降级计算周期：最近多少天")
	private int downgradePromotionDay;

/*	@ApiModelProperty(value = "每日充许取款次数")
	private Integer withDrawalTimes;

	@ApiModelProperty(value = "每日取款限额")
	private BigDecimal withDrawalQuota;*/

	@ApiModelProperty(value = "生日礼金")
	private List<JbirthdayInfoDto> birthdayBonusList;

	@ApiModelProperty(value = "升级礼金")
	private List<JUpgradeBonusLevelDto> upgradeBonusLevelDtos;

	@ApiModelProperty(value = "每月红包")
	private VipMonthlyBonusDto monthlyBonus;

	@ApiModelProperty(value = "VIP等级")
	private List<ActivityLevelDto> activityLevelList;

	@ApiModelProperty(value = "返水")
	private List<ActivityLevelCatDto> activityLevelCatDtos;

	@ApiModelProperty(value = "豪礼赠送配置信息")
	private String hlzs;

	@ApiModelProperty(value = "活动规则与详情配置信息")
	private String hdgz;
}
