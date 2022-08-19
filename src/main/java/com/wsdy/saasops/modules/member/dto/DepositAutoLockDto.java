package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepositAutoLockDto {
    @ApiModelProperty(value = "存款方式 {id: 999, label: '全部'},{id: 1, label: '网关QQ'},{id: 2, label: '网关微信'}," +
            "{id: 3, label: '网关京东'},{id: 4, label: '网关网银支付'},{id: 5, label: '网关支付宝'},{id: 7, label: '网关快捷支付'}," +
            "{id: 8, label: '网关银联扫码'},{id: 11, label: '网关卡转卡'},{id: 14, label: '银行卡转卡'},")
    private Integer depositType;
    @ApiModelProperty(value = "配置开始时间")
    private String startTime;
    @ApiModelProperty(value = "有效时长 小时")
    private Integer validHours;
    @ApiModelProperty(value = "未支付次数")
    private Integer notPayTimes;
    @ApiModelProperty(value = "锁定时长 分钟")
    private Integer lockTime;
    @ApiModelProperty(value = "是否发布私信 0否 1是")
    private Integer send;
    @ApiModelProperty(value = "私信内容")
    private String message;
}
