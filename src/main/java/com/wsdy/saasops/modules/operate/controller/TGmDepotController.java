package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.common.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/operate/tgmdepot")
@Api(tags = "游戏平台")
public class TGmDepotController {

    @Autowired
    private TGmDepotService tGmDepotService;

    @GetMapping("/listAll")
    @ApiOperation(value = "站点平台", notes = "站点平台")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listAll() {
        return R.ok().put("tGmDepots", tGmDepotService.findSiteTGmDepotList(CommonUtil.getSiteCode()));
    }

    @GetMapping("/listDepotCatAll")
    @ApiOperation(value = "平台分类信息列表", notes = "平台分类信息列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listDepotCatAll() {
        return R.ok().put("tDepotCats", tGmDepotService.findDepotCatList());
    }

    @GetMapping("/findCatDepot")
    @ApiOperation(value = "根据分类ID查询对应的平台", notes = "根据分类ID查询对应的平台")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findCatDepot(Integer catId) {
        return R.ok().put(tGmDepotService.findCatDepot(catId));
    }


    @GetMapping("catList")
    @ApiOperation(value = "查询本站点 所有分类", notes = "查询本站点 所有分类")
    public R catList() {
        return R.ok().put(tGmDepotService.findCatListBySiteCode(CommonUtil.getSiteCode()));
    }
}
