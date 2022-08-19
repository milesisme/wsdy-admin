package com.wsdy.saasops.modules.operate.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;



@Setter
@Getter
@ApiModel(value = "OprMsg", description = "运营管理-站内信消息")
@Table(name = "opr_msg")
public class OprMsg implements Serializable{
private static final long serialVersionUID=1L;
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@ApiModelProperty(value = "id")
private Integer id;

/*@ApiModelProperty(value = "消息接收对象(0会员，1代理) ")
private Byte msgType;
*/
@ApiModelProperty(value = "消息标题")
private String title;

@ApiModelProperty(value = "消息内容")
private String content;

@ApiModelProperty(value = "0会员，1代理")
private byte receiveType;

@ApiModelProperty(value = "发送时间")
private String sendTime;

@Transient
@ApiModelProperty(value = "阅读时间")
private String readTime;

@ApiModelProperty(value = "发送人账号")
private String sendloginName;

@Transient
@ApiModelProperty(value = "收件人登陆名")
private String loginName;


@Transient
@ApiModelProperty(value = "发送开始时间")

private String sendTimeStart;
@Transient
@ApiModelProperty(value = "发送结束时间")
private String sendTimeEnd;

@Transient
@ApiModelProperty(value = "阅读开始时间")

private String readTimeStart;
@Transient
@ApiModelProperty(value = "阅读结束时间")
private String readTimeEnd;

@Transient
@ApiModelProperty(value = "阅读状态(0未读，1已读)")
private String state;
@Transient
@ApiModelProperty(value = "账号列表")
private List<OprMsgAccTemp> accounts;

public interface ReceiveTypes{
	byte member=0;
	byte agent=1;
}
}