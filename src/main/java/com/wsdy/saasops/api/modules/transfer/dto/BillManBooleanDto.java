package com.wsdy.saasops.api.modules.transfer.dto;

import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import lombok.Data;

@Data
public class BillManBooleanDto {
    private Boolean isTransfer;
    private MbrBillManage mbrBillManage;
}
