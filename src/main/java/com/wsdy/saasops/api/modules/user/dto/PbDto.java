package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Getter
@Setter
public class PbDto {
    private String agentCode;
    //private String userCode;
    private String locale;
    private String userCode;
    private BigDecimal amount;
    //private String sport;
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (!StringUtils.isEmpty(agentCode)) {
            buffer.append("&agentCode=").append(agentCode);
        }
/*        if (!StringUtils.isEmpty(loginId))
            buffer.append("&loginId=").append(loginId);*/
        if (!StringUtils.isEmpty(locale)) {
            buffer.append("&locale=").append(locale);
        }
        if (!StringUtils.isEmpty(userCode)) {
            buffer.append("&userCode=").append(userCode);
        }
        if (!StringUtils.isEmpty(amount)) {
            buffer.append("&amount=").append(amount);
        }
        if (!StringUtils.isEmpty(buffer)) {
            buffer.delete(0, 1);
        }
        return buffer.toString();
    }
}
