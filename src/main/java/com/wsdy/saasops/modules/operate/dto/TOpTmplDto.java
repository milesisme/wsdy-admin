package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TOpTmplDto {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "活动分类编号")
    private String tmplCode;

    @ApiModelProperty(value = "活动分类名称")
    private String tmplName;

    @ApiModelProperty(value = "活动分类描述")
    private String tmplNameTag;

    @ApiModelProperty(value = "活动分类状态　1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "活动分类排序号")
    private Integer sortId;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "活动列表")
    private List<Map<String,Object>> actActivities;

}
