package com.wsdy.saasops.modules.operate.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author ${author}
 * @since 2022-05-21
 */
@Setter
@Getter
@ApiModel(value = "HelpGuessAsk", description = "客服帮助，猜你想问")
@Table(name = "help_guess_ask")
public class HelpGuessAsk implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 自增id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 问题
     */
    private String question;

    /**
     * 帮助中心内容
     */
    private String answer;

    /**
     * 排序号
     */
    private Integer orderNo;

    /**
     * 是否启用
     */
    private Boolean isOpen;

    /**
     * 更新人
     */
    private String updater;

    /**
     * 更新时间
     */
    private Date updatetime;

}
