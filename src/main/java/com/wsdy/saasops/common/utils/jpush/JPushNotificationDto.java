package com.wsdy.saasops.common.utils.jpush;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ApiModel(value = "消息推送DTO")
public class JPushNotificationDto {
    @ApiModelProperty(value = "通知内容标题")
    private String notificationTitle;
    @ApiModelProperty(value = "消息内容标题")
    private String msgTitle;
    @ApiModelProperty(value = "消息内容")
    private String msgContent;
    @ApiModelProperty(value = "扩展字段")
    private Map<String, String>  extras;
    @ApiModelProperty(value = "别名或别名组")
    private List<String> alias;
    @ApiModelProperty(value = "推送文件id")
    private String file_id;
    @ApiModelProperty(value = "极光推送保存时长 单位：小时")
    private long timeToLive;
}
