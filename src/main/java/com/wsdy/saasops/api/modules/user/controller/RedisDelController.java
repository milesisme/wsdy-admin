package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.modules.user.service.RedisDelService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/redis")
@Api(value = "删除Redis的KEY", tags = "删除Redis的KEY")
public class RedisDelController {

    @Autowired
    private RedisDelService redisDelService;

    @Autowired
    private RedisService redisService;

    @GetMapping("/redisCache")
    @ApiOperation(value = "删除Redis的KEY", notes = "删除Redis的KEY")
    public R redisDelete() {
        redisDelService.redisCache();
        return R.ok();
    }

    @GetMapping("/redisCacheByKey")
    @ApiOperation(value = "按key删除redis", notes = "按key删除redis")
    public R redisCacheByKey(@RequestParam("key")  @NotNull String key) {
        redisDelService.redisCacheByKey(key);
        return R.ok();
    }

    //@GetMapping("keys")
    public R keys(HttpServletRequest request){
        Set<String> keys = redisService.getKeys("*");
        Map<String, Object> map = new HashMap<>();
        for(String key :keys){
            Object value = redisService.get(key);
            map.put(key, value);
        }
        return R.ok(map);
    }
}
