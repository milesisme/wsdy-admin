package com.wsdy.saasops.modules.operate.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AuditDepot {

    private Integer depotId;

    private List<Integer> games;
}
