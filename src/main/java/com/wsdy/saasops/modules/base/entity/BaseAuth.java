package com.wsdy.saasops.modules.base.entity;


import lombok.Data;
import lombok.ToString;

/**
 * Created by William on 2017/11/28.
 */
@Data
@ToString
public class BaseAuth {

    /**
     * 会员组ids
     */
    private String groupIds;


    /**
     * 地区代理ids
     */
    private String agyAccountIds ;

    public BaseAuth(String groupIds, String agyAccountIds) {
        this.groupIds = groupIds;
        this.agyAccountIds = agyAccountIds;
    }

    public BaseAuth() {}
}
