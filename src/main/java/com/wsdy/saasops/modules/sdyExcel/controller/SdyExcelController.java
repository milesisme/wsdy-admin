package com.wsdy.saasops.modules.sdyExcel.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sdyExcel.dto.AccountInputEntityDto;
import com.wsdy.saasops.modules.sdyExcel.dto.BankInputEntityDto;
import com.wsdy.saasops.modules.sdyExcel.service.SdyExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/bkapi/sdyexcel/")
public class SdyExcelController extends AbstractController {

    @Autowired
    private SdyExcelService sdyExcelService;
    @Autowired
    private RedisService redisService;

    @PostMapping("upload")
    public R upload(@RequestParam("file") MultipartFile file) {
        return R.ok();
        /*String key = "sdyAccountUpload";
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, "file", 500, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复上传");
        }
        try {
            String siteCode = CommonUtil.getSiteCode();
            List<AccountInputEntityDto> inputEntityList = sdyExcelService.getInputEntityList(file);
            List<BankInputEntityDto> bankInputEntityDtos = sdyExcelService.getBankInputEntityList(file);
            if (Collections3.isNotEmpty(inputEntityList)) {
                List<AccountInputEntityDto> agentList = sdyExcelService.getAgentInputEntityList(inputEntityList);
                log.info("sdyexcel开始代理add" + DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                agentList.stream().forEach(ag -> sdyExcelService.inserAgentAccount(ag, siteCode));
                log.info("sdyexcel代理add结束" + DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                log.info("sdyexcel开始会员add" + DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                inputEntityList.stream().forEach(as -> sdyExcelService.insertAccount(as));
                log.info("sdyexcel会员add结束" + DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
            }
            if (Collections3.isNotEmpty(bankInputEntityDtos)) {
                log.info("sdyexcel开始bankadd");
                for (BankInputEntityDto entityDto : bankInputEntityDtos) {
                   // CompletableFuture.runAsync(() -> {
                        sdyExcelService.insertBankCard(entityDto, siteCode);
                   // });
                }
                log.info("sdyexcelBANKadd结束");
            }
            return R.ok();
        } finally {
            redisService.del(key);
        }*/
    }

}
