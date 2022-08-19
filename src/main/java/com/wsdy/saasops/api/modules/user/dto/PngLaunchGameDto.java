package com.wsdy.saasops.api.modules.user.dto;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.wsdy.saasops.api.utils.CRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PngLaunchGameDto {
    private String div;
    //private String gid;
    private String gameId;
    private String lang;
    private String pid;
    private String username;
    private String ticket;
    private String brand;
    private String practice;
    private Byte terminal;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (!StringUtils.isEmpty(div)) {
            buffer.append("div=").append(div).append("&");
        }

        if (!StringUtils.isEmpty(gameId)) {
            buffer.append("gameid=").append(gameId).append("&");
        }

/*		if (!StringUtils.isEmpty(gid))
			buffer.append("gid=").append(gid).append("&");*/

        if (!StringUtils.isEmpty(lang)) {
            buffer.append("lang=").append(lang).append("&");
        }

        if (!StringUtils.isEmpty(pid)) {
            buffer.append("pid=").append(pid).append("&");
        }

        if (!StringUtils.isEmpty(username)) {
            buffer.append("username=").append(username).append("&");
        }

        if (!StringUtils.isEmpty(brand)) {
            buffer.append("brand=").append(brand).append("&");
        }

        if (!StringUtils.isEmpty(practice)) {
            buffer.append("practice=").append(practice).append("&");
        }

        if (!StringUtils.isEmpty(terminal)) {
            buffer.append("terminal=").append(terminal).append("&");
        }

        if (!StringUtils.isEmpty(ticket)) {
            buffer.append("ticket=").append(ticket).append("&");
        }
        if (buffer.length() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        return buffer.toString();
    }

    public String toBase64() {
        String asB64 = null;
        try {
            asB64 = Base64.getEncoder().encodeToString(toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return asB64;
    }

    public static PngLaunchGameDto getPngLaunchGameDto(String codes) {
        PngLaunchGameDto dto = new PngLaunchGameDto();
        byte[] asBytes = Base64.getDecoder().decode(codes);
        try {
            String param = new String(asBytes, "utf-8");
            Map<String, String> mapRequest = CRequest.URLRequest(param);

            for (String strRequestKey : mapRequest.keySet()) {
                switch (strRequestKey) {
                    case "div":
                        dto.setDiv(mapRequest.get(strRequestKey));
                        break;
/*				case "gid":
					dto.setGid(mapRequest.get(strRequestKey));
					break;*/
                    case "gameid":
                        dto.setGameId(mapRequest.get(strRequestKey));
                        break;
                    case "lang":
                        dto.setLang(mapRequest.get(strRequestKey));
                        break;
                    case "pid":
                        dto.setPid(mapRequest.get(strRequestKey));
                        break;
                    case "username":
                        dto.setUsername(mapRequest.get(strRequestKey));
                        break;
                    case "brand":
                        dto.setBrand(mapRequest.get(strRequestKey));
                        break;
                    case "practice":
                        dto.setPractice(mapRequest.get(strRequestKey));
                        break;
                    case "terminal":
                        dto.setTerminal(new Byte(mapRequest.get(strRequestKey)));
                        break;
                    default:
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return dto;
    }
}
