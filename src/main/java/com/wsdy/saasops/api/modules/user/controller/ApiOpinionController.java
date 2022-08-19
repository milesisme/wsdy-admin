package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.member.service.MbrOpinionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/opinion")
@Api(value = "api", tags = "会员意见")
public class ApiOpinionController {

    @Autowired
    private MbrOpinionService opinionService;

    @Login
    @PostMapping(value = "messageSend")
    @ApiOperation(value = "会员意见反馈", notes = "会员意见反馈")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R messageSend(HttpServletRequest request,
                         @RequestParam(value = "textContent", required = false) String textContent,
                         @RequestParam(value = "type", required = false) Integer type,
                         @RequestParam(value = "uploadMessageFile", required = false) MultipartFile uploadMessageFile) {
    	String resut = "";
    	try {
	        if (StringUtils.isEmpty(textContent) && Objects.isNull(uploadMessageFile)) {
	            throw new R200Exception("发送内容不能为空");
	        }
	        if (StringUtils.isNotEmpty(textContent)) {
	            if (StringUtil.isHasEmoji(textContent)) {
	                throw new R200Exception("不支持发送表情！");
	            }
	            Assert.isLenght(textContent, "发送内容最大长度为1000", 0, 1000);
	        }
	        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
	        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
	        resut = opinionService.accountMessageSend(accountId, loginName, uploadMessageFile, textContent, type);
    	} catch (Exception e) {
			log.error("messageSend----- 异常", e);
		}
        return R.ok().put(resut);
    }
}
