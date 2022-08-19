package com.wsdy.saasops.modules.sys.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.member.entity.MbrMessageInfo;
import com.wsdy.saasops.modules.member.service.MbrMessageService;
import com.wsdy.saasops.modules.sys.dao.SysI18nDto;
import com.wsdy.saasops.modules.sys.service.SysI18nService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/bkapi/sys/i18n")
@Api(value = "/bkapi/sys/i18n", tags = "多语言")
public class SysI18nController {

    @Autowired
    private SysI18nService sysI18nService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrMessageService mbrMessageService;

    @GetMapping("/test")
    @ApiOperation(value = "测试", notes = "测试")
    @ApiImplicitParams({@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R test(@ModelAttribute MbrMessageInfo mbrMessageInfo) {
//        mbrMessageInfo.setIsAllDevice(0);
//        mbrMessageInfo.setIsPush(1);
//        mbrMessageInfo.setPushTitle("标题");
//        mbrMessageInfo.setPushContent("内容");
//
//        mbrMessageService.messagePushBatchSyn(mbrMessageInfo);
        return new R();
    }

    @PostMapping("/upload")
    @Transactional
    public R upload(@RequestParam("file") MultipartFile file, @RequestParam("i18n") String i18n, HttpServletRequest request ) {
        String key = "i18nUpload";
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, "file", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(isExpired)) {
            throw new R200Exception("任务处理中，请勿重复上传");
        }

        try {
            // 指定manage
            request.setAttribute(SystemConstants.SCHEMA_NAME, "manage");
            List<SysI18nDto> inputEntityList = sysI18nService.getInputEntityList(file);
            if (Collections3.isNotEmpty(inputEntityList)) {
                log.info("导入多语言开始" + DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                // 批量导入
                sysI18nService.insertI18nBatch(inputEntityList);
                log.info("导入多语言结束" + DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
            }
            return R.ok();
        } finally {
            redisService.del(key);
        }
    }
}
