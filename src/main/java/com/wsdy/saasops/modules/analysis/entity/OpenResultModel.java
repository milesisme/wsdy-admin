package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OpenResultModel {

    private String type;
    private List<Map<String,String>> resultMap;

    public OpenResultModel(){

    }
    public OpenResultModel(String type, List<Map<String,String>> map){
        this.type=type;
        this.resultMap=map;
    }

}
