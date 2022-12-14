package com.wsdy.saasops.modules.agent.service;

import static com.wsdy.saasops.common.constants.Constants.ONE_HUNDRED;
import static com.wsdy.saasops.common.utils.CommonUtil.generateString;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_6_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_DD_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentContracttMapper;
import com.wsdy.saasops.modules.agent.dao.AgyCommissionDepotMapper;
import com.wsdy.saasops.modules.agent.dao.AgyCommissionMapper;
import com.wsdy.saasops.modules.agent.dao.AgyCommissionProfitMapper;
import com.wsdy.saasops.modules.agent.dto.AgentContractDto;
import com.wsdy.saasops.modules.agent.dto.CommissionCastDto;
import com.wsdy.saasops.modules.agent.dto.GroupDepotDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentContract;
import com.wsdy.saasops.modules.agent.entity.AgyCommission;
import com.wsdy.saasops.modules.agent.entity.AgyCommissionDepot;
import com.wsdy.saasops.modules.agent.entity.AgyCommissionProfit;
import com.wsdy.saasops.modules.agent.entity.AgyTree;
import com.wsdy.saasops.modules.agent.mapper.CommissionCastMapper;

import lombok.extern.slf4j.Slf4j;

/**
 *		??????????????????job
 */
@Slf4j
@Service
public class CommissionCastService {

    @Autowired
    private CommissionCastMapper commissionCastMapper;
    @Autowired
    private AgentContracttMapper contracttMapper;
    @Autowired
    private AgyCommissionProfitMapper commissionProfitMapper;
    @Autowired
    private AgyCommissionDepotMapper commissionDepotMapper;
    @Autowired
    private AgyCommissionMapper commissionMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private RedisService redisService;


    /**
     * 	??????????????????job
     * 
     * @param siteCode
     * @param date
     */
    public void calculateCommission(String siteCode, String date) {
        String dd = getCurrentDate(FORMAT_DD_DATE);
        if (!date.equals(dd)) {
            log.info(dd + "????????????????????????" + date);
            return;
        }
        int count = contracttMapper.selectCount(null);
        if (count == 0) {
            log.info(siteCode + "???????????????????????????");
            return;
        }
        // ??????????????????
        List<AgentAccount> accounts = commissionCastMapper.findAgentCommission();
        if (Collections3.isNotEmpty(accounts)) {
            log.info("??????:{} ????????????????????????", siteCode);
            // ???????????????????????????
            accounts.stream().forEach(as -> {
            	if (as.getRebateratio() == null) {
            		as.setRebateratio(BigDecimal.ZERO);
            	}
            	if (as.getFirstagentratio() == null) {
            		as.setFirstagentratio(BigDecimal.ZERO);
            	}
            	startCalculating(as, siteCode);
            });
        }
    }

    /**
     * 	????????????????????????
     * 
     * @param agentAccount
     * @param siteCode
     */
    @Transactional
    @Async("commissionCastAsyncExecutor")
    public void startCalculating(AgentAccount agentAccount, String siteCode) {
    	log.info("calculateCommission------??????????????????:{}, ??????????????????:{}", siteCode, agentAccount.getAgyAccount());
        String key = RedisConstants.AGENT_COMM_CAST + siteCode + agentAccount.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, agentAccount.getAgyAccount(), 10, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                ThreadLocalCache.setSiteCodeAsny(siteCode);
                AgyCommission agyCommission = new AgyCommission();
                agyCommission.setAgyAccount(agentAccount.getAgyAccount());
                agyCommission.setTime(getCurrentDate(FORMAT_6_DATE));
                agyCommission.setType(Constants.EVNumber.zero);
                int count = commissionMapper.selectCount(agyCommission);
                if (count > 0) {
                    log.info("calculateCommission------??????:{},??????:{}????????????????????????,??????????????????", siteCode, agentAccount.getAgyAccount());
                    return;
                }

                Integer contractId = agentAccount.getContractId();
                if (isNull(contractId)) {
                    contractId = Constants.EVNumber.one;
                }
                String contractStart = agentAccount.getContractStart();
                String contractEnd = agentAccount.getContractEnd();
                if (!StringUtils.isEmpty(contractStart)) {
                    int num = DateUtil.timeCompare(getCurrentDate(FORMAT_18_DATE_TIME), contractStart, DateUtil.FORMAT_18_DATE_TIME);
                    if (num == -1) {
                        contractId = Constants.EVNumber.one;
                    }
                }
                if (!StringUtils.isEmpty(contractEnd)) {
                    int num = DateUtil.timeCompare(contractEnd, getCurrentDate(FORMAT_18_DATE_TIME), DateUtil.FORMAT_18_DATE_TIME);
                    if (num == -1) {
                        contractId = Constants.EVNumber.one;
                    }
                }
                AgentContract contract = contracttMapper.selectByPrimaryKey(contractId);
                if ((isNull(contract) || StringUtils.isEmpty(contract.getRule()))) {
                    log.info("calculateCommission------??????:{},??????:{}?????????????????????????????????", siteCode, agentAccount.getAgyAccount());
                    return;
                }
                String orderNo = new SnowFlake().nextId() + generateString(4);

                String startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//???????????????
                String endTime = DateUtil.getEndOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 1);//??????????????????

