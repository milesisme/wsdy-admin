package com.wsdy.saasops.modules.fund.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "FundAccLog", description = "会员操作资金日志表")
@Table(name = "fund_acc_log")
public class FundAccLog implements Serializable {
    private static final long serialVersionUID=1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "报文 json 字符串")
    private String content;

    @ApiModelProperty(value = "ip")
    private String ip;

    @ApiModelProperty(value = "createUser")
    private String createUser;

    @ApiModelProperty(value = "createTime")
    private String createTime;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "类型 0 线上入款 1 公司入款 2 会员提款 3 转账报表")
    private Integer type;

    public interface typeSign {
        Integer onLine = 0;
        Integer company = 1;
        Integer withdraw = 2;
        Integer report = 3;
    }
}