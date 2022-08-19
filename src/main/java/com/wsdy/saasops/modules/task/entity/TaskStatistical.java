package com.wsdy.saasops.modules.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@ApiModel(value = "TaskStatistical", description = "")
@Table(name = "task_statistical")
public class TaskStatistical implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "configId")
    private Integer configId;

    @ApiModelProperty(value = "点击量")
    private Long number;
}