                CommissionCastDto dto = commissionCastMapper.sumValidbet(startTime, endTime, agentAccount.getId(), null);
                log.info("calculateCommission------??????:" + siteCode ,"?????????"+ agentAccount.getAgyAccount() + "???????????????????????????" + JSON.toJSONString(dto));
                // ?????????
                BigDecimal depotCost = BigDecimal.ZERO;
                // ?????????
                BigDecimal serviceCost = BigDecimal.ZERO;
                
                // ???????????????  1????????????  2???????????? 3 ??????
                // ???????????? = ?????? - ?????? + ??????????????????
                if (agentAccount.getFeeModel() == 3) {
                	// ?????????
                	serviceCost = commissionCastMapper.findChargCostForSingle(agentAccount.getId(), null, null, startTime, endTime);
                	// ?????????
                	depotCost = depotCost(startTime, endTime, agentAccount, orderNo);
                	// ???????????? = ????????? +???????????? * ????????????????????????
                	BigDecimal additionalServicerate = agentAccount.getAdditionalServicerate().divide(new BigDecimal("100"));
                	depotCost = depotCost.add(depotCost.multiply(additionalServicerate).setScale(2, BigDecimal.ROUND_HALF_DOWN).setScale(2, BigDecimal.ROUND_DOWN));
                } else {
                	if (nonNull(agentAccount.getFeeModel()) && agentAccount.getFeeModel() == Constants.EVNumber.two) {
                		// 2????????????
                		serviceCost = commissionCastMapper.findChargCostForSingle(agentAccount.getId(), null, null, startTime, endTime);
                	} else {
                		// 1????????????
                		depotCost = depotCost(startTime, endTime, agentAccount, orderNo);
                		// ????????????  = ????????? +???????????? * ????????????????????????
                    	BigDecimal additionalServicerate = agentAccount.getAdditionalServicerate().divide(new BigDecimal("100"));
                    	depotCost = depotCost.add(depotCost.multiply(additionalServicerate).setScale(2, BigDecimal.ROUND_HALF_DOWN).setScale(2, BigDecimal.ROUND_DOWN));
                	}
                }
                // ?????????????????????????????????
                BigDecimal calculateProfit = commissionCastMapper.getCalculateProfitOfAgent(agentAccount.getId(), startTime, endTime);
                // ?????? + ????????? + ????????? + ????????????
                BigDecimal bonusamount = dto.getBonusamount().add(dto.getTaskBonusamount()).add(depotCost).add(serviceCost).add(calculateProfit);

