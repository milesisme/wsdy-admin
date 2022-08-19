package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaiduResResultDto {
  private String bank_card_number; // 银行卡卡号
  private String valid_date;       // 有效期
  private Integer bank_card_type; // 银行卡类型，0:不能识别; 1: 借记卡; 2: 信用卡
  private String bank_name;       // 银行名，不能识别时为空
}
