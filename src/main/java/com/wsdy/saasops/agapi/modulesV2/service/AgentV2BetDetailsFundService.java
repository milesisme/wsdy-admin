package com.wsdy.saasops.agapi.modulesV2.service;

import com.wsdy.saasops.agapi.modulesV2.mapper.AgentFundMapper;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetTotalModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
@Transactional
public class AgentV2BetDetailsFundService {

    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private AgentFundMapper agentFundMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private RedisService redisService;

    public PageUtils getBkRptBetListPage(AgentAccount account, Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        Boolean isAgent = setGameReportQueryModel(account, model);
        if (Boolean.FALSE.equals(isAgent)) {
            return new PageUtils();
        }
        PageUtils pageUtils = analysisService.getRptBetListPage(pageNo, pageSize, model);
        List<RptBetModel> list = (List<RptBetModel>) pageUtils.getList();
        if (list.size() != 0) {
            setEgDetails(list);
            if (Boolean.TRUE.equals(model.getIsSubtotal())) {
                //获取小计
                list.add(analysisService.getSubtotal(list));
            }
        }
        return pageUtils;
    }

    public List<RptBetTotalModel> getPlatformMaxTime(String siteCode) {
        return analysisService.getPlatformMaxTime(siteCode, null, "EG");
    }

    private Boolean setGameReportQueryModel(AgentAccount account, GameReportQueryModel model) {
        if (StringUtil.isEmpty(model.getAgyAccount()) && nonNull(account)) {
            String cagencyIdList = agentFundMapper.findChildnodeid(account.getId());
            if (StringUtil.isEmpty(cagencyIdList)) {
                return Boolean.FALSE;
            }
            model.setCagencyIdList(cagencyIdList);
        }
        if (StringUtil.isNotEmpty(model.getAgyAccount())) {
            AgentAccount agentAccount1 = agentFundMapper.findAgyAccount(model.getAgyAccount(), account.getId());
            if (isNull(agentAccount1)) {
                return Boolean.FALSE;
            }
            String cagencyIdList = agentFundMapper.findChildnodeid(agentAccount1.getId());
            if (StringUtil.isEmpty(cagencyIdList)) {
                return Boolean.FALSE;
            }
            model.setCagencyIdList(cagencyIdList);
        }
        return Boolean.TRUE;
    }

    private void setEgDetails(List<RptBetModel> rptBetModelList) {
        rptBetModelList.stream().forEach(rs -> {
            if (nonNull(rs.getOpenResultModel())) {
                List<Map<String, String>> resultMap = rs.getOpenResultModel().getResultMap();
                if (Collections3.isNotEmpty(resultMap)) {
                    Map<String, String> stringMap = resultMap.get(0);
                    String str = "";
                    if ("和".equals(rs.getResult())) {
                        str = "和 [";
                    }
                    if ("输".equals(rs.getResult()) && "庄".equals(rs.getBetType())) {
                        str = "闲 [";
                    }
                    if ("赢".equals(rs.getResult()) && "庄".equals(rs.getBetType())) {
                        str = "庄 [";
                    }
                    if ("输".equals(rs.getResult()) && "闲".equals(rs.getBetType())) {
                        str = "庄 [";
                    }
                    if ("赢".equals(rs.getResult()) && "闲".equals(rs.getBetType())) {
                        str = "闲 [";
                    }
                    String xOpenResult = stringMap.get("xOpenResult");
                    rs.setXOpenResult(getResult(xOpenResult));
                    str = str + "闲:" + rs.getXOpenResult() + ",";

                    String zOpenResult = stringMap.get("zOpenResult");
                    rs.setZOpenResult(getResult(zOpenResult));
                    str = str + "庄:" + rs.getZOpenResult() + "]";
                    rs.setEgDetails(str);
                }
            }
        });
    }

    public String getResult(String openResult) {
        openResult = openResult.replace("[", "");
        openResult = openResult.replace("]", "");
        openResult = openResult.replace("\"", "");
        String[] a = openResult.split(",");
        String sub = "";
        for (int i = 0; i < a.length; i++) {
            String al = a[i].substring(a[i].length() - 1);
            if ("T".equals(al)) {
                al = "10";
            }
            if ("1".equals(al)) {
                al = "A";
            }
            sub = sub + al;
            if (i < a.length - 1) {
                sub = sub + "-";
            }
        }
        return sub;
    }


    public SysFileExportRecord betDetailsExportExcel(GameReportQueryModel model, AgentAccount account, String module, String templatePath, String gameType) {
        Long userId = Long.valueOf(account.getId());
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sd.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String key = RedisConstants.EG_EXCEL_EXPORT + CommonUtil.getSiteCode() + module + userId;
            String siteCode = CommonUtil.getSiteCode();
            model.setSiteCode(siteCode);
            model.setIsEgBetDetauls(Boolean.TRUE);
            List<Map<String, Object>> list = null;
            try {
                setGameReportQueryModel(account, model);
                Boolean isAgent = setGameReportQueryModel(account, model);
                if (Boolean.FALSE.equals(isAgent)) {
                    throw new R200Exception("没有可以导出的数据");
                }
                List<RptBetModel> bets = analysisService.getRptBetList(model);
                if (CollectionUtils.isEmpty(bets)) {
                    throw new RRException("没有可导出的数据!");
                }
                setEgDetails(bets);
                list = bets.stream().map(e -> {
                    Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                    Date betTime = e.getBetTime();
                    Date payOutTime = e.getPayoutTime();
                    if (betTime != null) {
                        entityMap.put("betTime", sd.format(betTime));
                    }
                    if (payOutTime != null) {
                        entityMap.put("payoutTime", sd.format(payOutTime));
                    }
                    return entityMap;
                }).collect(Collectors.toList());
                sysFileExportRecordService.exportExcel(templatePath, list, userId, module, siteCode);//异步执行
            } catch (Exception e) {
                log.error("agentbetDetailsExportExcel", e);
                throw new RRException("查询异常,请稍后再试!");
            } finally {
                redisService.del(key);
            }
        }
        return record;
    }


    public List<TGmGame> egGameNameList() {
        return agentFundMapper.egGameNameList("EG");
    }

    public List<RptBetModel> finalBetting(AgentAccount account, GameReportQueryModel model) {
        Boolean isAgent = setGameReportQueryModel(account, model);
        if (Boolean.FALSE.equals(isAgent)) {
            return null;
        }
        PageUtils pageUtils = analysisService.getRptBetListPage(1, 10, model);
        List<RptBetModel> list = (List<RptBetModel>) pageUtils.getList();
        if (list.size() > 0) {
            setEgDetails(list);
        }
        return list;
    }
}
