package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Getter
@Setter
public class OpusSbDto {
    private String operatorId;
    private String firstName;
    private String userName;
    private String language;
    private Integer oddsType;
    private String currency;
    private String transId;
    private Integer direction;
    private BigDecimal amount;
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (!StringUtils.isEmpty(operatorId)) {
            buffer.append("&operator_id=").append(operatorId);
        }
        if (!StringUtils.isEmpty(firstName)) {
            buffer.append("&first_name=").append(firstName);
        }
        if (!StringUtils.isEmpty(userName)) {
            buffer.append("&user_name=").append(userName);
        }
        if (!StringUtils.isEmpty(language)) {
            buffer.append("&Language=").append(language);
        }
        if (!StringUtils.isEmpty(oddsType)) {
            buffer.append("&odds_type=").append(oddsType);
        }
        if (!StringUtils.isEmpty(currency)) {
            buffer.append("&currency=").append(currency);
        }
        if (!StringUtils.isEmpty(transId)) {
            buffer.append("&trans_id=").append(transId);
        }
        if (!StringUtils.isEmpty(direction)) {
            buffer.append("&direction=").append(direction);
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
