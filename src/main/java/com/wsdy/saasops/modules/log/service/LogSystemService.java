package com.wsdy.saasops.modules.log.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.formatEsDate;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wsdy.saasops.ElasticSearchConnection;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.sys.entity.SysOperatioLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.log.dao.LogSystemMapper;
import com.wsdy.saasops.modules.log.entity.LogSystem;
import com.github.pagehelper.PageHelper;


@Slf4j
@Service
public class LogSystemService extends BaseService<LogSystemMapper, LogSystem>{

    @Autowired
    private ElasticSearchConnection connection;

    public PageUtils queryListPage(LogSystem logSystem, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<LogSystem> list = queryListCond(logSystem);
        return BeanUtil.toPagedResult(list);
    }

    public void deleteBatch(String[] idArr) {
        //logSystemMapper.deleteBatch(ids);
    }

    public int save(String module, Byte flag, String remark, String createTime) {
        LogSystem logSystem = new LogSystem();
        logSystem.setModule(module);
        logSystem.setFlag(flag);
        logSystem.setRemark(remark);
        logSystem.setCreateTime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        return save(logSystem);
    }

    public int save(String module, String remark, String createTime) {
        return save(module, (byte) 0, remark, createTime);
    }

    public int save(String module, String remark) {
        return save(module, (byte) 0, remark, DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
    }

    public PageUtils queryLog(Integer pageNo, Integer pageSize, SysOperatioLog operatioLog) {
        List<Object> list = new ArrayList<>();
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (nonNull(operatioLog)) {
            if (StringUtil.isNotEmpty(operatioLog.getUserName())) {
                query.must(QueryBuilders.queryStringQuery(operatioLog.getUserName()).defaultField("userName"));
            }
            if (StringUtil.isNotEmpty(operatioLog.getOperatioTitle())) {
                query.must(QueryBuilders.queryStringQuery(operatioLog.getOperatioTitle()).defaultField("operatioTitle"));
            }
            if (StringUtil.isNotEmpty(operatioLog.getOperationTimeFrom())) {
                query.must(QueryBuilders.rangeQuery("operationTime").gte(formatEsDate(operatioLog.getOperationTimeFrom())));
            }
            if (StringUtil.isNotEmpty(operatioLog.getOperationTimeTo())) {
                query.must(QueryBuilders.rangeQuery("operationTime").lte(formatEsDate(operatioLog.getOperationTimeTo())));
            }
        }
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        query.must(builder);
        searchRequestBuilder.addSort("operationTime", SortOrder.DESC);
        searchRequestBuilder.setQuery(builder).setFrom((pageNo - 1) * pageSize).setSize(pageSize);
        searchRequestBuilder.setQuery(query);
        Response response = null;
        try {
            response = connection.restClient.performRequest("POST",
                    "/" +"sys_operatio/sysOperatioLog/_search", Collections.singletonMap("_source", "true"),
                    new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                list.add(JSON.parseObject(objmap.get("_source").toString(), SysOperatioLog.class));
            }
            Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
            PageUtils page = BeanUtil.toPagedResult(list);
            page.setTotalCount(total);
            page.setTotalPage(BigDecimalMath.ceil(total.intValue(), pageSize));
            page.setCurrPage(pageNo);
            return page;
        } catch (IOException e) {
            log.error("error:" + e);
        }
        return null;
    }
}
