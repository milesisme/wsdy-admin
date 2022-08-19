package com.wsdy.saasops.modules.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William on 2018/3/21.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreeMenuDto2 {

    private Long id;

    private String label;

    private Integer level;

    private Integer type;

    List<TreeMenuDto2> children = new ArrayList<>();
}