                // ??????????????????????????????0?????????????????????
                if (isNull(dto) || dto.getTotalPayout().compareTo(BigDecimal.ZERO) != -1) {
                	// ???????????????????????????0
                    if (nonNull(dto) && dto.getTotalPayout().compareTo(BigDecimal.ZERO) == 1) {
                        Integer countPro = commissionCastMapper.agyCommissionProfitCount(agentAccount.getId(), getCurrentDate(FORMAT_6_DATE));
                        log.info("calculateCommission------??????:" + siteCode + "?????????" + agentAccount.getAgyAccount() + "????????????????????????????????????" + countPro);
                        // ????????????????????????????????????????????????????????????????????????
                        if (nonNull(countPro) && countPro == 0) {
                            AgyCommissionProfit commissionProfit = getCommissionProfit(agentAccount);
                            // ???????????? + ???????????????????????? + ?????? + ????????? + ????????? + ???????????? = ?????????????????????
                            commissionProfit.setNetwinlose(dto.getTotalPayout().add(commissionProfit.getNetwinlose()));
                            commissionProfit.setNetwinlose(commissionProfit.getNetwinlose().add(bonusamount));
                            updateCommissionProfit(commissionProfit);
                        }
                    }
                    log.info("calculateCommission------??????:{},??????:{}???????????????????????????0,??????????????????", siteCode, agentAccount.getAgyAccount());
                    return;
                }
                dto.setTotalPayout(dto.getTotalPayout().negate());
                // ?????????????????? ?????? ????????? + ????????? + ????????? + ???????????? ???
                BigDecimal netwinlose = dto.getTotalPayout().subtract(bonusamount);

                log.info("calculateCommission------??????:" + siteCode + "?????????" + agentAccount.getAgyAccount() + "???????????????????????????" + netwinlose);
                // 0???????????????????????? : ???????????????0 ?????????????????????
                if (BigDecimal.ZERO.compareTo(netwinlose) != -1) {
                    AgyCommissionProfit commissionProfit = getCommissionProfit(agentAccount);
                    // ????????? ????????? + ???????????????????????? = ?????????????????????
                    commissionProfit.setNetwinlose(commissionProfit.getNetwinlose().add(netwinlose.negate()));
                    updateCommissionProfit(commissionProfit);
                    log.info("calculateCommission------??????:" + siteCode + "?????????" + agentAccount.getAgyAccount() + "????????????????????????????????????" + netwinlose + "????????????");
                    return;
                }

                AgentContractDto contractDto = new AgentContractDto();
                if (isNull(dto) || dto.getValidbet().compareTo(contract.getValidbetmax()) == -1) {
                    log.info("calculateCommission------??????:" + siteCode + "?????????" + agentAccount.getAgyAccount() + "????????????????????????????????????" + contract.getValidbetmax() + "????????????");
                    return;
                }
                contractDto = getContract(contract, netwinlose, dto);
                if (isNull(contractDto)) {
                    log.info("calculateCommission------??????:" + siteCode + "?????????" + agentAccount.getAgyAccount() + "????????????????????????????????????????????????");
                    return;
                }

                BigDecimal waterBigDecimal = commissionCastMapper.findAccountWater(agentAccount.getId(), startTime, endTime);
                dto.setWaterBigDecimal(waterBigDecimal);

                AgyCommissionProfit commissionProfit = getCommissionProfit(agentAccount);
                
