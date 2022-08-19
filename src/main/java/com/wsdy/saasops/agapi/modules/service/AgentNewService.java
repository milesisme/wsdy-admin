package com.wsdy.saasops.agapi.modules.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wsdy.saasops.agapi.modules.dto.DataTrendDto;
import com.wsdy.saasops.agapi.modules.dto.DataTrendParamDto;
import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentContracttMapper;
import com.wsdy.saasops.modules.agent.dao.AgentSubAccountMapper;
import com.wsdy.saasops.modules.agent.dto.AgentContractDto;
import com.wsdy.saasops.modules.agent.dto.CommissionCastDto;
import com.wsdy.saasops.modules.agent.dto.CostTotalDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentContract;
import com.wsdy.saasops.modules.agent.entity.AgentSubAccount;
import com.wsdy.saasops.modules.agent.mapper.AgentCommMapper;
import com.wsdy.saasops.modules.agent.mapper.AgentHomeMapper;
import com.wsdy.saasops.modules.agent.mapper.AgentNewMapper;
import com.wsdy.saasops.modules.agent.mapper.CommissionCastMapper;
import com.wsdy.saasops.modules.agent.mapper.DataTrendMapper;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AgentNewService {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentSubAccountMapper subAccountMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private AgentAccountService agentAccountService;
    @Autowired
    private AgentNewMapper agentNewMapper;
    @Autowired
    private AgentContracttMapper agentContracttMapper;
    @Autowired
    private DataTrendMapper dataTrendMapper;
    @Autowired
    private AgentHomeMapper agentHomeMapper;
    @Autowired
    private CommissionCastMapper commissionCastMapper;
    @Autowired
    private AgentCommMapper agentCommMapper;

    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private AgentFinaceReportService finaceReportService;

    /**
     * 代理注册
     *
     * @param agentAccount
     * @return
     */
    public Map<String, Object> agentRegister(AgentAccount agentAccount) {
        // 初始值
        String salt = RandomStringUtils.randomAlphanumeric(20);
        agentAccount.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentAccount.setCreateUser(agentAccount.getAgyAccount());
        agentAccount.setAgyPwd(new Sha256Hash(agentAccount.getAgyPwd(), salt).toHex());
        agentAccount.setSalt(salt);
        agentAccount.setAvailable(Constants.EVNumber.zero);
        agentAccount.setParentId(agentAccount.getParentId());
        agentAccount.setStatus(Constants.EVNumber.two);
        agentAccount.setRegisterSign(Constants.EVNumber.zero);
        agentAccount.setSpreadCode(agentAccountService.getSpreadCode());
        agentAccount.setModifyTime(agentAccount.getCreateTime());
        agentAccount.setModifyUser(agentAccount.getCreateUser());
        agentAccount.setAttributes(Constants.EVNumber.zero);
        agentAccount.setFeeModel(Constants.EVNumber.one);
        // 服务费存款比例
        agentAccount.setDepositServicerate(BigDecimal.ZERO);
        // 服务费取款比例
        agentAccount.setWithdrawServicerate(BigDecimal.ZERO);
        // 平台费额外比例
        agentAccount.setAdditionalServicerate(BigDecimal.ZERO);
        agentAccountMapper.insert(agentAccount);

        // 返回token
        String agentToken = jwtUtils.agentGenerateToken(String.valueOf(agentAccount.getId()));
        return ImmutableMap.of("agentToken", agentToken);
    }


    public Map<String, Object> agentAccountLogin(AgentAccount account) {
        Map<String, Object> map = new HashMap<>(8);
        AgentAccount account1 = checkoutAvailable(account.getAgyAccount());
        if (isNull(account1)) {
            throw new R200Exception("代理账号不存在或异常!");
        }
        if (Constants.EVNumber.two == account1.getStatus()) {
            throw new R200Exception("账号正在审核中");
        }
        if (Constants.EVNumber.zero == account1.getAvailable()) {
            throw new R200Exception("对不起，帐号已禁用，请联系在线客服");
        }
        String logAgyPwd = new Sha256Hash(account.getAgyPwd(), account1.getSalt()).toHex();
        if (!account1.getAgyPwd().equals(logAgyPwd)) {
            throw new R200Exception("密码有误,请重新输入!");
        }
        map.put("realName", account1.getRealName());
        map.put("attributes", account1.getAttributes());
        map.put("agyAccount", account1.getAgyAccount());
        map.put("agyAccountId", account1.getId());
        String agentToken = jwtUtils.agentGenerateToken(account1.getId()
                + ":" + account1.getAttributes() + ":" + account1.getAgyAccount());
        // 只允许一个设备登录
        apiUserService.updateAgentLoginTokenCache(CommonUtil.getSiteCode(), account.getAgyAccount(), agentToken);

        map.put("agentToken", agentToken);
        return map;
    }

    public AgentAccount checkoutAvailable(String agyAccount) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(agyAccount);
        AgentAccount account = agentAccountMapper.selectOne(agentAccount);
        if (isNull(account)) {
            AgentSubAccount subAccount = new AgentSubAccount();
            subAccount.setAgyAccount(agyAccount);
            AgentSubAccount subAccount1 = subAccountMapper.selectOne(subAccount);
            if (nonNull(subAccount1)) {
                account = new AgentAccount();
                account.setAgyAccount(subAccount1.getAgyAccount());
                account.setAttributes(Constants.EVNumber.four); //子账号
                account.setId(subAccount1.getId());
                account.setAvailable(Constants.EVNumber.one);
                account.setAgyPwd(subAccount1.getAgyPwd());
                account.setSalt(subAccount1.getSalt());
                account.setAgentId(subAccount1.getAgentId());
                account.setRealName(subAccount1.getRealName());
                account.setStatus(Constants.EVNumber.one);
            }
        }
        return account;
    }

    public void checkoutAgentMobile(String mobile) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setMobile(mobile);
        int count = agentAccountMapper.selectCount(agentAccount);
        if (count > 0) {
            throw new R200Exception("手机号码已存在");
        }
    }

    public void checkoutUsername(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        int acount = accountMapper.selectCount(account);
        if (acount == 0) {
            throw new R200Exception("会员账号不存在");
        }
        AgentAccount agentAccount = checkoutAvailable(loginName);
        if (nonNull(agentAccount) && StringUtil.isNotEmpty(agentAccount.getAgyAccount())) {
            throw new R200Exception("代理账号已经存在");
        }
    }

    /**
     * 	代理接口-首页总览
     * 
     * @param startTime
     * @param endTime
     * @param agentAccount
     * @return
     */
    public Map<String, Object> agent0verview(String startTime, String endTime, AgentAccount agentAccount) {
        AgentAccount account = null;
        if (agentAccount.getAttributes() == 4) {
            account = agentAccountMapper.selectByPrimaryKey(agentAccount.getAgentId());
        } else {
            account = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
        }
        log.info("开始获取代理总览，用户ID：{}，用户对象：{}", account.getId(), JSON.toJSONString(account));
        Integer agentId = null;
        Integer subcagencyId = null;
        if (account.getAttributes() == 4) {
            agentId = account.getAgentId();
        } else if (account.getAttributes() == 1) {
            subcagencyId = account.getId();
        } else {
            agentId = account.getId();
        }
        BigDecimal rate = BigDecimal.ZERO;
        List<Map<String, Object>> maps = Lists.newArrayList();
        maps = agentNewMapper.agent0verview(startTime, endTime, agentId, subcagencyId);
        setHighestPayout(maps, agentId, subcagencyId);
        if (account.getAttributes() == 0) {
            rate = commissionRate(account);
        }
        maps.add(ImmutableMap.of("count", rate, "active", "rate"));

        BigDecimal totoalDeposit = BigDecimal.ZERO;
        BigDecimal totoalWithdram = BigDecimal.ZERO;
        BigDecimal bonusTotal = BigDecimal.ZERO;
        for (Map<String, Object> a : maps) {
            if (a.get("active").equals("depositAmount")) {
                totoalDeposit = new BigDecimal(a.get("count").toString());
            }
            if (a.get("active").equals("withdramAmount")) {
                totoalWithdram = new BigDecimal(a.get("count").toString());
            }
            if (a.get("active").equals("bonusTotal")) {
            	bonusTotal = new BigDecimal(a.get("count").toString());
            }
            
        }
        // 设置派彩，平台费，服务费
        setPayout(maps, startTime, endTime, agentId, subcagencyId, bonusTotal);

        maps.add(ImmutableMap.of("count", totoalDeposit.subtract(totoalWithdram), "active", "ctDiffer"));
        return ImmutableMap.of("dataOverview", maps);
    }

    private void setHighestPayout(List<Map<String, Object>> maps, Integer agentId, Integer subcagencyId) {
        String startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_10_DATE, 0, 0);
        String endTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_10_DATE, -1, 0);
        DataTrendParamDto dto = new DataTrendParamDto();
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setAgentId(agentId);
        dto.setSubcagencyId(subcagencyId);

        BigDecimal highestPayout = agentHomeMapper.getHighestPayout(dto);
        if (nonNull(highestPayout)) {
            highestPayout = CommonUtil.adjustScale(highestPayout);
        } else {
            highestPayout = BigDecimal.ZERO;
        }
        maps.add(ImmutableMap.of("count", highestPayout, "active", "highestPayout"));
    }

    private void setPayout(List<Map<String, Object>> maps, String startTime, String endTime, Integer cagencyid, Integer subcagencyId, BigDecimal bonusTotal) {
        log.info("开始获取代理总览setPayout，用户ID：{}", cagencyid);
        BigDecimal bigDecimal = BigDecimal.ZERO;
        CommissionCastDto dto = commissionCastMapper.sumPayoutBonusamount(startTime, endTime, cagencyid, subcagencyId);
        if (nonNull(dto)) {
            AgentAccount account = agentAccountMapper.selectByPrimaryKey(cagencyid);
            log.info("开始获取代理总览setPayout，用户ID：{}，用户对象：{}", cagencyid, JSON.toJSONString(account));
            BigDecimal cost = BigDecimal.ZERO; 
            BigDecimal serviceCost = BigDecimal.ZERO;
            // 不是分线代理查询平台费，服务费
            if (account != null && account.getAttributes() != Constants.EVNumber.one) {
            	ReportParamsDto reportParamsDto = new ReportParamsDto();
            	reportParamsDto.setStartTime(startTime);
            	reportParamsDto.setEndTime(endTime);
            	reportParamsDto.setAgyAccount(account.getAgyAccount());
            	CostTotalDto costTotalDto = finaceReportService.depotCostTotalForSingle(reportParamsDto);
            	cost = costTotalDto.getCost();
            	serviceCost = costTotalDto.getServiceCost();
            }
            
            BigDecimal netwinlose = dto.getTotalPayout().add(cost).add(serviceCost).add(dto.getCalculateProfit())
                    .add(bonusTotal);
            bigDecimal = netwinlose.negate();
            // 平台费
            maps.add(ImmutableMap.of("count", cost, "active", "cost"));
            // 服务费
            maps.add(ImmutableMap.of("count", serviceCost, "active", "serviceCost"));
            
            // 人工调整费用
            maps.add(ImmutableMap.of("count", dto.getCalculateProfit(), "active", "calculateProfit"));
        }
        maps.add(ImmutableMap.of("count", bigDecimal, "active", "payout"));
    }

    public DepotCostDto getDepotCost(String startTime, String endTime, Integer cagencyid, Integer subcagencyId) {
        DepotCostDto costDto = new DepotCostDto();
        costDto.setStartTime(startTime);
        costDto.setEndTime(endTime);
        costDto.setCagencyId(cagencyid);
        costDto.setSubcagencyId(subcagencyId);
        costDto.setGroubyAgent(Boolean.TRUE);
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(cagencyid);
        // 1：平台费
        if (isNull(agentAccount) || isNull(agentAccount.getFeeModel()) || agentAccount.getFeeModel() == 1) {
            costDto.setFeeModel(Constants.EVNumber.one);
            // 2：服务费
        } else if (agentAccount.getFeeModel() == 2) {
            costDto.setFeeModel(Constants.EVNumber.two);
            // 3: 全部 ：  平台费 + （平台费 * 额外费率） + 服务费
        } else if (agentAccount.getFeeModel() == 3) {
            costDto.setFeeModel(Constants.EVNumber.three);
        }

        // 根据收费模式，查询费用
        DepotCostDto depotCostDto = agentCommMapper.sumDepotCost(costDto);

        if (nonNull(depotCostDto)) {
            depotCostDto.setFeeModel(costDto.getFeeModel());
            depotCostDto.setCost(nonNull(depotCostDto.getCost()) ? depotCostDto.getCost() : BigDecimal.ZERO);
            return depotCostDto;
        }
        DepotCostDto depotCostDto1 = new DepotCostDto();
        depotCostDto1.setCost(BigDecimal.ZERO);
        return depotCostDto1;
    }

    private BigDecimal commissionRate(AgentAccount account) {
        AgentContract contract = agentContracttMapper.selectByPrimaryKey(account.getContractId());
        AgentContract defaultContract = agentContracttMapper.selectByPrimaryKey(Constants.EVNumber.one);
        if (StringUtil.isNotEmpty(account.getContractStart())) {
            int result = DateUtil.timeCompare(account.getContractStart(), DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0), DateUtil.FORMAT_18_DATE_TIME);
            if (result == 1) {
                return getCommissionRate(defaultContract);
            }
            return getCommissionRate(contract);
        }
        if (StringUtil.isNotEmpty(account.getContractEnd())) {
            int result = DateUtil.timeCompare(account.getContractStart(), DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0), DateUtil.FORMAT_18_DATE_TIME);
            if (result == -1) {
                return getCommissionRate(defaultContract);
            }
            return getCommissionRate(contract);
        }
        return getCommissionRate(defaultContract);
    }

    private BigDecimal getCommissionRate(AgentContract contract) {
        List<AgentContractDto> contractDtos = new Gson().fromJson(contract.getRule(),
                new TypeToken<List<AgentContractDto>>() {
                }.getType());
        if (nonNull(contractDtos)) {
            Optional<AgentContractDto> optional = contractDtos.stream().filter(Objects::nonNull)
                    .max(Comparator.comparing(AgentContractDto::getCommissionRate));
            if (optional.isPresent()) {
                return optional.get().getCommissionRate();
            }
        }
        return BigDecimal.ZERO;
    }

    public AgentAccount getAgentAccount(Integer agentId) {
        return agentAccountMapper.selectByPrimaryKey(agentId);
    }

    public List<DataTrendDto> dataTrendList(AgentAccount account, DataTrendParamDto dto) {
        Integer agentId = null;
        Integer subcagencyId = null;
        if (account.getAttributes() == 4) {
            agentId = account.getAgentId();
        } else if (account.getAttributes() == 1) {
            subcagencyId = account.getId();
        } else {
            agentId = account.getId();
        }
        dto.setAgentId(agentId);
        dto.setSubcagencyId(subcagencyId);
        switch (dto.getType()) {
            case 1:
                return agentHomeMapper.findNetwinLoseList(dto);
            case 2:
                return dataTrendMapper.findDepositList(dto);
            case 3:
                return dataTrendMapper.findFirstDepositList(dto);
            case 4:
                return dataTrendMapper.findValidbetList(dto);
            case 5:
                return dataTrendMapper.findWithdrawList(dto);
            case 6:
                return dataTrendMapper.findRegisterList(dto);
            case 7:
                return dataTrendMapper.findFirstDepositNumList(dto);
            case 8:
                return dataTrendMapper.findDepositNumList(dto);
            case 9:
                return dataTrendMapper.findWithdrawNumList(dto);
            case 10:
                return dataTrendMapper.findValidbetNumList(dto);
            default:
        }
        return null;
    }


    public AgentAccount getAgentAccountByToken(AgentAccount account) {
        Claims claims;
        try {
            claims = jwtUtils.getClaimByToken(account.getAgentToken());
        } catch (ExpiredJwtException exception) {
            throw new R200Exception("token失效", 401);
        }
        Integer agentId = Integer.valueOf(claims.getSubject());
        return agentAccountMapper.selectByPrimaryKey(agentId);
    }

}
