package com.wsdy.saasops.modules.sys.controller;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.transferNew.dto.GatewayResponseDto;
import com.wsdy.saasops.api.modules.transferNew.service.GatewayDepotService;
import com.wsdy.saasops.api.modules.unity.dto.LoginModel;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.AESUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.log.dao.LogMbrregisterMapper;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrBankcardMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;

@Slf4j
@RestController
@RequestMapping("/bkapi/file/export")
public class SysFileExportRecordController {

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Value("${mbr.account.excel.path}")
    private String mbrAccountExcelTempPath;
    @Autowired
    private TCpSiteService tCpSiteService;

    @Autowired
    private MbrDepotWalletMapper mbrDepotWalletMapper;
    @Autowired
    private GatewayDepotService gatewayDepotService;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private LogMbrregisterMapper logMbrregisterMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrBankcardMapper mbrBankcardMapper;

    @GetMapping("downloadExcel")
    @ApiOperation(value = "下载excel文件",notes = "下载excel文件")
    public void downloadMbrAccountInfoExcel(@RequestParam("fileName")String fileName, @RequestParam("downloadFileName")String downloadFileName,@RequestParam("SToken")String SToken,
                                              HttpServletRequest request, HttpServletResponse response){
        Assert.isNull(fileName,"fileId不能为空");
        try{
            SToken = URLDecoder.decode(SToken, "utf-8");
            SToken = AESUtil.decrypt(SToken);
        } catch (UnsupportedEncodingException e) {
            log.error("downloadMbrAccountInfoExcel==下载excel文件URLDecoder错误==" + e);
        } catch (Exception e) {
            log.error("downloadMbrAccountInfoExcel==下载excel文件解析SToken错误==" + e);
        }

        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(SToken));
        sysFileExportRecordService.downloadFile(response,fileName,downloadFileName);
    }

    //todo 将checkfile服务统一到这里；数据库添加downloadfileName字段，用于关联module和文件名


    @GetMapping("/queryBalance")
    public R queryBalance(@RequestParam("siteCode") @NotNull  String siteCode,
                  @RequestParam("depotCode")  @NotNull String depotCode,
                  @RequestParam("opt")  @NotNull String opt,
                  @RequestParam("syn")   String syn,
                  HttpServletRequest request) {

        if("1".equals(syn)){
            return queryBalanceSyn(siteCode,depotCode,opt,request);
        }else{
            return queryBalanceAsy(siteCode,depotCode,opt,request);
        }
    }

    public R queryBalanceAsy(String siteCode,String depotCode,String opt,
                             HttpServletRequest request){
        // 设置操作库
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));

        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setDepotName(depotCode);
        List<MbrDepotWallet> mbrDepotWallets = mbrDepotWalletMapper.select(mbrDepotWallet);

        if(Objects.isNull(mbrDepotWallets)){
            return R.ok("会员0个");
        }
        if(Objects.nonNull(mbrDepotWallets) && "1".equals(opt)){    //opt 1 查询条数 0 导出余额
            return R.ok("会员" + mbrDepotWallets.size() + "个");
        }

        LoginModel loginModel = new LoginModel();
        loginModel.setSiteCode(siteCode);

        List<String> result = new ArrayList<>();
        long star = System.currentTimeMillis();
        // 循环查询
        for(MbrDepotWallet w : mbrDepotWallets){
            loginModel.setUserName(w.getLoginName());
            loginModel.setPassword(w.getPwd());
            TGmApi tGmApi = new TGmApi();
            tGmApi.setDepotCode(depotCode);
            loginModel.setTGmApi(tGmApi);
            GatewayResponseDto gatewayDto = gatewayDepotService.queryBalance(loginModel);
            if (nonNull(gatewayDto) && Boolean.TRUE.equals(gatewayDto.getCode()) && StringUtils.isNotEmpty(gatewayDto.getMessage())) {
                BigDecimal amount = new BigDecimal(gatewayDto.getMessage());
                String str = "用户名===" +  w.getLoginName() + " ===余额 "+ String.valueOf(amount) + " ===查询时间 " + getCurrentDate(FORMAT_18_DATE_TIME);
                result.add(str);
            }
            if(nonNull(gatewayDto) && Boolean.FALSE.equals(gatewayDto.getCode()) && StringUtils.isNotEmpty(gatewayDto.getMessage())){
                String str = "用户名===" +  w.getLoginName() + " ===余额 "+ gatewayDto.getMessage() + " ===查询时间 " + getCurrentDate(FORMAT_18_DATE_TIME);
                result.add(str);
            }
        }
        long end = System.currentTimeMillis();
        return R.ok(result).put("time:" + (end - star) + "size:" + mbrDepotWallets.size());
    }
    public R queryBalanceSyn(String siteCode,String depotCode,String opt,
                             HttpServletRequest request){
        // 设置操作库
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));

        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setDepotName(depotCode);
        List<MbrDepotWallet> mbrDepotWallets = mbrDepotWalletMapper.select(mbrDepotWallet);

        if(Objects.isNull(mbrDepotWallets)){
            return R.ok("会员0个");
        }
        if(Objects.nonNull(mbrDepotWallets) && "1".equals(opt)){    //opt 1 查询条数 0 导出余额
            return R.ok("会员" + mbrDepotWallets.size() + "个");
        }

        // 异步执行查询
        long star = System.currentTimeMillis();
        List<CompletableFuture<String>> result = new ArrayList<CompletableFuture<String>>();
        mbrDepotWallets.forEach(w->{
            result.add(getAsyncBalance(siteCode,depotCode,w));
        });

        CompletableFuture.allOf(result.toArray(new CompletableFuture[result.size()])).join();

        List<String> ret = new ArrayList<>();
        result.forEach(e -> {
            try {
                ret.add(e.get());
            } catch (Exception x) {
            }
        });
        long end = System.currentTimeMillis();
        return R.ok(ret).put("time:" + (end - star) + "size:" + mbrDepotWallets.size());
    }

    @Async
    public CompletableFuture<String> getAsyncBalance( String siteCode,  String depotCode, MbrDepotWallet w) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);

        LoginModel loginModel = new LoginModel();
        loginModel.setSiteCode(siteCode);
        loginModel.setUserName(w.getLoginName());
        loginModel.setPassword(w.getPwd());
        TGmApi tGmApi = new TGmApi();
        tGmApi.setDepotCode(depotCode);
        loginModel.setTGmApi(tGmApi);
        GatewayResponseDto gatewayDto = gatewayDepotService.queryBalance(loginModel);
        if (nonNull(gatewayDto) && Boolean.TRUE.equals(gatewayDto.getCode()) && StringUtils.isNotEmpty(gatewayDto.getMessage())) {
            BigDecimal amount = new BigDecimal(gatewayDto.getMessage());
            String str = "用户名===" +  w.getLoginName() + " ===余额 "+ String.valueOf(amount) + " ===查询时间 " + getCurrentDate(FORMAT_18_DATE_TIME);
            return CompletableFuture.completedFuture(str);
        }
        if(nonNull(gatewayDto) && Boolean.FALSE.equals(gatewayDto.getCode()) && StringUtils.isNotEmpty(gatewayDto.getMessage())){
            String str = "用户名===" +  w.getLoginName() + " ===余额 "+ gatewayDto.getMessage() + " ===查询时间 " + getCurrentDate(FORMAT_18_DATE_TIME);
            return CompletableFuture.completedFuture(str);
        }
        return CompletableFuture.completedFuture("");
    }

    @Transactional
    @GetMapping("/batchRegister")
    @ApiOperation(value = "站点会员批量转移",notes = "站点会员批量转移")
    public void batchRegister(@RequestParam("sourceSiteCode")String sourceSiteCode,
                              @RequestParam("destinationSiteCode")String destinationSiteCode,
                              @RequestParam("loginNames")List<String> loginNames,
                              HttpServletRequest request, HttpServletResponse response){
        log.info("batchRegister==sourceSiteCode==" + sourceSiteCode + "==destinationSiteCode==" + destinationSiteCode
        + "==loginnames==" + jsonUtil.toJson(loginNames) + "==开始");
        // 1. 获取原库会员数据
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(sourceSiteCode));
        List<MbrAccount> lists =  mbrMapper.getMbrListByLoginNames(loginNames); // mbr_account
        List<LogMbrRegister> registerLists =  mbrMapper.getRegisterListByLoginNames(loginNames);    // log_mbrregister
        List<MbrBankcard> cardLists =  mbrMapper.getBandcardListByLoginNames(loginNames);    // mbr_bankcard
        // 2. 批量注册
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(destinationSiteCode));
        int j = 0;
        for(MbrAccount mbr: lists){
            j++;
            log.info("batchRegister==" + j +"== 会员==loginname==" + mbr.getLoginName() + "==开始");
            // 2.0 存在会员则不保存
            MbrAccount t = new MbrAccount();
            t.setLoginName(mbr.getLoginName());
            List<MbrAccount> tmp = mbrAccountMapper.select(t);
            if(Objects.nonNull(tmp) && tmp.size()>0){
                log.info("batchRegister==" + j +"== 会员存在==loginname==" + mbr.getLoginName() );
                continue;
            }
            List<MbrBankcard> tmpList = new ArrayList<MbrBankcard>();
            // 2.0 获取该会员银行卡信息
            for(MbrBankcard card:cardLists){
                if(card.getAccountId().equals(mbr.getId())){
                    tmpList.add(card);
                    log.info("batchRegister==" + j +"== 获取该会员银行卡信息==loginname==" + mbr.getLoginName() );
                }
            }
            for(MbrBankcard card:tmpList){
                cardLists.remove(card);
            }
            // 2.1 保存会员
            mbr.setId(null);
            mbr.setGroupId(1);
            mbr.setTagencyId(1);
            mbr.setCagencyId(2);
            mbr.setActLevelId(1);
            String currentDate = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
//            mbr.setLoginTime(currentDate);
//            mbr.setRegisterTime(currentDate);
            mbr.setModifyTime(currentDate);
            mbr.setAvailable(Constants.Available.enable);
            mbr.setIsLock(Constants.Available.disable);
            mbrAccountMapper.insert(mbr);
            log.info("batchRegister==" + j +"== 保存会员==loginname==" + mbr.getLoginName()  + "==id==" + mbr.getId());
            // 2.2 保存钱包
            MbrWallet wallet = new MbrWallet();
            wallet.setLoginName(mbr.getLoginName());
            wallet.setBalance(Constants.DEAULT_ZERO_VALUE);
            wallet.setAccountId(mbr.getId());
            mbrWalletMapper.insert(wallet);
            log.info("batchRegister==" + j +"== 保存钱包==loginname==" + wallet.getLoginName()  + "==id==" + wallet.getId());
            // 2.3  保存节点
            mbrMapper.addMbrNodeEx(mbr.getId(), mbr.getId());
            log.info("batchRegister==" + j +"== 保存节点" );

            // 2.4 保存注册信息
            for(LogMbrRegister register:registerLists){
                if(register.getLoginName().equals(mbr.getLoginName())){
                    register.setAccountId(mbr.getId());
                    register.setId(null);
                    logMbrregisterMapper.insert(register);
                    log.info("batchRegister==" + j +"== 保存注册信息==loginname==" + register.getLoginName()  + "==id==" + register.getId());
                    break;
                }
            }
            // 2.5 保存银行卡信息
            for(MbrBankcard card:tmpList){
                card.setId(null);
                card.setAccountId(mbr.getId());
                card.setCreateTime(currentDate);
                mbrBankcardMapper.insert(card);
                log.info("batchRegister==" + j +"== 保存银行卡信息==loginname==" + mbr.getLoginName()  + "==id==" + card.getId());
            }

            log.info("batchRegister==" + j +"== 会员==loginname==" + mbr.getLoginName() + "==结束");
        }
        log.info("batchRegister==结束");
    }
}
