package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AgentComReportDto {

    /**
     *  总计信息(列表字段)：12个信息
     */
    // 资金报表 8个
    @ApiModelProperty(value = "净盈利")
    private BigDecimal totalProfit=BigDecimal.ZERO;
    @ApiModelProperty(value = "存款人数")
    private BigDecimal totalDepositBalanceNum=BigDecimal.ZERO;
    @ApiModelProperty(value = "存款金额：总存款")
    private BigDecimal totalDepositBalance=BigDecimal.ZERO;
    @ApiModelProperty(value = "提款人数")
    private BigDecimal totalDrawAmountNum=BigDecimal.ZERO;
    @ApiModelProperty(value = "提款金额：总提款")
    private BigDecimal totalDrawAmount=BigDecimal.ZERO;
    @ApiModelProperty(value = "优惠人数  优惠：任务+红利(活动)")
    private BigDecimal totalBonusAmountNum=BigDecimal.ZERO;
    @ApiModelProperty(value = "所有优惠金额  任务+红利(活动)")
    private BigDecimal totalBonusAmount=BigDecimal.ZERO;
    @ApiModelProperty(value = "任务返利")
    private BigDecimal totalTaskBonusAmount=BigDecimal.ZERO;
    @ApiModelProperty(value = "优惠金额")
    private BigDecimal totalYouhuiBonusAmount=BigDecimal.ZERO;
    @ApiModelProperty(value = "派彩金额：总派彩")
    private BigDecimal totalPayout=BigDecimal.ZERO;
    @ApiModelProperty(value = "存款实际到账金额：总提款")
    private BigDecimal totalActualarrival=BigDecimal.ZERO;
    @ApiModelProperty(value = "首存金额")
    private BigDecimal totalNewDepositAmount=BigDecimal.ZERO;
    // 总览 4个
    @ApiModelProperty(value = "活跃人数:活跃会员")
    private BigDecimal totalActiveMbrs=BigDecimal.ZERO;
    @ApiModelProperty(value = "投注人数:投注>0")
    private BigDecimal totalBetMbrs=BigDecimal.ZERO;
    @ApiModelProperty(value = "新增会员")
    private BigDecimal totalNewMbrs=BigDecimal.ZERO;
    @ApiModelProperty(value = "首存人数：时间范围内首存人数")
    private BigDecimal totalNewDeposits=BigDecimal.ZERO;
    @ApiModelProperty(value = "有效投注额：总有效投注")
    private BigDecimal totalValidBets=BigDecimal.ZERO;

    @ApiModelProperty(value = "存提比例:取款金额/存款金额")
    private BigDecimal ctRatio=BigDecimal.ZERO;
    @ApiModelProperty(value = "流水比例:总有效流水/（总红利+存款）")
    private BigDecimal lsRatio=BigDecimal.ZERO;
    @ApiModelProperty(value = "输赢比例:输赢值（派彩）/投注额")
    private BigDecimal syRatio=BigDecimal.ZERO;
    @ApiModelProperty(value = "优惠比例:优惠金额/存款金额")
    private BigDecimal yhRatio=BigDecimal.ZERO;
    @ApiModelProperty(value = "存提差:存款总额-提款总额")
    private BigDecimal ctDiffer=BigDecimal.ZERO;
    @ApiModelProperty(value = "投注最多的平台")
    private String mostBetsPlat;
    /**
     * 列表字段
     */
    @ApiModelProperty(value = "日期")
    private String createTime;
    @ApiModelProperty(value = "盈亏更新时间")
    private String winloseLastTime;
    @ApiModelProperty(value = "下级代理个数")
    private BigDecimal totalSubAgentNum;
    @ApiModelProperty(value = "下级会员个数")
    private BigDecimal totalSubMbrNum;

    @ApiModelProperty(value = "代理id")
    private Integer agyId;
    @ApiModelProperty(value = "代理名称")
    private String agyAccount;
    @ApiModelProperty(value = "上级代理id")
    private Integer parentId;
    @ApiModelProperty(value = "部门(类别)")
    private String cateGory;
    @ApiModelProperty(value = "总代id")
    private Integer tagencyId;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "会员id")
    private String accountId;
    @ApiModelProperty(value = "净输赢冲销")
    private BigDecimal netwinlose;
    @ApiModelProperty(value = "平台费用")
    private BigDecimal cost = BigDecimal.ZERO;
    @ApiModelProperty(value = "服务费用")
    private BigDecimal serviceCost = BigDecimal.ZERO;
    @ApiModelProperty(value = "存取款之和")
    private BigDecimal sumDepositAndWithdrawal = BigDecimal.ZERO;
    @ApiModelProperty(value = "资金调整金额")
    private BigDecimal calculateProfit = BigDecimal.ZERO;

    /**
     * 查询参数
     */
    @ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;
    @ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;
    @ApiModelProperty(value = "分页页数")
    private Integer pageNo;
    @ApiModelProperty(value = "分页大小")
    private Integer pageSize;
    @ApiModelProperty(value = "排序条件")
    private String orderBy;
    @ApiModelProperty(value = "是否是子代 1是 0不是(总代)")
    private Integer isCagency;
    @ApiModelProperty(value = "是否是测试代理 true是 false不是")
    private Boolean isTest;
    @ApiModelProperty(value = "是否查询直属代理")
    private Boolean isDirectlyAgy;
    @ApiModelProperty(value = "部门id")
    private Integer departmentid;
    @ApiModelProperty(value = "属性列表")
    private List<Long> attributesList;
    @ApiModelProperty(value = "属性列表")
    private List<Long> departmentIdList;
    @ApiModelProperty(value = "是否包含空部门")
    private Boolean departmentIdIsNull = false;

    // 导出参数/特别数据
    @ApiModelProperty(value = "导出报表类型：totalListByDay：按天汇总视图  tagencyList：总代(股东)视图/下级代理视图  categoryList：总代下部门(类别)视图  memberList：下级会员列表")
    private String module;
    @ApiModelProperty(value = "上级代理名，股东的上级就是'股东'")
    private String parentAgyAccount;

    // 其他
    @ApiModelProperty(value = "直属代理id")
    private Integer cagencyId;
    @ApiModelProperty(value = "分线id")
    private Integer subcagencyId;
    @ApiModelProperty(value = "分组属性")
    private Boolean groubyAgent = Boolean.FALSE;
    @ApiModelProperty(value = "代理等级")
    private Integer agentLevel;

    private Integer feemodel;
    
    @ApiModelProperty(value = "投注数量")
    private Integer betCount;
    
    @ApiModelProperty(value = "代理查询平台费， 服务费时，不包含自身代理")
    private Boolean isNotIncludeSelf;

    @ApiModelProperty(value = "代理线导出选择类型")
    private List<Integer> agentLineExportTypes;
}