package com.wsdy.saasops.modules.analysis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.util.List;


@Data
public class WinLostReportModelDto {

    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @ApiModelProperty(value = "代理账号")
    private String agyAccount;
    @ApiModelProperty(value = "会员账户")
    private String loginName;
    @ApiModelProperty(value = "返回时： 类别表头-->  null股东 0总代 >=1 代理 account会员 4仅查询会员可使用  代理视图切换表头level --> 1 agent 2 mbr 3 all ")
    private String isSign;
    @ApiModelProperty(value = "查询时：会员名 或 代理名  返回时：用于前端的账号列")
    private String username;
    @ApiModelProperty(value = "是否分组 按会员分组group  true 就是计算会员总的， false就是会员的游戏类别")
    private Boolean isGroup = Boolean.FALSE;
    @ApiModelProperty(value = "查询排序")
    private String orderBy;
    @ApiModelProperty(value = "游戏类型  1 体育， 3 真人，5,电子，6棋牌，8捕鱼，12彩票 ")
    private List<Integer> catIds;
    @ApiModelProperty(value = "游戏类型字符串   1体育Sport   3真人Live  5电子Slot   6棋牌Chess 8 捕鱼Hunter   12彩票Lottery  其他 Others ")
    private List<String> catCodes;
    @ApiModelProperty(value = "平台编码 数组")
    private List<String> depotCodes;

    @ApiModelProperty(value = "全民代理标志 0非代理会员 1代理会员")
    private Integer agyflag;

    @ApiModelProperty(value = "部门id")
    private Integer departmentid;

    @ApiModelProperty(value = "首存时间")
    @Transient
    private String depositTime;

    @Transient
    @ApiModelProperty(value = "首存时间开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String depositTimeStart;

    @Transient
    @ApiModelProperty(value = "首存时间结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String depositTimeEnd;

    @ApiModelProperty(value = "导出统计类型：0，全部统计；1：按日统计")
    private Integer exportType;
}
