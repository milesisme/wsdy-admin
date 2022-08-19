package com.wsdy.saasops.modules.analysis.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.util.List;

@Data
public class BounsReportQueryModel {

    /***针对前端传入的时间进行处理***/
    private static final String strTiming = " 00:00:00";
    private static final String endTiming = " 23:59:59";

    private Integer pageNo;
    private Integer pageSize;
    /**
     * 总代理
     */
    private Integer parentAgentId;
//    /**
//     * 代理账号
//     */
//    private Integer agentid;
    /**
     * 会员组
     */
    private Integer groupid;
    /**
     * 会员名
     */
    private String userName;
    private String userId;
    /**
     * 活动ID
     */
    private Integer activityId;
    private Integer catId;
    private Integer origin;
    /*** 1 PC端 ，2 手机端***/
    private Integer enablePc;
    private Integer enableMb;
    private String betStrTime;
    private String betEndTime;
    private String loginSysUserName;

    @ApiModelProperty(value = "0 前台申请 1后台添加 2人工增加 3人工减少")
    private Integer source;

    //查询使用
    @Transient
    private List<Integer> parentAgentidList;
    @Transient
    private  List<Integer> agentIdList;
    @Transient
    private List<Integer> groupIdList;
    @Transient
    private List<Integer> activityIdList;
    //活动名称/客户端
    @Transient
    private String catIdList;
    @Transient
    private String originList;

    public Integer getEnablePc() {
        return origin != null && origin == 1 ? 1 : 0;
    }

    public Integer getEnableMb() {
        return origin != null && origin == 2 ? 1 : 0;
    }

    public String getBetStrTime() {
        if (betStrTime == null || betStrTime.isEmpty()) {
            return null;
        }
        return betStrTime + strTiming;
    }

    public String getBetEndTime() {
        if (betEndTime == null || betEndTime.isEmpty()) {
            return null;
        }
        return betEndTime + endTiming;
    }

    @Transient
//    @ApiModelProperty(value = "父代理")
    private Integer agentId;

    // 参数
    @Transient
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @Transient
    @ApiModelProperty(value = "代理 0正式 1测试")
    private Integer isTest;
    @Transient
    @ApiModelProperty(value = "代理名称")
    private String agyAccountStr;

    @ApiModelProperty(value = "代理名称")
    private String agyAccount;

    private Integer isSign;

    @ApiModelProperty(value = "是否线上活动，true：线上 false 线下 ")
    private Boolean isOnline;

    @ApiModelProperty(value = "指定下级代理名称")
    private String specifyAgyAccount;
}
