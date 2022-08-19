package com.wsdy.saasops.api.modules.user.controller;

import javax.servlet.http.HttpServletRequest;

import com.wsdy.saasops.api.constants.ApiConstants;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wsdy.saasops.api.config.PtConfig;
import com.wsdy.saasops.api.modules.user.dto.PtRouteDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Slf4j
@Controller
@RequestMapping("/api/ptGame")
@Api(value = "pt游戏跳转", tags = "pt游戏跳转")
public class PtGameRoutingController {
    @Autowired
    private PtConfig ptConfig;

    @GetMapping("/routing")
    @ApiOperation(value = "pt游戏跳转", notes = "pt游戏跳转")
    public String routing(@RequestParam("param") String param, HttpServletRequest request) {
        PtRouteDto dto = PtRouteDto.getPtRouteDto(param);
        if (dto.getTerminal().compareTo(ApiConstants.Terminal.pc) == 0) {
            dto.setJsUrl(ptConfig.getJsUrl());
            dto.setGameRouteUrl(ptConfig.getGameRouteUrl());
            dto.setGameUrl(dto.getGameRouteUrl() + PtRouteDto.generateParm(dto));
            request.setAttribute("obj", dto);
            return "/modules/pt/ptLogin";
        }
        if (dto.getTerminal().compareTo(ApiConstants.Terminal.mobile) == 0) {
            dto.setJsUrl(ptConfig.getJsUrl());
            dto.setGameRouteUrl(ptConfig.getGameMbRouteUrl());
            dto.setGameUrl(dto.getGameRouteUrl() + PtRouteDto.generateParm(dto));
            request.setAttribute("obj", dto);
            return "/modules/pt/ptTest";
        }
        return "";
    }
}