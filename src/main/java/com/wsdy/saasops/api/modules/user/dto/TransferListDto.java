package com.wsdy.saasops.api.modules.user.dto;

import com.wsdy.saasops.common.utils.PageUtils;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TransferListDto extends PageUtils {
    private Double subtotalMoney;
    private Double totalMoney;
}
