package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import com.wsdy.saasops.modules.operate.service.TGmGameService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/operate/gameredlimit")
@Api(tags = "游戏列表-限红设置")
public class GameRedLimitController {
    @Autowired
    private TGmGameService tGmGameService;

    @GetMapping("/findDepotList")
    @ApiOperation(value = "查询需要限红的平台列表", notes = "查询需要限红的平台列表")
    public R findDepotList(@RequestParam("pageNo") @NotNull Integer pageNo,
                           @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", tGmGameService.findDepotList(CommonUtil.getSiteCode(), pageNo, pageSize));
    }

    @GetMapping("/listDepotGame")
    @ApiOperation(value = "根据游戏平台查询游戏名称", notes = "根据游戏平台查询游戏名称")
    public R listDepotGame(@ModelAttribute TGmGame tGmGame, @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", tGmGameService.listDepotGame(tGmGame, pageNo, pageSize));
    }


}
