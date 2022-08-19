package com.wsdy.saasops.modules.operate.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.wsdy.saasops.api.constants.ApiConstants;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.entity.TGmDepotcat;
import com.wsdy.saasops.modules.operate.service.TGmCatService;
import com.wsdy.saasops.modules.operate.service.TGmDepotcatService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/operate/tgmcat")
@Api(value = "TGmCat", tags = "")
public class TGmCatController {
    @Autowired
    private TGmCatService tGmCatService;
    @Autowired
    private TGmDepotcatService tGmDepotcatService;

    @GetMapping("/listBaseAll")
    @ApiOperation(value = "信息", notes = "列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listBaseAll() {
        TGmCat tGmCat = new TGmCat();
        tGmCat.setAvailable(Available.enable);
        tGmCat.setParentId(Constants.SYS_DEPOT_ID);
        List<TGmCat> tGmCats = tGmCatService.queryListCond(tGmCat);
        return R.ok().put("tGmCats", tGmCats);
    }

    @GetMapping("/listCatDepot")
    @ApiOperation(value = "根据游戏类别查找平台", notes = "根据游戏类别查找平台")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "", required = true, dataType = "Integer", paramType = "header")})
    public R listCatDepot(@RequestParam("catId") @NotNull Integer catId, @RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        List<TGmDepotcat> tGmCats = tGmDepotcatService.catDepotList(catId, terminal);
        return R.ok().put("tDepots", tGmCats);
    }

    @GetMapping("/listSubCat")
    @ApiOperation(value = "根据游戏类别查找子类别", notes = "根据游戏类别查找子别类")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "", required = true, dataType = "Integer", paramType = "header")})
    public R listSubCat(@RequestParam("catId") @NotNull Integer catId) {
        List<TGmCat> tGmCats = tGmCatService.querySubCat(catId);
        return R.ok().put("subCats", tGmCats);
    }

    @GetMapping("/listSubAll/{parentId}")
    @ApiOperation(value = "信息", notes = "列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listCatAll(@PathVariable("parentId") Integer parentId) {
        TGmCat tGmCat = new TGmCat();
        tGmCat.setParentId(parentId);
        List<TGmCat> tGmCats = tGmCatService.queryListCond(tGmCat);
        return R.ok().put("tGmCats", tGmCats);
    }


    @GetMapping("/gameList")
    @RequiresPermissions("operate:tgmcat:gameList")
    @ApiOperation(value = "游戏列表", notes = "游戏列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R gameList(@ModelAttribute TGmDepot tGmDepot, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, String orderBy) {

        return R.ok().put("page", tGmCatService.queryTGmCatList(tGmDepot, pageNo, pageSize, orderBy));
    }

    @GetMapping("/exportExcel")
    @RequiresPermissions("operate:tgmcat:exportExcel")
    @ApiOperation(value = "游戏列表", notes = "游戏列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void exportExcel(@ModelAttribute TGmDepot tGmDepot, HttpServletResponse response) {
        tGmCatService.exportExcel(response);
    }
}
