package com.wsdy.saasops.api.modules.apisys.entity;

import com.wsdy.saasops.common.utils.DateUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_gm_apiprefix")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TGmApiprefix {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @ApiModelProperty(name = "id")
    private Integer id;

    @ApiModelProperty(name = "线路id")
    private String apiId;
    @ApiModelProperty(name = "线路前缀")
    private String prefix;
    @ApiModelProperty(name = "站点id")
    private Integer siteId;
    @ApiModelProperty(name = "1开启，0禁用，2维护")
    private Byte available;
    @ApiModelProperty(name = "创建人")
    private String createUser;
    @ApiModelProperty(name = "创建时间")
    private String createTime;
    @ApiModelProperty(name = "修改人")
    private String modifyUser;
    @ApiModelProperty(name = "修改时间")
    private String modifyTime;


    public TGmApiprefix(String apiId, String prefix, Integer siteId, Byte available, String createUser, String modifyUser) {
        this.apiId = apiId;
        this.prefix = prefix;
        this.siteId = siteId;
        this.available = available;
        this.createUser = createUser;
        this.createTime = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
        this.modifyUser = modifyUser;
        this.modifyTime = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
    }
}
