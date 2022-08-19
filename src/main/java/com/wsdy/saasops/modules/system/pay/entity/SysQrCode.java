package com.wsdy.saasops.modules.system.pay.entity;

import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "SysQrCode", description = "站点个人二维码")
@Table(name = "set_basic_sys_qrcode")
public class SysQrCode {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "状态 :0：禁用，1：启用")
    private Integer available;

    @ApiModelProperty(value = "支付名称")
    private String name;

    @ApiModelProperty(value = "前台展示名称")
    private String showName;

    @ApiModelProperty(value = "手续费上限金额CNY")
    private BigDecimal feeTop;

    @ApiModelProperty(value = "手续费 按比例收费")
    private BigDecimal feeScale;

    @ApiModelProperty(value = "固定收费")
    private BigDecimal feeFixed;

    @ApiModelProperty(value = "收费的方式 (0-按比例收费 1固定收费)")
    private Integer feeWay;

    @ApiModelProperty(value = "每日最大存款金额")
    private BigDecimal dayMaxAmout;

    @ApiModelProperty(value = "二维码图片文件名")
    private String qrImgFileName;

    @ApiModelProperty(value = "二维码图片文件地址")
    private String qrImgUrl;

    @ApiModelProperty(value = "")
    private String createUser;

    @ApiModelProperty(value = "")
    private String createTime;

    @ApiModelProperty(value = "")
    private String modifyUser;

    @ApiModelProperty(value = "")
    private String modifyTime;

    @ApiModelProperty(value = "日已存款金额")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "是否删除 逻辑删除 0否 1是")
    private Integer isDelete;

    @ApiModelProperty(value = "限额模式 0区间限额 1固定金额")
    private Integer amountType;

    @ApiModelProperty(value = "单笔存款最小值")
    private BigDecimal minAmout;

    @ApiModelProperty(value = "单笔存款最大值")
    private BigDecimal maxAmout;

    @ApiModelProperty(value = "单笔固定金额")
    private String fixedAmount;
    
    @ApiModelProperty(value = "是否热门：true 是 false 不是")
    private Boolean isHot;
    
    @ApiModelProperty(value = "是否推荐：true 是 false 不是")
    private Boolean isRecommend;

    @Transient
    @ApiModelProperty(value = "会员组")
    private List<MbrGroup> groupList;

    @Transient
    @ApiModelProperty(value = "开户银行id,t_bs_bank主键id")
    private List<Integer> bankIds;

    @Transient
    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @Transient
    private String devSource;

    @Transient
    @ApiModelProperty(value = "会员组id")
    private Integer groupId;

    @Transient
    @ApiModelProperty(value = "asc,desc")
    private String sortBy;


    @Transient
    @ApiModelProperty(value = "会员组ids")
    private List<Integer> groupIds;

    @Transient
    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;

    @Transient
    @ApiModelProperty(value = "会员组ids")
    private List<BaseBank> bankList;

    @Transient
    @ApiModelProperty(value = "1 已分配到会员组，0 未分配到会员组")
    private Integer selected;

    @Transient
    @ApiModelProperty(value = "扫码支持的类型")
    private String bankNamesStr;

    @Transient
    @ApiModelProperty(value = "0 跳转，1不跳转（前端处理生成二维码）")
    private Integer urlMethod = 1;

    @Transient
    @ApiModelProperty(value = "扫码logo")
    private String ewmLogo;

    @Transient
    @ApiModelProperty(value = "bankLogo")
    private String bankLogo;

    @Transient
    @ApiModelProperty(value = "不可用logo")
    private String disableLogo;

    @Transient
    @ApiModelProperty(value = "1 按银行名称排序，2 按限红排序 3 按时间排序")
    private Integer sortItem;

}
