package com.wsdy.saasops.modules.sys.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tony
 * @create 2019-08-28 16:45
 */
@RestController
public class Health {
    public static final Map<String, String> HEALTH_RESULT = getResultMap();


    @RequestMapping("/v2/health")
    public Object getHealth() {
        return HEALTH_RESULT;
    }

    private static Map<String, String> getResultMap() {
        Map<String, String> resultMap = new HashMap<>(2);
        resultMap.put("code", "200");
        resultMap.put("message", "OK");
        return resultMap;
    }
}
