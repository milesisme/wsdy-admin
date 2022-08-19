package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AgentWinLostReportModelDto {
    @ApiModelProperty(value = "代理Id")
    private Integer agyAccountId;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "会员账户")
    private String loginName;

    @ApiModelProperty(value = "表头： null股东 0总代 >=1 代理 account会员 4仅查询会员可使用  视图： 1 2 3")
    private String isSign;

    @ApiModelProperty(value = "会员账户 OR 代理")
    private String username;

    @ApiModelProperty(value = "是否分组")
    private Boolean isGroup = Boolean.FALSE;
    @ApiModelProperty(value = "游戏类型字符串   1体育Sport   3真人Live  5电子Slot   6棋牌Chess 8 捕鱼Hunter   12彩票Lottery  其他 Others ")
    private List<String> catCodes;

    @ApiModelProperty(value = "查询排序")
    private String orderBy;
}
