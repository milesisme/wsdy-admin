package com.wsdy.saasops.modules.sdyExcel.service;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.aff.service.SdyDataService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.log.dao.LogMbrregisterMapper;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.member.dao.*;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.operate.mapper.SdyActivityMapper;
import com.wsdy.saasops.modules.sdyExcel.dto.AccountInputEntityDto;
import com.wsdy.saasops.modules.sdyExcel.dto.BankInputEntityDto;
import com.wsdy.saasops.modules.sdyExcel.dto.ValilBetEntityDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class SdyExcelService {

    @Autowired
    private AgentAccountService agentAccountService;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private MbrGroupMapper groupMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Value("${api.regDeaultCagencyId}")
    private int cagencyId;
    @Autowired
    private MbrAccountService accountService;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private LogMbrregisterMapper logMbrregisterMapper;
    @Autowired
    private MbrActivityLevelMapper activityLevelMapper;
    @Autowired
    private SdyActivityMapper sdyActivityMapper;
    @Autowired
    private MbrBankcardMapper bankcardMapper;
    @Autowired
    private SdyDataService sdyDataService;


    private static final String SDY_EXCEL_AGENT = "GeneralAgent";

    public List<AccountInputEntityDto> getInputEntityList(MultipartFile file) {
        try {
            ImportParams params = new ImportParams();
            params.setStartSheetIndex(0);
            List<AccountInputEntityDto> result = ExcelImportUtil.importExcel(file.getInputStream(),
                    AccountInputEntityDto.class, params);
            return result;
        } catch (Exception e) {
            log.info("解析文件出错", e);
        }
        return null;
    }

    public List<AccountInputEntityDto> getAgentInputEntityList(List<AccountInputEntityDto> accountInputEntityDtos) {
        List<AccountInputEntityDto> agentInputEntityDtos =
                accountInputEntityDtos.stream().filter(e ->
                        StringUtils.isNotEmpty(e.getAffiliatecode()))
                        .collect(Collectors.toList());
        return agentInputEntityDtos;
    }

    public List<BankInputEntityDto> getBankInputEntityList(MultipartFile file) {
        try {
            ImportParams params = new ImportParams();
            params.setStartSheetIndex(1);
            List<BankInputEntityDto> result = ExcelImportUtil.importExcel(file.getInputStream(),
                    BankInputEntityDto.class, params);
            return result;
        } catch (Exception e) {
            log.info("解析银行卡文件出错", e);
        }
        return null;
    }

    @Transactional
    public void inserAgentAccount(AccountInputEntityDto entityDto, String siteCode) {
        try {
            if (StringUtils.isEmpty(entityDto.getMembercode()) || "NULL".equalsIgnoreCase(entityDto.getMembercode())) {
                log.info("excel000**************代理名为空跳过");
                return;
            }
            entityDto.setMembercode(entityDto.getMembercode().trim());
            AgentAccount agentAccount1 = new AgentAccount();
            agentAccount1.setAgyAccount(entityDto.getMembercode());
            int count = agentAccountMapper.selectCount(agentAccount1);
            if (count > 0) {
                log.info(entityDto.getMembercode() + "excel000**************代理已经存在跳过," + JSON.toJSONString(entityDto));
                return;
            }

            AgentAccount agentAccount = new AgentAccount();
            String salt = RandomStringUtils.randomAlphanumeric(20);
            if (StringUtils.isNotEmpty(entityDto.getPass())) {
                agentAccount.setAgyPwd(new Sha256Hash(entityDto.getPass().trim(), salt).toHex());
                agentAccount.setSecurePwd(new Sha256Hash(entityDto.getPass().trim(), salt).toHex());
            }
            agentAccount.setSalt(salt);
            agentAccount.setAgyAccount(entityDto.getMembercode());
            agentAccount.setAvailable(Constants.EVNumber.one);
            agentAccount.setEmail(entityDto.getEmail());
            if (nonNull(entityDto.getJoined_date())) {
                agentAccount.setCreateTime(format(entityDto.getJoined_date(), FORMAT_18_DATE_TIME));
            }
            if (StringUtils.isNotEmpty(entityDto.getRegister_ip())
                    && !"NULL".equalsIgnoreCase(entityDto.getRegister_ip())) {
                agentAccount.setIp(entityDto.getRegister_ip());
            }
            agentAccount.setParentId(getParentId(entityDto.getParentmembercode()));
            agentAccount.setStatus(Constants.EVNumber.one);
            agentAccount.setRegisterSign(Constants.EVNumber.one);
            if (StringUtils.isNotEmpty(entityDto.getAffilaitedomain())
                    && !"NULL".equalsIgnoreCase(entityDto.getAffilaitedomain())) {
                entityDto.setAffilaitedomain(entityDto.getAffilaitedomain().trim());
                agentAccount.setRegisterUrl(entityDto.getAffilaitedomain());
            }
            if (StringUtils.isNotEmpty(entityDto.getFullname())
                    && !"NULL".equalsIgnoreCase(entityDto.getFullname())) {
                agentAccount.setRealName(entityDto.getFullname().trim());
            }
            agentAccount.setMobile(getSubMobile(entityDto.getContact()));
            agentAccount.setGroupId(getGroupId(entityDto.getGroupname()));
            if (StringUtils.isNotEmpty(entityDto.getAffiliatecode())) {
                agentAccount.setSpreadCode(entityDto.getAffiliatecode().trim());
            } else {
                agentAccount.setSpreadCode(agentAccountService.getSpreadCode());
            }
            agentAccount.setCommissionId(null);
            agentAccountMapper.insert(agentAccount);
            agentAccountService.addAgentWalletAndTree(agentAccount);
            if (StringUtils.isNotEmpty(entityDto.getAffilaitedomain())) {
                String[] str = entityDto.getAffilaitedomain().split(",");
                for (int i = 0; i < str.length; i++) {
                    sdyDataService.agyDomainAudit(agentAccount, str[i], siteCode);
                }
            }
            log.info(entityDto.getMembercode() + "excel111**************代理add成功，" + JSON.toJSONString(entityDto));
        } catch (Exception e) {
            log.error(entityDto.getMembercode() + "excel000**************代理add出错，" + JSON.toJSONString(entityDto), e);
        }
    }

    public Integer getGroupId(String groupName) {
        if (StringUtils.isNotEmpty(groupName)
                && !"NULL".equalsIgnoreCase(groupName)) {
            MbrGroup group = new MbrGroup();
            group.setGroupName(groupName);
            MbrGroup group1 = groupMapper.selectOne(group);
            if (nonNull(group1)) {
                return group1.getId();
            }
        }
        return Constants.EVNumber.one;
    }

    private String getSubMobile(String contact) {
        try {
            if (StringUtils.isNotEmpty(contact)
                    && !"NULL".equalsIgnoreCase(contact)) {
                String mobile = contact.trim();
                return mobile.substring(3).trim();
            }
        } catch (Exception e) {
            log.info(contact + "出错");
        }
        return null;
    }

    public Integer getParentId(String parentmembercode) {
        if (StringUtils.isNotEmpty(parentmembercode)) {
            AgentAccount agentAccount1 = new AgentAccount();
            agentAccount1.setAgyAccount(parentmembercode.trim());
            AgentAccount agentAccount = agentAccountMapper.selectOne(agentAccount1);
            if (nonNull(agentAccount)) {
                return agentAccount.getId();
            }
        }
        AgentAccount agentAccount1 = new AgentAccount();
        agentAccount1.setAgyAccount(SDY_EXCEL_AGENT);
        AgentAccount agentAccount = agentAccountMapper.selectOne(agentAccount1);
        if (nonNull(agentAccount)) {
            return agentAccount.getId();
        }
        return Constants.EVNumber.zero;
    }

    private AgentAccount getAgentAccount(String parentmembercode) {
        if (StringUtils.isNotEmpty(parentmembercode) && !"NULL".equalsIgnoreCase(parentmembercode)) {
            AgentAccount agentAccount1 = new AgentAccount();
            agentAccount1.setAgyAccount(parentmembercode.trim());
            AgentAccount agentAccount = agentAccountMapper.selectOne(agentAccount1);
            if (nonNull(agentAccount)) {
                return agentAccount;
            }
        }
        return agentAccountMapper.selectByPrimaryKey(cagencyId);
    }


    @Transactional
    public void insertAccount(AccountInputEntityDto entityDto) {
        try {
            if (StringUtils.isEmpty(entityDto.getMembercode()) || "NULL".equalsIgnoreCase(entityDto.getMembercode())) {
                log.info("excel000**************会员名为空跳过");
                return;
            }
            entityDto.setMembercode(entityDto.getMembercode().trim());
            MbrAccount account = new MbrAccount();
            account.setLoginName(entityDto.getMembercode());
            int count = accountMapper.selectCount(account);
            if (count > 0) {
             /* //  CompletableFuture.runAsync(() -> {
                    updateBanlace(account.getLoginName(), setAmount(entityDto.getBalance()));
               // });*/
                updateBanlace(account.getLoginName(), setAmount(entityDto.getBalance()));
                log.info(entityDto.getMembercode() + "excel000**************会员已经存在跳过," + JSON.toJSONString(entityDto));
                return;
            }
            String salt = RandomStringUtils.randomAlphanumeric(20);
            if (StringUtils.isNotEmpty(entityDto.getPass())) {
                account.setLoginPwd(new Sha256Hash(entityDto.getPass().trim(), salt).toHex());
                account.setSecurePwd(new Sha256Hash(entityDto.getPass().trim(), salt).toHex());
            }
            account.setMobile(getSubMobile(entityDto.getContact()));
            if (StringUtils.isNotEmpty(entityDto.getEmail())
                    && !"NULL".equalsIgnoreCase(entityDto.getEmail())) {
                account.setEmail(entityDto.getEmail());
            }
            if (StringUtils.isNotEmpty(entityDto.getFullname())
                    && !"NULL".equalsIgnoreCase(entityDto.getFullname())) {
                account.setRealName(entityDto.getFullname());
            }
            account.setSalt(salt);
            String currentDate = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
            account.setModifyTime(currentDate);
            account.setLoginTime(currentDate);
            account.setDomainCode(accountService.getDomainCode());
            if (nonNull(entityDto.getDob())) {
                account.setBirthday(format(entityDto.getDob(), FORMAT_10_DATE));
            }
            account.setActLevelId(1);
            if (StringUtils.isNotEmpty(entityDto.getVip()) && !"NULL".equalsIgnoreCase(entityDto.getVip())) {
                MbrActivityLevel activityLevel = getMbrActivityLevel(entityDto.getVip());
                if (nonNull(activityLevel)) {
                    account.setActLevelId(activityLevel.getId());
                }
            }
            if (nonNull(entityDto.getJoined_date())) {
                account.setRegisterTime(format(entityDto.getJoined_date(), FORMAT_18_DATE_TIME));
            }
            setAdminMbrAccount(account, entityDto);
            if (StringUtils.isNotEmpty(entityDto.getStatus())) {
                if ("SUSPENDED".equals(entityDto.getStatus())) {
                    account.setAvailable((byte) 0);
                }
            }
            accountMapper.insert(account);
            MbrWallet wallet = accountService.getMbrWallet(account);
            wallet.setBalance(setAmount(entityDto.getBalance()));
            if (StringUtils.isNotEmpty(entityDto.getRegister_ip()) &&
                    !"NULL".equalsIgnoreCase(entityDto.getRegister_ip())) {
                account.setRegisterIp(entityDto.getRegister_ip().trim());
            }
            if (nonNull(entityDto.getJoined_date())) {
                account.setRegisterTime(format(entityDto.getJoined_date(), FORMAT_18_DATE_TIME));
            }
            mbrWalletMapper.insert(wallet);

            accountService.setMbrNode(account, account.getCodeId(), Boolean.FALSE);
            logMbrregisterMapper.insert(getlogRegister(account));

            log.info(entityDto.getMembercode() + "excel111**************会员add成功，" + JSON.toJSONString(entityDto));
        } catch (Exception e) {
            log.error(entityDto.getMembercode() + "excel000**************会员add出错，" + JSON.toJSONString(entityDto), e);
        }
    }

    public void updateBanlace(String loginName, BigDecimal amount) {
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setLoginName(loginName);
        MbrWallet wallet = mbrWalletMapper.selectOne(mbrWallet);
        wallet.setBalance(amount);
        mbrWalletMapper.updateByPrimaryKeySelective(wallet);
    }

    private BigDecimal setAmount(String balance) {
        try {
            if (StringUtils.isNotEmpty(balance) && !"NULL".equalsIgnoreCase(balance)) {
                if (new BigDecimal(balance).compareTo(BigDecimal.ZERO) == 1) {
                    return new BigDecimal(balance);
                }
            }
        } catch (Exception e) {
            log.error("balane", e);
        }
        return BigDecimal.ZERO;
    }

    private MbrActivityLevel getMbrActivityLevel(String tierName) {
        MbrActivityLevel activityLevel = new MbrActivityLevel();
        activityLevel.setTierName(tierName);
        return activityLevelMapper.selectOne(activityLevel);
    }


    private LogMbrRegister getlogRegister(MbrAccount mbrAccount) {
        LogMbrRegister logRegister = new LogMbrRegister();
        logRegister.setRegisterIp(mbrAccount.getRegisterIp());
        logRegister.setRegisterSource((byte) 1);
        logRegister.setRegisterUrl(mbrAccount.getRegisterUrl());
        logRegister.setLoginName(mbrAccount.getLoginName());
        logRegister.setAccountId(mbrAccount.getId());
        logRegister.setRegisterTime(mbrAccount.getRegisterTime());
        //logRegister.setRegArea(ipService.getIpArea(logRegister.getRegisterIp()));
        return logRegister;
    }

    private void setAdminMbrAccount(MbrAccount mbrAccount, AccountInputEntityDto entityDto) {
        mbrAccount.setId(null);
        mbrAccount.setIsVerifyMoblie(Constants.Available.disable);
        mbrAccount.setIsAllowMsg(Constants.Available.disable);
        mbrAccount.setIsLock(Constants.Available.disable);
        mbrAccount.setAvailable(Constants.Available.enable);
        mbrAccount.setIsVerifyEmail(Constants.Available.disable);
        mbrAccount.setIsAllowEmail(Constants.Available.enable);
        mbrAccount.setIsOnline(Constants.Available.disable);
        // mbrAccount.setActLevelId(Constants.EVNumber.one);
        mbrAccount.setIsActivityLock(Constants.EVNumber.zero);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(mbrAccount.getMobile())) {
            mbrAccount.setIsVerifyMoblie(Constants.Available.enable);
            mbrAccount.setIsAllowMsg(Constants.Available.enable);
        }
        AgentAccount agentAccount1 = getAgentAccount(entityDto.getParentmembercode());
        mbrAccount.setCagencyId(agentAccount1.getId());
        // 获取总代-无限层级
        AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount1);
        mbrAccount.setTagencyId(tAgent.getId());
        mbrAccount.setGroupId(getGroupId(entityDto.getGroupname()));
    }

    @Transactional
    public void insertBankCard(BankInputEntityDto entityDto, String siteCode) {
        try {
            if (StringUtils.isEmpty(entityDto.getMembercode())) {
                log.info("excel222**************银行卡会员名为空跳过");
                return;
            }
            if (StringUtils.isEmpty(entityDto.getBankacc())) {
                log.info("excel222**************银行卡为空跳过");
                return;
            }
            //ThreadLocalCache.setSiteCodeAsny(siteCode);
            entityDto.setMembercode(entityDto.getMembercode().trim());
            MbrAccount account = new MbrAccount();
            account.setLoginName(entityDto.getMembercode());
            MbrAccount mbrAccount = accountMapper.selectOne(account);
            if (isNull(mbrAccount)) {
                log.info(entityDto.getMembercode() + "excel222**************会员不存在跳过," + JSON.toJSONString(entityDto));
                return;
            }
            BaseBank baseBank = getBaseBank(entityDto.getBankname());
            if (isNull(baseBank)) {
                log.info(entityDto.getMembercode() + "excel222**************银行不存在跳过," + JSON.toJSONString(entityDto));
                return;
            }
            MbrBankcard bankcard = new MbrBankcard();
            bankcard.setRealName(entityDto.getBankaccname());
            bankcard.setIsDel(Constants.Available.disable);
            bankcard.setAvailable(Constants.Available.enable);
            bankcard.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bankcard.setLoginName(mbrAccount.getLoginName());
            bankcard.setAccountId(mbrAccount.getId());
            bankcard.setBankName(baseBank.getBankName());
            bankcard.setBankCardId(baseBank.getId());
            bankcard.setProvince(entityDto.getProvince());
            bankcard.setCity(entityDto.getCity());
            bankcard.setCardNo(getCardNo(entityDto.getBankacc()));
            bankcardMapper.insert(bankcard);

            log.info(entityDto.getMembercode() + "exce9**************会员银行卡add成功，" + JSON.toJSONString(entityDto));
        } catch (Exception e) {
            log.error(entityDto.getMembercode() + "excel222**************会员银行卡add出错，" + JSON.toJSONString(entityDto), e);
        }
    }

    private String getCardNo(String bankacc) {
        bankacc = bankacc.trim();
        bankacc = bankacc.replaceAll(" ", "");
        return bankacc;
    }

    private BaseBank getBaseBank(String bankName) {
        if (StringUtils.isNotEmpty(bankName)) {
            if (!"中国银行".equals(bankName)) {
                bankName = bankName.replaceAll("中国", "");
                bankName = bankName.replaceAll("银行", "");
            }
            return sdyActivityMapper.findBankOne(bankName);
        }
        return null;
    }

    @SneakyThrows
    public static void main(String[] args) {
        String path = "D:\\sdy会员\\UserDPTurnover.xlsx";
        ImportParams params = new ImportParams();
        params.setStartSheetIndex(0);
      /*  List<AccountInputEntityDto> result = ExcelImportUtil.importExcel(new File(path),
                AccountInputEntityDto.class, params);
        System.out.println(format(result.get(0).getJoined_date(), FORMAT_18_DATE_TIME));
        System.out.println(format(result.get(0).getDob(), FORMAT_10_DATE));
        ImportParams paramsBank = new ImportParams();
        paramsBank.setStartSheetIndex(3);*/
        List<ValilBetEntityDto> result1 = ExcelImportUtil.importExcel(new File(path),
                ValilBetEntityDto.class, params);
        String str = "";
        for (ValilBetEntityDto dto : result1) {
             str += "INSERT INTO `aff_vaildbet`(`loginname`, `totalvalidbet`, `totaldp`) VALUES ('" + dto.getMemberaccount() + "', " + dto.getTotalvalidbet() + ", " + dto.getTotaldp() + ");\r\n";
            //System.out.println(str);
        }
        File writename = new File("D:\\sdy会员\\有效投注.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件
        writename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));
        out.write(str);
        out.flush();
        out.close();

    }
}
