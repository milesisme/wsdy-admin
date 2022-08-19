package com.wsdy.saasops.modules.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreeMenuDto3 {
    // 顶级权限
    private Long FId;           // 权限id
    private String FName;       // 权限名
    private Integer FType;      // 权限类型
    private Long FParentId;     // 权限父节点id

    private Long SId;
    private String SName;
    private Integer SType;
    private Long SParentId;

    private Long TId;
    private String TName;
    private Integer TType;
    private Long TParentId;

    private Long GId;
    private String GName;
    private Integer GType;
    private Long GParentId;

    private Long LId;
    private String LName;
    private Integer LType;
    private Long LParentId;

    private Long MId;
    private String MName;
    private Integer MType;
    private Long MParentId;
}
