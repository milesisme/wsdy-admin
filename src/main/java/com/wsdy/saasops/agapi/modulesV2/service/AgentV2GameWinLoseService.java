package com.wsdy.saasops.agapi.modulesV2.service;


import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2WinLostReportDto;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentV2Mapper;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentV2WinLoseMapper;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.constants.Constants.ONE_HUNDRED;


@Slf4j
@Service
@Transactional
public class AgentV2GameWinLoseService {
    @Autowired
    private AgentV2WinLoseMapper winLoseMapper;
    @Autowired
    private AgentV2Mapper agentAccountMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private AnalysisService analysisService;

    public List<AgentV2WinLostReportDto> findWinLostReportList(AgentV2WinLostReportDto reportModelDto) {
        // 获得该代理信息
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(reportModelDto.getAgyAccount());
        AgentAccount account = agentAccountMapper.getAgentInfo(agentAccount);
        if(Objects.isNull(account)){
            throw new R200Exception(reportModelDto.getAgyAccount() + "该账号不存在！");
        }

        // 获得级别 0 公司(总代)/1股东/2总代/ >2 代理   -1 会员  -2 总计
        String agentTypeStr = winLoseMapper.findAgyAccountDepth(reportModelDto.getAgyAccount());
        if(StringUtils.isEmpty(agentTypeStr)){
            throw new R200Exception(reportModelDto.getAgyAccount() + "该账号不存在！！");
        }
        Integer agentType = Integer.valueOf(agentTypeStr);

        reportModelDto.setAgentType(agentType);
        reportModelDto.setIsContainMbr(Constants.EVNumber.zero);    // 默认不包含直属会员  0 公司(总代)/1股东

        // 包括所属会员 2总代 和 >2 代理
        if(Integer.valueOf(Constants.EVNumber.two).compareTo(agentType) <= 0){
            reportModelDto.setIsContainMbr(Constants.EVNumber.one);
        }

        // 查询数据
        List<AgentV2WinLostReportDto>  list = winLoseMapper.findWinLostReportList(reportModelDto);

        // 无数据，补全数据
        if (Collections3.isEmpty(list)){
            list = dealData(list, reportModelDto, account, agentType);
            list = list.stream().sorted(
                            Comparator.comparing(AgentV2WinLostReportDto::getAgentType).reversed()
                    ).collect(Collectors.toList());
            // 处理总计
            AgentV2WinLostReportDto total = getTotal(list,true, false);
            list.add(total);
            return list;
        }

        // 计算
        list = calculatte(list,true);

        // 补全数据
        list = dealData2(list, reportModelDto, account, agentType);
        list = list.stream().sorted(
                Comparator.comparing(AgentV2WinLostReportDto::getAgentType).reversed()
        ).collect(Collectors.toList());

        // 处理总计
        AgentV2WinLostReportDto total = getTotal(list,true, false);
        list.add(total);
        return list;
    }

    public PageUtils findWinLostListLevel(AgentV2WinLostReportDto reportModelDto){
        // 获得级别 0 公司(总代)/1股东/2总代/ >2 代理   -1 会员  -2 总计
        String agentTypeStr = winLoseMapper.findAgyAccountDepth(reportModelDto.getAgyAccount());
        if(StringUtils.isEmpty(agentTypeStr)){
            throw new R200Exception(reportModelDto.getAgyAccount() + "该账号不存在！！");
        }

        // 查询数据
        List<AgentV2WinLostReportDto> list = new ArrayList<>();
        PageHelper.startPage(reportModelDto.getPageNo(), reportModelDto.getPageSize());
        list = winLoseMapper.findWinLostListLevel(reportModelDto);
        // 处理数据
        if (Collections3.isEmpty(list)){
           return BeanUtil.toPagedResult(new ArrayList<AgentV2WinLostReportDto>());
        }
        // 计算
        list = calculatte(list,true);

        // 计算：股东下级代理明细 ： 总代明细股东相关字段
        if(String.valueOf(Constants.EVNumber.one).equals(agentTypeStr)){
            list = calculatteShareholder(list,reportModelDto);
        }

        return BeanUtil.toPagedResult(list);
    }

