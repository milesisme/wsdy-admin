package com.wsdy.saasops.modules.system.pay.entity;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;

@Getter
@Setter
@ApiModel(value = "set_basic_cryptocurrencies_bank", description = "数字加密银行关联表")
@Table(name = "set_basic_cryptocurrencies_bank")
public class SetBasicCryptoCurrenciesBank {

    private Integer currenciesId;

    private Integer bankId;

}
