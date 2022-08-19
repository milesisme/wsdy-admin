package com.wsdy.saasops.modules.operate.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.operate.entity.TGameLogo;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import com.wsdy.saasops.modules.operate.service.TGmGameService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.List;

@RestController
@RequestMapping("/bkapi/operate/tgmgame")
@Api(value = "TGmGame", tags = "")
public class TGmGameController {
    @Autowired
    private TGmGameService tGmGameService;


    @RequiresPermissions("operate:tgmgame:info")
    @ApiOperation(value = "信息", notes = "信息")
    @GetMapping("/info/{id}")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        TGmGame tGmGame = tGmGameService.queryObjectOne(id);
        return R.ok().put("tGmGame", tGmGame);
    }


    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @ApiOperation(value = "修改", notes = "修改")
    @PostMapping("/available")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R available(@RequestBody TGmGame tGmGame) {
        TGmGame record = new TGmGame();
        record.setId(tGmGame.getId());
        record.setAvailable(tGmGame.getAvailable());
        tGmGameService.update(record);
        return R.ok();
    }


    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @ApiOperation(value = "修改", notes = "修改")
    @PostMapping("/enablePc")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R enablePc(@RequestBody TGmGame tGmGame) {
        TGmGame record = new TGmGame();
        record.setId(tGmGame.getId());
        record.setEnablePc(tGmGame.getEnablePc());
        tGmGameService.update(record);
        return R.ok();
    }


    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @PostMapping("/enableMb")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R enableMb(@RequestBody TGmGame tGmGame) {
        TGmGame record = new TGmGame();
        record.setId(tGmGame.getId());
        record.setEnableMb(tGmGame.getEnableMb());
        tGmGameService.update(record);
        return R.ok();
    }


    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @PostMapping("/enableTest")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R enableTest(@RequestBody TGmGame tGmGame) {
        TGmGame record = new TGmGame();
        record.setId(tGmGame.getId());
        record.setEnableTest(tGmGame.getEnableTest());
        tGmGameService.update(record);
        return R.ok();
    }


    @RequiresPermissions("operate:tgmcat:gameList")
    @GetMapping("/gameCatList")
    @ApiOperation(value = "游戏分类列表", notes = "游戏分类列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R gameCatList(@ModelAttribute TGmDepot tGmDepot, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, String orderBy) {
        return R.ok().put("page", tGmGameService.queryGmCatList(tGmDepot, pageNo, pageSize, orderBy));
    }


    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @GetMapping("/exportGameCatExcel")
    @ApiOperation(value = "导出游戏分类列表", notes = "导出游戏分类列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void exportGameCatExcel(@ModelAttribute TGmDepot tGmDepot, HttpServletResponse response) {
        tGmGameService.exportGameCatExcel(tGmDepot, response);
    }


    @RequiresPermissions("operate:tgmcat:gameList")
    @GetMapping("/gameList")
    @ApiOperation(value = "游戏列表", notes = "游戏列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R gameList(@ModelAttribute TGmGame tGmGame, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, String orderBy) {
        return R.ok().put("page", tGmGameService.queryTGmGameList(tGmGame, pageNo, pageSize, orderBy));
    }


    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @GetMapping("/exportGameExcel")
    @ApiOperation(value = "导出游戏列表", notes = "导出游戏列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void exportGameExcel(@ModelAttribute TGmGame tGmGame, HttpServletResponse response) {
        tGmGameService.exportGameExcel(tGmGame, response);
    }

    @GetMapping("/findGameType")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findGameType() {
        return R.ok().put("page", tGmGameService.findGameType());
    }

    @GetMapping("/findGameTypeEx")
    @ApiOperation(value = "查询输赢报表游戏类型", notes = "查询输赢报表游戏类型")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findGameTypeEx() {
        List<TGmCat>  cat = tGmGameService.findGameCatCode();
        TGmCat tip=new TGmCat();
        tip.setCatCode("Tip");
        tip.setCatName("小费");
        cat.add(tip);
        TGmCat others=new TGmCat();
        others.setCatCode("Others");
        others.setCatName("其他");
        cat.add(others);
        return R.ok().put("page", cat);
    }


    @GetMapping("/listGame")
    @ApiOperation(value = "查询分类下平台信息（统计）", notes = "查询分类下平台信息（统计）")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listGame(@ModelAttribute TGameLogo tGameLogo, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        tGameLogo.setSiteCode(CommonUtil.getSiteCode());
        if (StringUtils.isEmpty(tGameLogo.getTerminal())) {
            tGameLogo.setTerminal(ApiConstants.Terminal.pc);
        }
        return R.ok().put("page", tGmGameService.queryListGamePage(tGameLogo, pageNo, pageSize));
    }

    @GetMapping(value = "/listCatDepot")
    @ApiOperation(value = "查询分类下的游戏平台", notes = "查询分类下的游戏平台")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listCatDepot(@RequestParam("catId") Integer catId) {
        return R.ok().putPage(tGmGameService.findCatGameDepot(catId,CommonUtil.getSiteCode()));
    }

    @GetMapping("/findGameList")
    @ApiOperation(value = "根据平台查询游戏列表", notes = "根据平台查询游戏列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findGameList(@ModelAttribute TGmGame tGmGame, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", tGmGameService.findGameList(tGmGame, pageNo, pageSize));
    }
    
    @GetMapping("/findGameHasSubcatList")
    @ApiOperation(value = "根据平台查询游戏列表，且为手机端的", notes = "根据平台查询游戏列表，且为手机端的")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findGameHasSubcatList(@ModelAttribute TGmGame tGmGame, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
    	return R.ok().put("page", tGmGameService.findGameHasSubcatList(tGmGame, pageNo, pageSize));
    }

    @PostMapping("/update")
    @ApiOperation(value = "单个游戏修改", notes = "单个游戏修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody TGmGame tGmGame) {
        tGmGameService.update(tGmGame);
        return R.ok();
    }

}
