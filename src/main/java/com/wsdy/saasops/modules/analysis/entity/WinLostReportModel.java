package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

import javax.persistence.Transient;

@Data
public class WinLostReportModel {
    /***针对前端传入的时间进行处理***/
    private static final String strTiming = " 00:00:00";
    private static final String endTiming = " 23:59:59";

    private Integer pageNo;
    private Integer pageSize;
    private String betStrTime;
    private String betEndTime;
    private Integer userId;
    private Integer agentId;
    private Integer groupId;
    private String userName;
    private Integer parentAgentId;
    private String loginSysUserName;
    private String transactionType;

    //查询使用
    @Transient
    private String parentAgentIdList;
    @Transient
    private String agentIdList;
    @Transient
    private String groupIdList;

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

}
