package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Table(name = "mbr_opinion")
public class MbrOpinion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "文本内容")
    private String textContent;

    @ApiModelProperty(value = "图片url 只存key")
    private String imageUrl;

    @ApiModelProperty(value = "提交时间")
    private String createTime;

    @ApiModelProperty(value = "1待处理 2已处理")
    private Integer status;

    @ApiModelProperty(value = "修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "修改时间")
    private String modifyUser;

    @ApiModelProperty(value = "1 提款问题 2 游戏问题 3 优惠问题 4 修改资料 5 流水问题 6 程序错误 7 产品建议 8 其他 9存款问题")
    private Integer type;

    @Transient
    @ApiModelProperty(value = "真实名称")
    private String realName;

    @Transient
    @ApiModelProperty(value = "VIP")
    private String tierName;

    @Transient
    @ApiModelProperty(value = "活动层级id")
    private Integer actLevelId;
}