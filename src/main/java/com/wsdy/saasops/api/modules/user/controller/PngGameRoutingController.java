package com.wsdy.saasops.api.modules.user.controller;

import javax.servlet.http.HttpServletRequest;

import com.wsdy.saasops.api.constants.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wsdy.saasops.api.config.PngConfig;
import com.wsdy.saasops.api.modules.user.dto.PngLaunchGameDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/api/pngGame")
@Api(value = "png游戏跳转", tags = "png游戏跳转")
@Slf4j
public class PngGameRoutingController {
    @Autowired
    private PngConfig pngConfig;

    @GetMapping("/routing")
    @ApiOperation(value = "png游戏跳转", notes = "png游戏跳转")
    public String routing(@RequestParam("param") String param, HttpServletRequest request) {
        log.debug("png 进入开始");
        PngLaunchGameDto dto = PngLaunchGameDto.getPngLaunchGameDto(param);
        if (dto.getTerminal().compareTo( ApiConstants.Terminal.pc) == 0) {
            request.setAttribute("url", pngConfig.getJsUrl() + dto.toString() + pngConfig.getWithAndHight());
        } else {
            dto.setTicket(dto.getUsername());
            dto.setUsername(null);
//            request.setAttribute("url", pngConfig.getMbUrl() + dto.toString());
            return "redirect:" + pngConfig.getMbUrl() + dto.toString();
        }
        log.debug("url地址参数{}", pngConfig.getJsUrl() + dto.toString() + pngConfig.getWithAndHight());
//        System.out.println(request.getAttribute("url"));
        return "/modules/png/routing";
    }
}