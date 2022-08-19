package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Table(name = "mbr_message_info")
public class MbrMessageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "messageId")
    private Integer messageId;
    @ApiModelProperty(value = "文本内容")
    private String textContent;
    @ApiModelProperty(value = "图片url 只存key")
    private String imageUrl;
    @ApiModelProperty(value = "回复时间or提交时间")
    private String createTime;
    @ApiModelProperty(value = "createUser")
    private String createUser;
    @ApiModelProperty(value = "0 会员 1后台客服(私信) 2系统消息")
    private Integer isSign;
    @ApiModelProperty(value = "会员是否已读：0 未读 1已读; 查询时含义：是否把该条查询的消息置为会员已读  1不处理 0处理为会员已读")
    private Integer isRead;
    @ApiModelProperty(value = "管家是否已读：0 未读 1已读")
    private Integer isReadSys ;
    @ApiModelProperty(value = "消息删除标志：0 未删除 1已删除")
    private Integer isDelete ;
    @ApiModelProperty(value = "推送私信过期时间 ")
    private String expirationTime;

    @Transient
    @ApiModelProperty(value = "会员id")
    private Integer accountId;
    @Transient
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @Transient
    @ApiModelProperty(value = "会员名list")
    private List<String> loginNameList;
    @Transient
    @ApiModelProperty(value = "代理list")
    private List<Integer> agyList;
    @Transient
    @ApiModelProperty(value = "会员组list")
    private List<Integer> groupList;
    @Transient
    @ApiModelProperty(value = "查询：消息类型  0全部消息，1.人工消息，2.管家消息")
    private Integer msgType;
    @Transient
    @ApiModelProperty(value = "查询：查询已读未读状态：1: 查询会员未读的消息(收件箱+通知)； 0：查询会员已读+未读")
    private Integer mbrIsRead;
    @Transient
    private Integer infoId;
    @Transient
    @ApiModelProperty(value = "1 下翻  0上翻")
    private Integer isMessage;
    @Transient
    @ApiModelProperty(value = "數量")
    private Integer num;
    @Transient
    @ApiModelProperty(value = "0 单条通知置为已读； 1 会员所有通知置为已读")
    private Integer setReadType;

    @Transient
    @ApiModelProperty(value = "0 不推送； 1 推送")
    private Integer isPush;
    @Transient
    @ApiModelProperty(value = "0 非设备推送: 所有会员； 1 设备推送 2 非设备推送: 指定会员/会员组 ")
    private Integer isAllDevice;
    @Transient
    @ApiModelProperty(value = "推送标题")
    private String pushTitle;
    @Transient
    @ApiModelProperty(value = "推送内容")
    private String pushContent;
    @Transient
    @ApiModelProperty(value = "站点code")
    private String siteCode;

    @Transient
    @ApiModelProperty(value = "极光推送离线保存时长 单位：小时")
    private long timeToLive;

}