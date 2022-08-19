package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

import javax.persistence.Transient;
import java.util.List;

@Data
public class GameReportModel {
    /***针对前端传入的时间进行处理***/
    private static final String strTiming = " 00:00:00";
    private static final String endTiming = " 23:59:59";

    private Integer pageNo;
    private Integer pageSize;
    private String betStrTime;
    private String betEndTime;
    private Integer catId;
    private Integer agent;
    private String userName;
    private Integer agentId;
    private Integer platform;
    private Integer groupId;
    private String gameCode;
    private Integer topAgent;
    private Integer topAgentId;
    private Integer parentAgentId;

    @Transient
    private List<Integer> platformList;
    @Transient
    private String catIdList;
    @Transient
    private String parentAgentIdList;
    @Transient
    private String agentIdList;
    @Transient
    private String groupIdList;

    private List<Integer> agentIds;

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

    private Integer isSign;
}
