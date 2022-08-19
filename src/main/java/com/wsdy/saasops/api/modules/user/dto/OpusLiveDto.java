package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Getter
@Setter
public class OpusLiveDto {
    private String secretKey;
    private String operatorId;
    private String siteCode;
    private String productCode;
    private String memberId;
    private String language;
    private String currency;
    private BigDecimal amount;
    private String referenceId;
    private String remark;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (!StringUtils.isEmpty(secretKey)) {
            buffer.append("&secret_key=").append(secretKey);
        }
        if (!StringUtils.isEmpty(operatorId)) {
            buffer.append("&operator_id=").append(operatorId);
        }
        if (!StringUtils.isEmpty(siteCode)) {
            buffer.append("&site_code=").append(siteCode);
        }
        if (!StringUtils.isEmpty(productCode)) {
            buffer.append("&product_code=").append(productCode);
        }
        if (!StringUtils.isEmpty(memberId)) {
            buffer.append("&member_id=").append(memberId);
        }
        if (!StringUtils.isEmpty(language)) {
            buffer.append("&Language=").append(language);
        }
        if (!StringUtils.isEmpty(currency)) {
            buffer.append("&currency=").append(currency);
        }
        if (!StringUtils.isEmpty(amount)) {
            buffer.append("&amount=").append(amount);
        }
        if (!StringUtils.isEmpty(referenceId)) {
            buffer.append("&reference_id=").append(referenceId);
        }
        if (!StringUtils.isEmpty(remark)) {
            buffer.append("&remark=").append(remark);
        }
        if (!StringUtils.isEmpty(buffer)) {
            buffer.delete(0, 1);
        }
        return buffer.toString();
    }
}
