package com.wsdy.saasops.modules.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by William on 2018/3/14.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentTree {

    private Integer id;

    private String label;

    private Integer parentId;

    private List<AgentTree> children;

    private Boolean disabled =false ;

}