                // ?????? ???????????????????????? ?????? 0
                if (commissionProfit.getNetwinlose().compareTo(BigDecimal.ZERO) == 1) {
                	// ?????????????????????  ?????? ????????????????????????
                    BigDecimal winlose = netwinlose.subtract(commissionProfit.getNetwinlose());
                    if (winlose.compareTo(BigDecimal.ZERO) != 1) {
                        commissionProfit.setNetwinlose(winlose.negate());
                        updateCommissionProfit(commissionProfit);
                        addAgyCommission(agentAccount, dto, orderNo, startTime, endTime,
                                netwinlose, netwinlose, BigDecimal.ZERO, BigDecimal.ZERO,
                                Constants.EVNumber.zero, null, dto.getBonusamount(),
                                dto.getTaskBonusamount(), depotCost, serviceCost, Boolean.TRUE);
                        return;
                    } else {
                    	// ?????????????????????  ?????? ????????????????????????
                        commissionProfit.setNetwinlose(BigDecimal.ZERO);
                        updateCommissionProfit(commissionProfit);
                        BigDecimal witeOff = netwinlose.subtract(winlose);
                        generateCommission(agentAccount, dto, winlose,
                                orderNo, startTime, endTime, contractDto, contract, witeOff, depotCost, serviceCost);
                    }
                } else {
                    generateCommission(agentAccount, dto, netwinlose, orderNo, startTime,
                            endTime, contractDto, contract, BigDecimal.ZERO, depotCost, serviceCost);
                }
                log.info("calculateCommission------??????:{} ??????????????????????????????:{}", siteCode, agentAccount.getAgyAccount());
            } catch(Exception e) {
            	log.error("calculateCommission????????????------??????" + siteCode + "??????????????????job??????", e);
            	throw e;
            } finally {
                redisService.del(key);
            }
        }
    }

    private void generateCommission(AgentAccount agentAccount, CommissionCastDto dto, BigDecimal netwinlose,
                                    String orderNo, String startTime, String endTime, AgentContractDto contractDto,
                                    AgentContract contract, BigDecimal witeOff, BigDecimal depotCost, BigDecimal serviceCost) {
        BigDecimal bigDecimal = CommonUtil.adjustScale(netwinlose.multiply(
                contractDto.getCommissionRate().divide(new BigDecimal(ONE_HUNDRED))));
        addAgyCommission(agentAccount, dto, orderNo, startTime, endTime,
                netwinlose, witeOff, contractDto.getCommissionRate(), bigDecimal,
                Constants.EVNumber.zero, null, dto.getBonusamount(), dto.getTaskBonusamount(),
                depotCost, serviceCost, Boolean.FALSE);
      /*  if (agentAccount.getFeeModel() != Constants.EVNumber.one) {
            return;
        }*/
        if (agentAccount.getAgentType() > Constants.EVNumber.one) {
            List<AgyTree> agyTreeList = commissionCastMapper.findAgyTree(agentAccount.getId());
            if (agyTreeList.size() == 1) {
                BigDecimal rate =  agentAccount.getRate() == null ? agentAccount.getRebateratio(): new BigDecimal(agentAccount.getRate());
                BigDecimal rebigDecimal = CommonUtil.adjustScale(netwinlose.multiply(
                        rate.divide(new BigDecimal(ONE_HUNDRED))));
                if (rebigDecimal.compareTo(BigDecimal.ZERO) == 1) {
                    AgyTree agyTree = agyTreeList.get(0);
                    AgentAccount agentAccount1 = agentAccountMapper.selectByPrimaryKey(agyTree.getParentId());
                    addAgyCommission(agentAccount1, dto, orderNo, startTime, endTime,
                            netwinlose, BigDecimal.ZERO, rate, rebigDecimal,
                            Constants.EVNumber.one, agentAccount, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE);
                }
            }
            if (agyTreeList.size() == 2) {
                BigDecimal rate =  agentAccount.getRate() == null ? agentAccount.getFirstagentratio(): new BigDecimal(agentAccount.getRate());
                BigDecimal firstRebigDecimal = CommonUtil.adjustScale(netwinlose.multiply(
                        rate.divide(new BigDecimal(ONE_HUNDRED))));
                if (firstRebigDecimal.compareTo(BigDecimal.ZERO) == 1) {
                    AgyTree agyTree = agyTreeList.get(1);
                    AgentAccount agentAccount1 = agentAccountMapper.selectByPrimaryKey(agyTree.getParentId());
                    addAgyCommission(agentAccount1, dto, orderNo, startTime, endTime,
                            netwinlose, BigDecimal.ZERO, rate, firstRebigDecimal,
                            Constants.EVNumber.one, agentAccount, BigDecimal.ZERO, 
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE);
                }

                BigDecimal rebigDecimal = CommonUtil.adjustScale(netwinlose.multiply(
                		agentAccount.getRebateratio().divide(new BigDecimal(ONE_HUNDRED))));
                if (rebigDecimal.compareTo(BigDecimal.ZERO) == 1) {
                    AgyTree agyTree = agyTreeList.get(0);
                    AgentAccount agentAccount1 = agentAccountMapper.selectByPrimaryKey(agyTree.getParentId());

                    addAgyCommission(agentAccount1, dto, orderNo, startTime, endTime,
                            netwinlose, BigDecimal.ZERO, agentAccount.getRebateratio(), rebigDecimal,
                            Constants.EVNumber.one, agentAccount, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, Boolean.FALSE);
                }
            }
        }
    }

    /**
     * 	????????????
     * 
     * @param contract
     * @param netwinlose
     * @param dto
     * @return
     */
    private AgentContractDto getContract(AgentContract contract, BigDecimal netwinlose, CommissionCastDto dto) {
        List<AgentContractDto> contractDtos = new Gson().fromJson(contract.getRule(),
                new TypeToken<List<AgentContractDto>>() {
                }.getType());
        Collections.sort(contractDtos, Comparator.comparing(AgentContractDto::getCommissionRate).reversed());
        for (AgentContractDto agentContractDto : contractDtos) {
            if (isNull(agentContractDto.getActivenumber()) &&
                    isNull(agentContractDto.getCommissionRate())
                    && isNull(agentContractDto.getNetprofitAmount())
                    && isNull(agentContractDto.getValidBet())) {
                continue;
            }
            Integer activenumber = isNull(agentContractDto.getActivenumber()) ? Constants.EVNumber.one : agentContractDto.getActivenumber();
            BigDecimal netprofitAmount = isNull(agentContractDto.getNetprofitAmount()) ? BigDecimal.ZERO : agentContractDto.getNetprofitAmount();
            BigDecimal validBet = isNull(agentContractDto.getValidBet()) ? BigDecimal.ZERO : agentContractDto.getValidBet();
            if (dto.getValidbet().compareTo(validBet) != -1 &&
                    dto.getUserCount() >= activenumber && netwinlose.compareTo(netprofitAmount) != -1) {
                return agentContractDto;
            }
        }
        return null;
    }

    /**
     * 	????????????????????????
     * 
     * @param agentAccount
     * @param dto
     * @param orderNo
     * @param cycleStart
     * @param cycleEnd
     * @param netwinlose
     * @param writeOff
     * @param rate
     * @param decimalCommission
     * @param type
     * @param subAgentAccount
     * @param bonusAmount
     * @param taskAmount
     * @param cost
     * @param serviceCost
     * @param isSign
     */
    private void addAgyCommission(AgentAccount agentAccount, CommissionCastDto dto,
                                  String orderNo, String cycleStart, String cycleEnd,
                                  BigDecimal netwinlose, BigDecimal writeOff,
                                  BigDecimal rate, BigDecimal decimalCommission, Integer type,
                                  AgentAccount subAgentAccount, BigDecimal bonusAmount,
                                  BigDecimal taskAmount, BigDecimal cost, BigDecimal serviceCost, Boolean isSign) {
        AgyCommission commission = new AgyCommission();
        commission.setAgyAccount(agentAccount.getAgyAccount());
        commission.setAgentId(agentAccount.getId());
        commission.setOrderNo(orderNo);
        commission.setCycleStart(cycleStart);
        commission.setCycleEnd(cycleEnd);
        commission.setActiveNum(dto.getUserCount());
        commission.setCycleBet(dto.getValidbet());
        commission.setNetwinlose(netwinlose);
        commission.setWriteOff(writeOff);
        commission.setRate(rate);
        commission.setCommission(decimalCommission);
        commission.setReviewStatus(Constants.EVNumber.two);
        commission.setIssuestatus(Constants.EVNumber.two);
        if (isSign) {
            commission.setReviewStatus(Constants.EVNumber.one);
        }
        commission.setTime(getCurrentDate(FORMAT_6_DATE));
        commission.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        commission.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        commission.setType(type);
        // ?????????
        commission.setCost(cost);
        // ?????????
        commission.setServiceCost(serviceCost);
        
        if (nonNull(subAgentAccount)) {
            commission.setSubAgyaccount(subAgentAccount.getAgyAccount());
            commission.setSubAgentId(subAgentAccount.getId());
        } else {
            commission.setPayout(dto.getTotalPayout().negate());
            commission.setRebateAmount(dto.getWaterBigDecimal());
            commission.setBonusAmount(bonusAmount.subtract(dto.getWaterBigDecimal()));
            commission.setTaskAmount(taskAmount);
            // ???????????????
            commission.setFeeModel(agentAccount.getFeeModel());
            // ?????????????????????
            commission.setDepositServicerate(agentAccount.getDepositServicerate());
            // ?????????????????????
            commission.setWithdrawServicerate(agentAccount.getWithdrawServicerate());
            // ?????????????????????
            commission.setAdditionalServicerate(agentAccount.getAdditionalServicerate());
        }
        commissionMapper.insert(commission);
    }

    private AgyCommissionProfit getCommissionProfit(AgentAccount agentAccount) {
        AgyCommissionProfit profit = new AgyCommissionProfit();
        profit.setAgentId(agentAccount.getId());
        AgyCommissionProfit commissionProfit = commissionProfitMapper.selectOne(profit);
        if (isNull(commissionProfit)) {
            AgyCommissionProfit profit1 = new AgyCommissionProfit();
            profit1.setAgyAccount(agentAccount.getAgyAccount());
            profit1.setAgentId(agentAccount.getId());
            profit1.setNetwinlose(BigDecimal.ZERO);
            profit1.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
            commissionProfitMapper.insert(profit1);
            return profit1;
        }
        return commissionProfit;
    }

    private void updateCommissionProfit(AgyCommissionProfit commissionProfit) {
        commissionProfit.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
        commissionProfitMapper.updateByPrimaryKeySelective(commissionProfit);
    }


    private BigDecimal depotCost(String startTime, String endTime, AgentAccount agentAccount, String orderNo) {
        List<GroupDepotDto> depotDtoList = commissionCastMapper.findGroupDepotPayout(startTime, endTime, agentAccount.getId(), null);
        BigDecimal depotCost = BigDecimal.ZERO;
        if (depotDtoList.size() > 0) {
            for (GroupDepotDto depotDto : depotDtoList) {
                depotCost = depotCost.add(depotDto.getWaterCost());
                if (depotDto.getPayout().compareTo(BigDecimal.ZERO) != -1 && depotCost.compareTo(BigDecimal.ZERO) != 1) {
                    continue;
                }
                AgyCommissionDepot commissionDepot = new AgyCommissionDepot();
                if (depotDto.getPayout().compareTo(BigDecimal.ZERO) == -1) {
                    depotDto.setPayout(depotDto.getPayout().negate());
                    BigDecimal rate = commissionCastMapper.findDepotRate(depotDto.getDepotId(), depotDto.getType());
                    BigDecimal cost = BigDecimal.ZERO;
                    if (nonNull(rate)) {
                        cost = CommonUtil.adjustScale(depotDto.getPayout()
                                .multiply(rate.divide(new BigDecimal(ONE_HUNDRED))));
                    }
                    depotCost = CommonUtil.adjustScale(depotCost.add(cost));
                    commissionDepot.setCost(cost);
                    commissionDepot.setRate(isNull(rate) ? BigDecimal.ZERO : rate);
                }

                commissionDepot.setWaterCost(depotDto.getWaterCost());
                commissionDepot.setValidbet(depotDto.getValidbet());
                commissionDepot.setWaterrate(depotDto.getWaterrate());
                commissionDepot.setAgyAccount(agentAccount.getAgyAccount());
                commissionDepot.setAgentId(agentAccount.getId());
                commissionDepot.setOrderNo(orderNo);
                commissionDepot.setDepotId(depotDto.getDepotId());
                commissionDepot.setPayout(depotDto.getPayout());
                commissionDepot.setCatId(depotDto.getType());
                commissionDepotMapper.insert(commissionDepot);
            }
        }
        return depotCost;
    }
}

