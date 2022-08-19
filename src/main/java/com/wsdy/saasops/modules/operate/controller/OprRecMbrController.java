package com.wsdy.saasops.modules.operate.controller;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javassist.NotFoundException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.operate.dto.AgyAccDto;
import com.wsdy.saasops.modules.operate.entity.OprCustom;
import com.wsdy.saasops.modules.operate.entity.OprRecMbr;
import com.wsdy.saasops.modules.operate.service.OprRecMbrService;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/operate/oprrecmbr")
@Api(value = "OprRecMbr", tags = "")
public class OprRecMbrController extends AbstractController {
    @Autowired
    private OprRecMbrService oprRecMbrService;

    @GetMapping("/mbrList")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R mbrlist(@ModelAttribute MbrAccount mbrAccount){

        return R.ok().putPage(oprRecMbrService.queryMbrList(mbrAccount, "auth"));
    }


    @GetMapping("/agtList")
    @RequiresPermissions("operate:oprrecmbr:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R agtlist(@ModelAttribute AgyAccDto aad){

        return R.ok().putPage(oprRecMbrService.queryAgentList(aad, "auth"));
    }

    @GetMapping("/list")
    @RequiresPermissions("operate:oprrecmbr:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute OprRecMbr oprRecMbr, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value = "orderBy", required = false) String orderBy) throws NotFoundException {

        return R.ok().putPage(oprRecMbrService.queryListPage(oprRecMbr, pageNo, pageSize, orderBy, "auth"));
    }

    @PostMapping("/save")
    @RequiresPermissions("operate:oprrecmbr:save")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody OprRecMbr oprRecMbr, HttpServletRequest request) {
        Assert.isNull(oprRecMbr, "不能为空");
        Assert.isBlank(oprRecMbr.getContext(), "不能为空");
        if (oprRecMbr.getContext().length() > 200) {
            return R.error(2000, "内容长度最大为200字符");
        }
        oprRecMbrService.saveOprRecMbr(oprRecMbr, getUser().getUsername(), Constants.EVNumber.two, CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/deleteBatch")
    @RequiresPermissions("operate:oprrecmbr:delete")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody OprCustom oc, HttpServletRequest request) {
        oprRecMbrService.modifyOrm(oc.getOrmList(), getUser().getUsername(), CommonUtil.getIpAddress(request), Boolean.TRUE);
        return R.ok();
    }

    /*    @GetMapping("/exportExcel")
        @RequiresPermissions("operate:oprrecmbr:exportExcel")
        @ApiOperation(value="站内信列表", notes="站内信列表")
        @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
        public void exportExcel(@ModelAttribute OprRecMbr oprRecMbr,HttpServletResponse response) {
            oprRecMbrService.exportExcel(oprRecMbr,response);
        }
        */
    @PostMapping("/readMsg")
    @RequiresPermissions("operate:oprrecmbr:delete")
    @ApiOperation(value = "点击阅读站内信", notes = "点击阅读站内信")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R readMsg(@RequestBody OprRecMbr orm) {
        oprRecMbrService.readMsg(orm);
        return R.ok();
    }

}
