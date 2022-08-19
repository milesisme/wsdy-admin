package com.wsdy.saasops.api.modules.user.dto;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

import com.wsdy.saasops.api.constants.ApiConstants;
import org.springframework.util.StringUtils;

import com.wsdy.saasops.api.utils.CRequest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "游戏跳转必须参数")
public class PtRouteDto {
    @ApiModelProperty(value = "会员账号")
    private String loginName;
    @ApiModelProperty(value = "会员密码")
    private String pwd;
    @ApiModelProperty(value = "游戏语言")
    private String language;
    @ApiModelProperty(value = "游戏代码")
    private String game;
    @ApiModelProperty(value = "未知做用")
    private String nolobby;
    @ApiModelProperty(value = "参数JAVASCRIPT 路径")
    private String jsUrl;
    @ApiModelProperty(value = "游戏跳转路径")
    private String gameRouteUrl;
    @ApiModelProperty(value = "游戏URL")
    private String gameUrl;
    private Byte terminal;
    private String gameId;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (!StringUtils.isEmpty(loginName)) {
            buffer.append("&loginName=").append(loginName);
        }
        if (!StringUtils.isEmpty(pwd)) {
            buffer.append("&pwd=").append(pwd);
        }
        if (!StringUtils.isEmpty(language)) {
            buffer.append("&language=").append(language);
        }
        if (!StringUtils.isEmpty(game)) {
            buffer.append("&game=").append(game);
        }
        if (!StringUtils.isEmpty(nolobby)) {
            buffer.append("&nolobby=").append(nolobby);
        }
        if (!StringUtils.isEmpty(jsUrl)) {
            buffer.append("&scriptUrl=").append(jsUrl);
        }
        if (!StringUtils.isEmpty(gameRouteUrl)) {
            buffer.append("&routeUrl=").append(gameRouteUrl);
        }
        if (!StringUtils.isEmpty(terminal)) {
            buffer.append("&terminal=").append(terminal);
        }
        if (!StringUtils.isEmpty(gameId)) {
            buffer.append("&gameId=").append(gameId);
        }
        if (!StringUtils.isEmpty(buffer)) {
            buffer.delete(0, 1);
        }
        String asB64 = null;
        try {
            asB64 = Base64.getEncoder().encodeToString(buffer.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return asB64;
    }

    public static PtRouteDto getPtRouteDto(String codes) {
        PtRouteDto dto = new PtRouteDto();
        byte[] asBytes = Base64.getDecoder().decode(codes);
        try {
            String param = new String(asBytes, "utf-8");
            Map<String, String> mapRequest = CRequest.URLRequest(param);

            for (String strRequestKey : mapRequest.keySet()) {
                switch (strRequestKey) {
                    case "loginName":
                        dto.setLoginName(mapRequest.get(strRequestKey).toUpperCase());
                        break;
                    case "pwd":
                        dto.setPwd(mapRequest.get(strRequestKey));
                        break;
                    case "language":
                        dto.setLanguage(mapRequest.get(strRequestKey));
                        break;
                    case "game":
                        dto.setGame(mapRequest.get(strRequestKey));
                        break;
                    case "nolobby":
                        dto.setNolobby(mapRequest.get(strRequestKey));
                        break;
                    case "jsUrl":
                        dto.setJsUrl(mapRequest.get(strRequestKey));
                        break;
                    case "routeUrl":
                        dto.setGameRouteUrl(mapRequest.get(strRequestKey));
                        break;
                    case "terminal":
                        dto.setTerminal(new Byte(mapRequest.get(strRequestKey)));
                        break;
                    case "gameId":
                        dto.setGameId(mapRequest.get(strRequestKey));
                        break;
                    default:
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return dto;
    }

    public static String generateParm(PtRouteDto dto) {
        StringBuffer buffer = new StringBuffer();
        if (dto.getTerminal().compareTo(ApiConstants.Terminal.pc) == 0
                && !StringUtils.isEmpty(dto.getGame())) {
            buffer.append("&game=").append(dto.getGame());
            if (!StringUtils.isEmpty(dto.getLanguage())) {
                buffer.append("&language=").append(dto.getLanguage());
            }
            if (!StringUtils.isEmpty(dto.getNolobby())) {
                buffer.append("&nolobby=").append(dto.getNolobby());
            }
        }
        if (dto.getTerminal().compareTo(ApiConstants.Terminal.mobile) == 0
                && !StringUtils.isEmpty(dto.getGameId())) {
            buffer.append("&gameId=").append(dto.getGameId());
            buffer.append("&real=").append(1);
            if (!StringUtils.isEmpty(dto.getLoginName())) {
                buffer.append("&username=").append(dto.getLoginName());
            }
            if (!StringUtils.isEmpty(dto.getLanguage())) {
                buffer.append("&lang=").append(dto.getLanguage());
            }
        }
        if (!StringUtils.isEmpty(buffer)) {
            buffer.replace(0, 1, "?");
        }
        return buffer.toString();
    }
}
