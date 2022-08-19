package com.wsdy.saasops.modules.system.pay.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 小额客服线
 * </p>
 *
 */
@Setter
@Getter
@ApiModel(value = "SetSmallAmountLine", description = "小额客服线")
@Table(name = "set_small_amount_line")
public class SetSmallAmountLine implements Serializable {

    private static final long serialVersionUID=1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 支付名称
     */
    private String name;

    /**
     * 会员组id对应mbr_group.id
     */
    private String groupIds;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 操作人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否打开 默认是
     */
    private Boolean available;
    

    /**
     *  当日点击量
     */
    @Transient
    private long todatCount;

}