    public AgentV2WinLostReportDto findWinLostListLevelSum(AgentV2WinLostReportDto reportModelDto){
        // 获得级别 0 公司(总代)/1股东/2总代/ >2 代理   -1 会员  -2 总计
        String agentTypeStr = winLoseMapper.findAgyAccountDepth(reportModelDto.getAgyAccount());
        if(StringUtils.isEmpty(agentTypeStr)){
            throw new R200Exception(reportModelDto.getAgyAccount() + "该账号不存在！！");
        }
        // 查询数据
        List<AgentV2WinLostReportDto> list = new ArrayList<>();
        list = winLoseMapper.findWinLostListLevel(reportModelDto);
        // 处理数据
        if (Collections3.isEmpty(list)){
            return new AgentV2WinLostReportDto();
        }
        // 计算
        list = calculatte(list,true);
        // 计算：股东下级代理明细 ： 总代明细股东相关字段
        if(String.valueOf(Constants.EVNumber.one).equals(agentTypeStr)){
            list = calculatteShareholder(list,reportModelDto);
            // 处理总计
            return getTotal(list,true, true);
        }
        // 处理总计
        return getTotal(list,true,false);
    }

    public PageUtils findWinLostListLevelMbr(AgentV2WinLostReportDto reportModelDto){
        List<AgentV2WinLostReportDto> list = new ArrayList<>();
        PageHelper.startPage(reportModelDto.getPageNo(), reportModelDto.getPageSize());

        // 查询数据
        list = winLoseMapper.findWinLostListLevelMbr(reportModelDto);
        // 处理数据
        if (Collections3.isEmpty(list)){
            return BeanUtil.toPagedResult(new ArrayList<AgentV2WinLostReportDto>());
        }
        // 计算
        list = calculatte(list,false);

        return BeanUtil.toPagedResult(list);
    }

    public AgentV2WinLostReportDto findWinLostListLevelMbrSum(AgentV2WinLostReportDto reportModelDto){

        // 查询数据
        List<AgentV2WinLostReportDto> list = new ArrayList<>();
        list = winLoseMapper.findWinLostListLevelMbr(reportModelDto);
        // 处理数据
        if (Collections3.isEmpty(list)){
            return new AgentV2WinLostReportDto();
        }
        // 计算
        list = calculatte(list,false);
        // 处理总计
        return getTotal(list,false,false);
    }

