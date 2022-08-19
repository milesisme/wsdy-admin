package com.wsdy.saasops.common.utils;

import com.wsdy.saasops.common.xss.SQLFilter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 查询参数
 *
 */
@Getter
@Setter
public class Query extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	//当前页码
    private int page;
    //每页条数
    private int limit;

    public Query(Map<String, Object> params){
        this.putAll(params);

        //分页参数
        if(!org.springframework.util.StringUtils.isEmpty(params.get("page")) && !org.springframework.util.StringUtils.isEmpty(params.get("limit"))) {
            this.page = Integer.parseInt(params.get("page").toString());
            this.put("page", page);
            this.limit = Integer.parseInt(params.get("limit").toString());
            this.put("limit", limit);
            this.put("offset", (page - 1) * limit);
        }
        //防止SQL注入（因为sidx、order是通过拼接SQL实现排序的，会有SQL注入风险）
        String sidx = (String)params.get("sidx");
        String order = (String)params.get("order");
        if(StringUtils.isNotBlank(sidx)){
            this.put("sidx", SQLFilter.sqlInject(sidx));
        }
        if(StringUtils.isNotBlank(order)){
            this.put("order", SQLFilter.sqlInject(order));
        }

    }
}
