package com.wsdy.saasops.modules.sys.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限
 * Created by William on 2017/11/22.
 */
@Data
public class Authority implements Serializable {

    private String userId;

    //TODO 因为在rdis中存在值时，jsckson把rdsis中的json转化为实体类时报错： 原本使用内部类 jackJson 转换报找不到无参构造方法，后来换外部类，报找不到这个类，最终只能使用map
    private Map<String,Object> rowAuthority;

    private Map<String,Object> cosAuthority;


    public Authority() {}



    public Authority(String userId,Map<String,Object> rowAuthority) {
        this.userId = userId;
        this.rowAuthority =rowAuthority;
        this.cosAuthority =new HashMap<>();
        this.cosAuthority.put("userId",userId);
    }


}
