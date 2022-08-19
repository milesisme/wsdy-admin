package com.wsdy.saasops.api.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DepotManageOperation implements Serializable {


    private boolean enableLoggingOut =false;

    private boolean enableLock =false;
}
