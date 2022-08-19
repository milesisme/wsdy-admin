package com.wsdy.saasops.modules.agent.service;

import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.dto.AgentChargeMDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.entity.*;
import com.wsdy.saasops.modules.agent.mapper.*;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.constants.ColumnAuthConstants.AGENT_MOBILE_CONTACT;
import static com.wsdy.saasops.common.constants.ColumnAuthConstants.AGENT_NAME_CONTACT;


@Slf4j
@Service
public class AgentExportService {

    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgentNewMapper agentNewMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private AgyReportMapper agyReportMapper;
    @Autowired
    private AgentCommMapper agentCommMapper;
    @Autowired
    private DepositMapper depositMapper;
    @Autowired
    private WithdrawMapper withdrawMapper;
    @Autowired
    private AgyAuditMapper agyAuditMapper;
    @Autowired
    private WaterCostMapper waterCostMapper;
    @Autowired
    private ChargeCostMapper chargeCostMapper;
    @Autowired
    private MbrMapper mbrMapper;

    public R checkFile(String module, Long userId, String path) {
        // 查询用户的module下载记录
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    public SysFileExportRecord agyAccountReviewExcel(AgentAccount agentAccount, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<AgentAccount> accounts = agentNewMapper.newfindAgyAccountListPage(agentAccount);
            List<Map<String, Object>> list = accounts.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord agentDomainExport(AgyDomain agyDomain, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<AgyDomain> agyDomains = agentMapper.findAgyDomainListPage(agyDomain);
            List<Map<String, Object>> list = agyDomains.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord agentContractExport(Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<AgentContract> contracts = contractMapper.contractList();
            List<Map<String, Object>> list = contracts.stream().map(e -> {
                e.setModifyTime(getTime(e.getModifyTime()));
                e.setValidbetmaxStr("周期内累计投注额 ≥" + e.getValidbetmax());
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    /**
     * 	代理列表导出
     * 
     * @param agentAccount
     * @param userId
     * @param agentReviewExcelPath
     * @param module
     * @return
     */
    public SysFileExportRecord agentListExport(AgentAccount agentAccount, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            agentAccount.setStatus(Constants.EVNumber.one);
            List<AgentAccount> accounts = agentNewMapper.newfindAgyAccountListPage(agentAccount);
            String moblie = mbrMapper.findAccountContact(userId, AGENT_MOBILE_CONTACT);
            String name = mbrMapper.findAccountContact(userId, AGENT_NAME_CONTACT);
            accounts.stream().forEach(ls -> {
                if (StringUtils.isEmpty(moblie) && StringUtils.isNotEmpty(ls.getMobile())) {
                    ls.setMobile(StringUtil.phone(ls.getMobile()));
                }
                if (StringUtils.isEmpty(name) && StringUtils.isNotEmpty(ls.getRealName())) {
                    ls.setRealName(StringUtil.realName(ls.getRealName()));
                }
            });
            List<Map<String, Object>> list = accounts.stream().map(e -> {
                e.setCreateTime(getTime(e.getCreateTime()));
                setNum2Char(e);
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }
    
    /**
     * 	代理域名列表导出
     * 
     * @return
     */
    public SysFileExportRecord agentDomainListExport(AgyDomain agyDomain, Long userId, String agentReviewExcelPath, String module) {
    	SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
    	if (null != record) {
    		List<AgyDomain> domainList = agentMapper.findAgyDomainListPage(agyDomain);
    		List<Map<String, Object>> list = domainList.stream().map(e -> {
    			e.setCreateTime(getTime(e.getCreateTime()));
    			Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
    			return entityMap;
    		}).collect(Collectors.toList());
    		String siteCode = CommonUtil.getSiteCode();
    		sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
    	}
    	return record;
    }

    private void setNum2Char(AgentAccount account) {
        if (account.getAttributes() == 0) {
            account.setAttributesStr("直线代理");
        } else if (account.getAttributes() == 1) {
            account.setAttributesStr("分线代理");
        } else if (account.getAttributes() == 2) {
            account.setAttributesStr("推广员工");
        } else if (account.getAttributes() == 3) {
            account.setAttributesStr("招商员工");
        }
        if (account.getAttributes() != 1) {
            if (account.getAgentType() == 0) {
                account.setAgentTypeStr("股东");
            } else if (account.getAgentType() == 1) {
                account.setAgentTypeStr("总代");
            } else if (account.getAgentType() == 2) {
                account.setAgentTypeStr("一级代理");
            } else if (account.getAgentType() == 3) {
                account.setAgentTypeStr("二级代理");
            }
        }
    }

    public SysFileExportRecord upperScoreRecordExport(AgyBillDetail billDetail, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            billDetail.setWalletType(Constants.EVNumber.one);
            List<AgyBillDetail> detailList = agyReportMapper.upperScoreRecord(billDetail);
            List<Map<String, Object>> list = detailList.stream().map(e -> {
                e.setOrderTime(getTime(e.getOrderTime()));
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord commissionReportExport(AgyCommission commission, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<AgyCommission> reportDtos = agentCommMapper.commissionReport(commission);
            List<Map<String, Object>> list = reportDtos.stream().map(e -> {
                e.setPassTime(getTime(e.getPassTime()));
                e.setSubAgyaccount(e.getType() == 1 ? e.getSubAgyaccount() : "-");
                e.setTypeStr(e.getType() == 1 ? "下级返佣" : "会员返佣");
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord agentAccountChangeExport(AgyBillDetail billDetail, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            billDetail.setWalletType(Constants.EVNumber.one);
            List<AgyBillDetail> detailList = agyReportMapper.agentAccountChange(billDetail);
            List<Map<String, Object>> list = detailList.stream().map(e -> {
                e.setOrderTime(getTime(e.getOrderTime()));
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord commissionDepotCostExport(DepotCostDto dto, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<DepotCostDto> dtoList = agentCommMapper.depotCostList(dto);
            List<Map<String, Object>> list = dtoList.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord agentOnLineExport(AgentDeposit fundDeposit, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<AgentDeposit> deposits = depositMapper.findDepositList(fundDeposit);
            List<Map<String, Object>> list = deposits.stream().map(e -> {
                e.setAuditTime(getTime(e.getAuditTime()));
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord accWithdrawExport(AgyWithdraw accWithdraw, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<Integer> statuss = accWithdraw.getStatuss();
            if (Collections3.isNotEmpty(statuss)) {
                int forFlag = statuss.size();
                for (int i = 0; i < statuss.size(); i++) {
                    if (2 == statuss.get(i) || 4 == statuss.get(i)) {
                        statuss.add(4);
                        statuss.add(2);
                    } else if (3 == statuss.get(i) || 5 == statuss.get(i)) {
                        statuss.add(3);
                        statuss.add(5);
                    }
                    if (i + 1 == forFlag) {
                        break;
                    }
                }
            }
            List<AgyWithdraw> accWithdrawList = withdrawMapper.findAccWithdrawList(accWithdraw);
            List<Map<String, Object>> list = accWithdrawList.stream().map(e -> {
                e.setAuditTime(getTime(e.getAuditTime()));
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }


    public SysFileExportRecord agentAuditExport(AgentAudit fundAudit, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<AgentAudit> agentAudits = agyAuditMapper.auditList(fundAudit);
            List<Map<String, Object>> list = agentAudits.stream().map(e -> {
                e.setCreateTime(getTime(e.getCreateTime()));
                if (e.getWalletType() == 1){
                    e.setWalletTypeStr("代充钱包");
                }else if (e.getWalletType() == 2){
                    e.setWalletTypeStr("彩金钱包");
                }else {
                    e.setWalletTypeStr("佣金钱包");
                }

                e.setFinancialCode("GA".equals(e.getFinancialCode()) ? "人工增加" : "人工减少");
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord commissionReviewExport(AgyCommission commission, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            commission.setType(Constants.EVNumber.zero);
            List<AgyCommission> commissions = agentCommMapper.commissionReviewList(commission);
            List<Map<String, Object>> list = commissions.stream().map(e -> {
                e.setAuditTime(getTime(e.getAuditTime()));
                e.setCycleTime(e.getCycleStart() + "/" + e.getCycleEnd());
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }
    
    public SysFileExportRecord commissionAllSubListExport(AgyCommission commission, Long userId, String agentReviewExcelPath, String module) {
    	SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
    	if (null != record) {
    		commission.setType(Constants.EVNumber.one);
        	List<AgyCommission> commissions = agentCommMapper.commissionAllSubList(commission);
    		List<Map<String, Object>> list = commissions.stream().map(e -> {
    			if (e.getAgentType() == 0) {
    				e.setAgentTypeStr("股东");
    			} else if (e.getAgentType() == 1) {
    				e.setAgentTypeStr("总代");
    			} else if (e.getAgentType() == 2) {
    				e.setAgentTypeStr("一级代理");
    			} else if (e.getAgentType() == 3) {
    				e.setAgentTypeStr("二级代理");
    			}
    			e.setAuditTime(getTime(e.getAuditTime()));
    			e.setCycleTime(e.getCycleStart() + "/" + e.getCycleEnd());
    			Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
    			return entityMap;
    		}).collect(Collectors.toList());
    		String siteCode = CommonUtil.getSiteCode();
    		sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
    	}
    	return record;
    }

    public SysFileExportRecord commissionFreedListExport(AgyCommission commission, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            commission.setReviewStatus(Constants.EVNumber.one);
            commission.setType(Constants.EVNumber.zero);
            List<AgyCommission> reportDtos = agentCommMapper.commissionReviewList(commission);
            List<Map<String, Object>> list = reportDtos.stream().map(e -> {
                e.setAuditTime(getTime(e.getAuditTime()));
                e.setCycleTime(e.getCycleStart() + "/" + e.getCycleEnd());
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }


    public SysFileExportRecord agentDepotCostExport(DepotCostDto dto, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<DepotCostDto> reportDtos = waterCostMapper.findCostReportViewAgent(dto);
            List<Map<String, Object>> list = reportDtos.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord accountDepotCostExport(DepotCostDto dto, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            dto.setAccountAgyAccount(dto.getAgyAccount());
            dto.setTopAgyAccount(dto.getAgyAccount());
            dto.setAgyAccount(null);
            List<DepotCostDto> reportDtos = waterCostMapper.findCostListLevel(dto);
            List<Map<String, Object>> list = reportDtos.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord serviceChargAgentCostExport(AgentChargeMDto dto, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<AgentChargeMDto> reportDtos = chargeCostMapper.findServiceChargAgent(dto);
            List<Map<String, Object>> list = reportDtos.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public SysFileExportRecord serviceChargAccountCostExport(AgentChargeMDto dto, Long userId, String agentReviewExcelPath, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            List<AgentChargeMDto> reportDtos = chargeCostMapper.findServiceChargAccount(dto);
            List<Map<String, Object>> list = reportDtos.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(agentReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }


    public String getTime(String time) {
        if (StringUtils.isNotEmpty(time) && time.length() > 19) {
            return time.substring(0, 19);
        }
        return time;
    }
}