    public List<AgentV2WinLostReportDto> calculatte(List<AgentV2WinLostReportDto> list, boolean revenue){
        list.stream().forEach(dto -> {
            // 真人Live  电子Slot"
            if("Live".equals(dto.getGamecategory())){
                dto.setWash(dto.getRealpeoplewash());   // 洗码比
                dto.setRevenue(dto.getRealpeople());    // 分成
            }
            if("Slot".equals(dto.getGamecategory())){
                dto.setWash(dto.getElectronicwash());
                dto.setRevenue(dto.getElectronic());
            }

            // 计算
            // 洗码佣金 = (洗码比/100)*洗码量
            dto.setWashCommission(CommonUtil.adjustScaleUp(
                    dto.getWash().divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN).multiply(dto.getValidbetTotal()))
            );
            // 总金额 = 输赢金额+洗码佣金
            dto.setTotalAmount(dto.getPayoutTotal().add(dto.getWashCommission()));
            if(revenue){
                // 本级交公司 = ((100-本级分成）/100) * 总金额
                dto.setToCompayAmount(CommonUtil.adjustScaleUp(
                        (new BigDecimal(ONE_HUNDRED).subtract(dto.getRevenue())).divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN).multiply(dto.getTotalAmount()))
                );
                // 本级交公司投注金额 = ((100-本级分成）/100) * 投注金额
                dto.setToCompayBet(CommonUtil.adjustScaleUp(
                        (new BigDecimal(ONE_HUNDRED).subtract(dto.getRevenue())).divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN).multiply(dto.getBetTotal()))
                );
                // 本级交公司投洗码量=  ((100-本级分成）/100) * 洗码量
                dto.setToCompayValidbet(CommonUtil.adjustScaleUp(
                        (new BigDecimal(ONE_HUNDRED).subtract(dto.getRevenue())).divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN).multiply(dto.getValidbetTotal()))
                );
                // 公司获利比例 = 本级交公司/本级交公司投注金额
                if(dto.getToCompayBet().compareTo(BigDecimal.ZERO) == 0){
                    dto.setCompanyProfitRratio(BigDecimal.ZERO);
                }else{
                    dto.setCompanyProfitRratio(CommonUtil.adjustScaleUp(
                            dto.getToCompayAmount().divide(dto.getToCompayBet(), 4, RoundingMode.DOWN).multiply(new BigDecimal(ONE_HUNDRED)))
                    );
                }
            }
        });
        return list;
    }

    public List<AgentV2WinLostReportDto> calculatteShareholder(List<AgentV2WinLostReportDto> list, AgentV2WinLostReportDto reportModelDto){
        // 获得该代理信息
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(reportModelDto.getAgyAccount());
        AgentAccount account = agentAccountMapper.getAgentInfo(agentAccount);
        if(Objects.isNull(account)){
            throw new R200Exception(reportModelDto.getAgyAccount() + "该账号不存在！");
        }
        list.stream().forEach(dto -> {
            // 真人Live  电子Slot"
            if("Live".equals(dto.getGamecategory())){
                dto.setWashShareholder(account.getRealpeoplewash());   // 洗码比
                dto.setRevenueShareholder(account.getRealpeople());    // 分成
            }
            if("Slot".equals(dto.getGamecategory())){
                dto.setWashShareholder(account.getElectronicwash());
                dto.setRevenueShareholder(account.getElectronic());
            }

            // 计算
            // 洗码佣金 = (洗码比/100)*洗码量
            dto.setWashCommissionShareholder(CommonUtil.adjustScaleUp(
                    dto.getWashShareholder().divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN).multiply(dto.getValidbetTotal()))
            );
            // 总金额 = 输赢金额+洗码佣金
            dto.setTotalAmountShareholder(dto.getPayoutTotal().add(dto.getWashCommissionShareholder()));
            // 本级交公司 = ((100-本级分成）/100) * 总金额
            dto.setToCompayAmountShareholder(CommonUtil.adjustScaleUp(
                    (new BigDecimal(ONE_HUNDRED).subtract(dto.getRevenueShareholder())).divide(new BigDecimal(ONE_HUNDRED), 4, RoundingMode.DOWN).multiply(dto.getTotalAmountShareholder()))
            );
        });
        return list;
    }

    public AgentV2WinLostReportDto getTotal(List<AgentV2WinLostReportDto>  list , boolean revenue, boolean shareholder){
        BigDecimal betTotal = BigDecimal.ZERO;              // 投注金额
        BigDecimal payoutTotal = BigDecimal.ZERO;           // 输赢金额
        BigDecimal validbetTotal = BigDecimal.ZERO;         // 洗码量
        BigDecimal washCommission = BigDecimal.ZERO;        // 洗码佣金
        BigDecimal totalAmount = BigDecimal.ZERO;           // 总金额
        BigDecimal toCompayAmount = BigDecimal.ZERO;        // 本级交公司
        BigDecimal toCompayBet = BigDecimal.ZERO;           // 本级交公司投注金额
        BigDecimal toCompayValidbet = BigDecimal.ZERO;      // 本级交公司投洗码量
        BigDecimal companyProfitRratio = BigDecimal.ZERO;   // 公司获利比例
        // 股东下级代理明细 ： 总代明细股东相关字段
        BigDecimal washCommissionShareholder = BigDecimal.ZERO;        // 股东洗码佣金
        BigDecimal totalAmountShareholder = BigDecimal.ZERO;           // 股东总金额
        BigDecimal toCompayAmountShareholder = BigDecimal.ZERO;        // 股东本级交公司

        for(AgentV2WinLostReportDto report : list){
            betTotal = betTotal.add(report.getBetTotal());
            payoutTotal = payoutTotal.add(report.getPayoutTotal());
            validbetTotal = validbetTotal.add(report.getValidbetTotal());
            washCommission = washCommission.add(report.getWashCommission());
            totalAmount = totalAmount.add(report.getTotalAmount());
            if(revenue){
                toCompayAmount = toCompayAmount.add(report.getToCompayAmount());
                toCompayBet = toCompayBet.add(report.getToCompayBet());
                toCompayValidbet = toCompayValidbet.add(report.getToCompayValidbet());
            }
            // 股东下级代理明细 ： 总代明细股东相关字段
            if(shareholder){
                washCommissionShareholder = washCommissionShareholder.add(report.getWashCommissionShareholder());
                totalAmountShareholder = totalAmountShareholder.add(report.getTotalAmountShareholder());
                toCompayAmountShareholder = toCompayAmountShareholder.add(report.getToCompayAmountShareholder());
            }
        }

        if(revenue && toCompayBet.compareTo(BigDecimal.ZERO) > 0){
            companyProfitRratio = CommonUtil.adjustScaleUp(
                    toCompayAmount.divide(toCompayBet ,4, RoundingMode.DOWN).multiply(new BigDecimal(ONE_HUNDRED))
            );
        }

        AgentV2WinLostReportDto dto = new AgentV2WinLostReportDto();
        dto.setAgentType(-2);
        dto.setBetTotal(betTotal);
        dto.setPayoutTotal(payoutTotal);
        dto.setValidbetTotal(validbetTotal);
        dto.setWashCommission(washCommission);
        dto.setTotalAmount(totalAmount);
        dto.setToCompayAmount(toCompayAmount);
        dto.setToCompayBet(toCompayBet);
        dto.setToCompayValidbet(toCompayValidbet);
        dto.setCompanyProfitRratio(companyProfitRratio);
        // 股东下级代理明细 ： 总代明细股东相关字段
        dto.setWashCommissionShareholder(washCommissionShareholder);
        dto.setTotalAmountShareholder(totalAmountShareholder);
        dto.setToCompayAmountShareholder(toCompayAmountShareholder);

        return dto;
    }

    public Integer findWinLostReportListMbrSum(AgentV2WinLostReportDto reportModelDto){
        // 查询数据
        return winLoseMapper.findWinLostReportListMbrSum(reportModelDto);
    }

    public AgentV2WinLostReportDto findWinLostReportListSum(AgentV2WinLostReportDto reportModelDto){
        // 查询数据
        AgentV2WinLostReportDto dto = winLoseMapper.findWinLostReportListSum(reportModelDto);
        return dto;
    }

    private List<AgentV2WinLostReportDto> dealData(List<AgentV2WinLostReportDto>  list, AgentV2WinLostReportDto reportModelDto,
                                                        AgentAccount account, Integer agentType){
        // 真人
        if(Objects.isNull(reportModelDto.getCatCodes()) || reportModelDto.getCatCodes().size() == 0
                || ( Objects.nonNull(reportModelDto.getCatCodes()) && reportModelDto.getCatCodes().size() > 0 && reportModelDto.getCatCodes().contains("Live"))){
            // 代理--真人
            AgentV2WinLostReportDto agentReal = new AgentV2WinLostReportDto();
            agentReal.setAgentType(agentType);  // 级别
            agentReal.setUserName(reportModelDto.getAgyAccount());  // 用户名称
            agentReal.setGamecategory("Live");  // 类别
            agentReal.setWash(account.getRealpeoplewash()); // 洗码比
            agentReal.setRevenue(account.getRealpeople());  // 分成

            agentReal.setBetTotal(BigDecimal.ZERO);
            agentReal.setPayoutTotal(BigDecimal.ZERO);
            agentReal.setValidbetTotal(BigDecimal.ZERO);
            agentReal.setWashCommission(BigDecimal.ZERO);
            agentReal.setTotalAmount(BigDecimal.ZERO);
            agentReal.setToCompayAmount(BigDecimal.ZERO);
            agentReal.setToCompayBet(BigDecimal.ZERO);
            agentReal.setToCompayValidbet(BigDecimal.ZERO);
            agentReal.setCompanyProfitRratio(BigDecimal.ZERO);
            list.add(agentReal);

            // 包括所属会员 2总代 和 >2 代理
            if(Integer.valueOf(Constants.EVNumber.two).compareTo(agentType) <= 0){
                // 会员--真人
                AgentV2WinLostReportDto mbrReal = new AgentV2WinLostReportDto();
                mbrReal.setAgentType(-1);  // 级别 -1 会员
                mbrReal.setUserName(reportModelDto.getAgyAccount());  // 用户名称
                mbrReal.setGamecategory("Live");  // 类别
                mbrReal.setWash(account.getRealpeoplewash()); // 洗码比
                mbrReal.setRevenue(account.getRealpeople());  // 分成

                mbrReal.setBetTotal(BigDecimal.ZERO);
                mbrReal.setPayoutTotal(BigDecimal.ZERO);
                mbrReal.setValidbetTotal(BigDecimal.ZERO);
                mbrReal.setWashCommission(BigDecimal.ZERO);
                mbrReal.setTotalAmount(BigDecimal.ZERO);
                mbrReal.setToCompayAmount(BigDecimal.ZERO);
                mbrReal.setToCompayBet(BigDecimal.ZERO);
                mbrReal.setToCompayValidbet(BigDecimal.ZERO);
                mbrReal.setCompanyProfitRratio(BigDecimal.ZERO);
                list.add(mbrReal);
            }
        }
        // 电子
        if(Objects.isNull(reportModelDto.getCatCodes())  || reportModelDto.getCatCodes().size() == 0
                || ( Objects.nonNull(reportModelDto.getCatCodes()) && reportModelDto.getCatCodes().size() > 0 && reportModelDto.getCatCodes().contains("Slot"))){
            // 代理--电子
            AgentV2WinLostReportDto agentElectronic = new AgentV2WinLostReportDto();
            agentElectronic.setAgentType(agentType);  // 级别
            agentElectronic.setUserName(reportModelDto.getAgyAccount());  // 用户名称
            agentElectronic.setGamecategory("Slot");  // 类别
            agentElectronic.setWash(account.getRealpeoplewash()); // 洗码比
            agentElectronic.setRevenue(account.getRealpeople());  // 分成

            agentElectronic.setBetTotal(BigDecimal.ZERO);
            agentElectronic.setPayoutTotal(BigDecimal.ZERO);
            agentElectronic.setValidbetTotal(BigDecimal.ZERO);
            agentElectronic.setWashCommission(BigDecimal.ZERO);
            agentElectronic.setTotalAmount(BigDecimal.ZERO);
            agentElectronic.setToCompayAmount(BigDecimal.ZERO);
            agentElectronic.setToCompayBet(BigDecimal.ZERO);
            agentElectronic.setToCompayValidbet(BigDecimal.ZERO);
            agentElectronic.setCompanyProfitRratio(BigDecimal.ZERO);
            list.add(agentElectronic);
            // 包括所属会员 2总代 和 >2 代理
            if(Integer.valueOf(Constants.EVNumber.two).compareTo(agentType) <= 0){
                // 会员--电子
                AgentV2WinLostReportDto mbrElectronic = new AgentV2WinLostReportDto();
                mbrElectronic.setAgentType(-1);  // 级别
                mbrElectronic.setUserName(reportModelDto.getAgyAccount());  // 用户名称
                mbrElectronic.setGamecategory("Slot");  // 类别
                mbrElectronic.setWash(account.getRealpeoplewash()); // 洗码比
                mbrElectronic.setRevenue(account.getRealpeople());  // 分成

                mbrElectronic.setBetTotal(BigDecimal.ZERO);
                mbrElectronic.setPayoutTotal(BigDecimal.ZERO);
                mbrElectronic.setValidbetTotal(BigDecimal.ZERO);
                mbrElectronic.setWashCommission(BigDecimal.ZERO);
                mbrElectronic.setTotalAmount(BigDecimal.ZERO);
                mbrElectronic.setToCompayAmount(BigDecimal.ZERO);
                mbrElectronic.setToCompayBet(BigDecimal.ZERO);
                mbrElectronic.setToCompayValidbet(BigDecimal.ZERO);
                mbrElectronic.setCompanyProfitRratio(BigDecimal.ZERO);
                list.add(mbrElectronic);
            }
        }

        return list;

    }

    private List<AgentV2WinLostReportDto> dealData2(List<AgentV2WinLostReportDto>  list, AgentV2WinLostReportDto reportModelDto,
                                                   AgentAccount account, Integer agentType){
        // 真人Live  电子Slot"
        boolean realMbrflg = false;
        boolean realAgyflg = false;
        boolean electronicMbrflag = false;
        boolean electronicAgyflag = false;
        for(AgentV2WinLostReportDto dto : list){
            if("Live".equals(dto.getGamecategory()) && dto.getAgentType().equals(-1)){
                realMbrflg = true;
            }
            if("Live".equals(dto.getGamecategory()) && !dto.getAgentType().equals(-1)){
                realAgyflg = true;
            }
            if("Slot".equals(dto.getGamecategory()) && dto.getAgentType().equals(-1)){
                electronicMbrflag = true;
            }
            if("Slot".equals(dto.getGamecategory())&& !dto.getAgentType().equals(-1)){
                electronicAgyflag = true;
            }
        }

        // 真人
        if(Objects.isNull(reportModelDto.getCatCodes())  || reportModelDto.getCatCodes().size() == 0
                || ( Objects.nonNull(reportModelDto.getCatCodes()) && reportModelDto.getCatCodes().size() > 0 && reportModelDto.getCatCodes().contains("Live"))){
            if(!realAgyflg){
                // 代理--真人
                AgentV2WinLostReportDto agentReal = new AgentV2WinLostReportDto();
                agentReal.setAgentType(agentType);  // 级别
                agentReal.setUserName(reportModelDto.getAgyAccount());  // 用户名称
                agentReal.setGamecategory("Live");  // 类别
                agentReal.setWash(account.getRealpeoplewash()); // 洗码比
                agentReal.setRevenue(account.getRealpeople());  // 分成

                agentReal.setBetTotal(BigDecimal.ZERO);
                agentReal.setPayoutTotal(BigDecimal.ZERO);
                agentReal.setValidbetTotal(BigDecimal.ZERO);
                agentReal.setWashCommission(BigDecimal.ZERO);
                agentReal.setTotalAmount(BigDecimal.ZERO);
                agentReal.setToCompayAmount(BigDecimal.ZERO);
                agentReal.setToCompayBet(BigDecimal.ZERO);
                agentReal.setToCompayValidbet(BigDecimal.ZERO);
                agentReal.setCompanyProfitRratio(BigDecimal.ZERO);
                list.add(agentReal);
            }
            if(!realMbrflg){
                // 会员--真人
                AgentV2WinLostReportDto mbrReal = new AgentV2WinLostReportDto();
                mbrReal.setAgentType(-1);  // 级别 -1 会员
                mbrReal.setUserName(reportModelDto.getAgyAccount());  // 用户名称
                mbrReal.setGamecategory("Live");  // 类别
                mbrReal.setWash(account.getRealpeoplewash()); // 洗码比
                mbrReal.setRevenue(account.getRealpeople());  // 分成

                mbrReal.setBetTotal(BigDecimal.ZERO);
                mbrReal.setPayoutTotal(BigDecimal.ZERO);
                mbrReal.setValidbetTotal(BigDecimal.ZERO);
                mbrReal.setWashCommission(BigDecimal.ZERO);
                mbrReal.setTotalAmount(BigDecimal.ZERO);
                mbrReal.setToCompayAmount(BigDecimal.ZERO);
                mbrReal.setToCompayBet(BigDecimal.ZERO);
                mbrReal.setToCompayValidbet(BigDecimal.ZERO);
                mbrReal.setCompanyProfitRratio(BigDecimal.ZERO);
                list.add(mbrReal);
            }
        }
        // 电子
        if(Objects.isNull(reportModelDto.getCatCodes()) || reportModelDto.getCatCodes().size() == 0
                || ( Objects.nonNull(reportModelDto.getCatCodes()) && reportModelDto.getCatCodes().size() > 0 && reportModelDto.getCatCodes().contains("Slot"))){
            if(!electronicAgyflag){
                // 代理--电子
                AgentV2WinLostReportDto agentElectronic = new AgentV2WinLostReportDto();
                agentElectronic.setAgentType(agentType);  // 级别
                agentElectronic.setUserName(reportModelDto.getAgyAccount());  // 用户名称
                agentElectronic.setGamecategory("Slot");  // 类别
                agentElectronic.setWash(account.getRealpeoplewash()); // 洗码比
                agentElectronic.setRevenue(account.getRealpeople());  // 分成

                agentElectronic.setBetTotal(BigDecimal.ZERO);
                agentElectronic.setPayoutTotal(BigDecimal.ZERO);
                agentElectronic.setValidbetTotal(BigDecimal.ZERO);
                agentElectronic.setWashCommission(BigDecimal.ZERO);
                agentElectronic.setTotalAmount(BigDecimal.ZERO);
                agentElectronic.setToCompayAmount(BigDecimal.ZERO);
                agentElectronic.setToCompayBet(BigDecimal.ZERO);
                agentElectronic.setToCompayValidbet(BigDecimal.ZERO);
                agentElectronic.setCompanyProfitRratio(BigDecimal.ZERO);
                list.add(agentElectronic);
            }
            if(!electronicMbrflag){
                // 会员--电子
                AgentV2WinLostReportDto mbrElectronic = new AgentV2WinLostReportDto();
                mbrElectronic.setAgentType(-1);  // 级别
                mbrElectronic.setUserName(reportModelDto.getAgyAccount());  // 用户名称
                mbrElectronic.setGamecategory("Slot");  // 类别
                mbrElectronic.setWash(account.getRealpeoplewash()); // 洗码比
                mbrElectronic.setRevenue(account.getRealpeople());  // 分成

                mbrElectronic.setBetTotal(BigDecimal.ZERO);
                mbrElectronic.setPayoutTotal(BigDecimal.ZERO);
                mbrElectronic.setValidbetTotal(BigDecimal.ZERO);
                mbrElectronic.setWashCommission(BigDecimal.ZERO);
                mbrElectronic.setTotalAmount(BigDecimal.ZERO);
                mbrElectronic.setToCompayAmount(BigDecimal.ZERO);
                mbrElectronic.setToCompayBet(BigDecimal.ZERO);
                mbrElectronic.setToCompayValidbet(BigDecimal.ZERO);
                mbrElectronic.setCompanyProfitRratio(BigDecimal.ZERO);
                list.add(mbrElectronic);
            }
        }

        return list;

    }

    public String getBetLastDate(String siteCode){
        String betLastDateStr;
        try {
            betLastDateStr=analysisService.getBetLastDate(siteCode);
        } catch (Exception e) {
            log.error("getBetLastDate==" + e);
            throw new RRException("查询异常!");
        }
        return betLastDateStr;
    }

    public SysFileExportRecord exportWinLostReportList(AgentV2WinLostReportDto reportModelDto, Long userId, String module, String templatePath){
        // 生成文件导出记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);

        if (null != record) {
            List<AgentV2WinLostReportDto> winLostReportlist = findWinLostReportList(reportModelDto);
//            if (winLostReportlist.size() > 10000) {
//                throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
//            }
            List<Map<String, Object>> list = winLostReportlist.stream().map(e -> {
                dealAndTransData(e);
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();

            String lastBetTime= getBetLastDate(siteCode);

            Map<String,Object> map = new HashMap<>(8);
            map.put("startTime",reportModelDto.getStartTime());
            map.put("endTime",reportModelDto.getEndTime());
            map.put("updateTime",lastBetTime);
            map.put("mapList",list);

            sysFileExportRecordService.exportExcel(templatePath,map,userId,module,siteCode);//异步执行
        }
        return record;
    }

    public List<AgentV2WinLostReportDto> exportWinLostListLevelData(AgentV2WinLostReportDto reportModelDto){
        // 获得级别 0 公司(总代)/1股东/2总代/ >2 代理   -1 会员  -2 总计
        String agentTypeStr = winLoseMapper.findAgyAccountDepth(reportModelDto.getAgyAccount());
        if(StringUtils.isEmpty(agentTypeStr)){
            throw new R200Exception(reportModelDto.getAgyAccount() + "该账号不存在！！");
        }
        // 查询数据
        List<AgentV2WinLostReportDto> list = new ArrayList<>();
        list = winLoseMapper.findWinLostListLevel(reportModelDto);
        // 处理数据
        if (Collections3.isEmpty(list)){
            return new ArrayList<AgentV2WinLostReportDto>();
        }
        // 计算
        list = calculatte(list,true);
        // 计算：股东下级代理明细 ： 总代明细股东相关字段
        if(String.valueOf(Constants.EVNumber.one).equals(agentTypeStr)){
            list = calculatteShareholder(list,reportModelDto);
            // 处理总计
            AgentV2WinLostReportDto total = getTotal(list,true, true);
            list.add(total);
            return list;
        }
        // 处理总计
        AgentV2WinLostReportDto total = getTotal(list,true, false);
        list.add(total);
        return list;
    }

    public SysFileExportRecord exportWinLostListLevel(AgentV2WinLostReportDto reportModelDto, Long userId, String module, String templatePath){
        // 生成文件导出记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);

        if (null != record) {
            List<AgentV2WinLostReportDto> winLostReportlist =  exportWinLostListLevelData(reportModelDto);
//            if (winLostReportlist.size() > 10000) {
//                throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
//            }
            List<Map<String, Object>> list = winLostReportlist.stream().map(e -> {
                dealAndTransData(e);
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();

            String lastBetTime= getBetLastDate(siteCode);

            Map<String,Object> map = new HashMap<>(8);
            map.put("startTime",reportModelDto.getStartTime());
            map.put("endTime",reportModelDto.getEndTime());
            map.put("updateTime",lastBetTime);
            map.put("mapList",list);

            sysFileExportRecordService.exportExcel(templatePath,map,userId,module,siteCode);//异步执行
        }
        return record;
    }

    public List<AgentV2WinLostReportDto> exportWinLostListLevelMbrData(AgentV2WinLostReportDto reportModelDto){

        // 查询数据
        List<AgentV2WinLostReportDto> list = new ArrayList<>();
        list = winLoseMapper.findWinLostListLevelMbr(reportModelDto);
        // 处理数据
        if (Collections3.isEmpty(list)){
            return new ArrayList<AgentV2WinLostReportDto>();
        }
        // 计算
        list = calculatte(list,false);
        // 处理总计
        AgentV2WinLostReportDto total = getTotal(list,false,false);
        list.add(total);
        return list;
    }

    public SysFileExportRecord exportWinLostListLevelMbr(AgentV2WinLostReportDto reportModelDto, Long userId, String module, String templatePath){
        // 生成文件导出记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);

        if (null != record) {
            List<AgentV2WinLostReportDto> winLostReportlist = exportWinLostListLevelMbrData(reportModelDto);
//            if (winLostReportlist.size() > 10000) {
//                throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
//            }
            List<Map<String, Object>> list = winLostReportlist.stream().map(e -> {
                dealAndTransData(e);
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();

            String lastBetTime= getBetLastDate(siteCode);

            Map<String,Object> map = new HashMap<>(8);
            map.put("startTime",reportModelDto.getStartTime());
            map.put("endTime",reportModelDto.getEndTime());
            map.put("updateTime",lastBetTime);
            map.put("mapList",list);

            sysFileExportRecordService.exportExcel(templatePath,map,userId,module,siteCode);//异步执行
        }
        return record;
    }

    private void dealAndTransData(AgentV2WinLostReportDto dto){
        // 级别： 0 公司(总代)/1股东/2总代/ >2 代理  -1 会员 -2汇总
         if(Integer.valueOf(0).equals(dto.getAgentType())) {
             dto.setAgentTypeStr("公司");
         } else if(Integer.valueOf(1).equals(dto.getAgentType())) {
             dto.setAgentTypeStr("股东");
         } else if(Integer.valueOf(2).equals(dto.getAgentType())) {
             dto.setAgentTypeStr("总代");
         } else if(Integer.valueOf(-1).equals(dto.getAgentType())) {
             dto.setAgentTypeStr("会员");
         }else if(Integer.valueOf(-2).equals(dto.getAgentType())) {
             dto.setAgentTypeStr("总计");
         }else{
             dto.setAgentTypeStr("代理");
         }
         // 类别：gamecategory 真人Live  电子Slot
        if("Live".equals(dto.getGamecategory())) {
            dto.setGamecategory("真人");
        } else if("Slot".equals(dto.getGamecategory())) {
            dto.setGamecategory("电子");
        }
    }
}
