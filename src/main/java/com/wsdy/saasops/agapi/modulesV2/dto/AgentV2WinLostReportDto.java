package com.wsdy.saasops.agapi.modulesV2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AgentV2WinLostReportDto {

    // 查询参数
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "游戏类型字符串   1体育Sport   3真人Live  5电子Slot   6棋牌Chess 8 捕鱼Hunter   12彩票Lottery  其他 Others ")
    private List<String> catCodes;
    @ApiModelProperty(value = "代理名")
    private String agyAccount;
    @ApiModelProperty(value = "是否包含直属会员 0不包含  1包含")
    private Integer isContainMbr;
    @ApiModelProperty(value = "分页：页码")
    private Integer pageNo;
    @ApiModelProperty(value = "分页：分页大小")
    private Integer pageSize;

    // 列表数据: 返回数据
    @ApiModelProperty(value = "代理id")
    private Integer agentId;
    @ApiModelProperty(value = "用户名称：代理名/会员名")
    private String userName;
    @ApiModelProperty(value = "级别： 0 公司(总代)/1股东/2总代/ >2 代理  -1 会员 -2汇总")
    private Integer agentType;
    @ApiModelProperty(value = "投注金额")
    private BigDecimal betTotal;
    @ApiModelProperty(value = "赢输金额")
    private BigDecimal payoutTotal;
    @ApiModelProperty(value = "洗码量")
    private BigDecimal validbetTotal;
    @ApiModelProperty(value = "类别：真人Live  电子Slot")
    private String gamecategory;
    @ApiModelProperty(value = "真人分成")
    private BigDecimal realpeople;
    @ApiModelProperty(value = "电子分成")
    private BigDecimal electronic;
    @ApiModelProperty(value = "真人洗码佣金比例/洗码比")
    private BigDecimal realpeoplewash;
    @ApiModelProperty(value = "电子洗码佣金比例/洗码比")
    private BigDecimal electronicwash;

    // 列表数据： 计算数据
    @ApiModelProperty(value = "佣金比例/洗码比")
    private BigDecimal wash;
    @ApiModelProperty(value = "洗码佣金 = 洗码量 * 洗码比")
    private BigDecimal washCommission;
    @ApiModelProperty(value = "总金额 = 输赢金额+洗码佣金")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "分成")
    private BigDecimal revenue;
    @ApiModelProperty(value = "本级交公司 = 总金额 * (100-本级分成）")
    private BigDecimal toCompayAmount;
    @ApiModelProperty(value = "本级交公司投注金额 = 投注金额 * (100-本级分成）")
    private BigDecimal toCompayBet;
    @ApiModelProperty(value = "本级交公司投洗码量= 洗码量 * (100-本级分成）")
    private BigDecimal toCompayValidbet;
    @ApiModelProperty(value = "公司获利比例 = 本级交公司/本级交公司投注金额")
    private BigDecimal companyProfitRratio;

    // 股东下级代理明细 ： 总代明细股东相关字段
    @ApiModelProperty(value = "股东真人分成")
    private BigDecimal realpeopleShareholder;
    @ApiModelProperty(value = "股东电子分成")
    private BigDecimal electronicShareholder;
    @ApiModelProperty(value = "股东真人洗码佣金比例/洗码比")
    private BigDecimal realpeoplewashShareholder;
    @ApiModelProperty(value = "股东电子洗码佣金比例/洗码比")
    private BigDecimal electronicwashShareholder;

    @ApiModelProperty(value = "股东佣金比例/洗码比")
    private BigDecimal washShareholder;
    @ApiModelProperty(value = "股东洗码佣金 = 洗码量 * 洗码比")
    private BigDecimal washCommissionShareholder;
    @ApiModelProperty(value = "股东总金额 = 输赢金额+洗码佣金")
    private BigDecimal totalAmountShareholder;

    @ApiModelProperty(value = "股东股东分成")
    private BigDecimal revenueShareholder;
    @ApiModelProperty(value = "股东本级交公司 = 总金额 * (100-本级分成）")
    private BigDecimal toCompayAmountShareholder;

    // 导出字段
    @ApiModelProperty(value = "导出报表类型：winLostReportList 表头   winLostListLevel 下级代理   winLostListLevelMbr 下级会员 ")
    private String module;
    @ApiModelProperty(value = "级别： 0 公司(总代)/1股东/2总代/ >2 代理  -1 会员 -2汇总")
    private String agentTypeStr;
}

