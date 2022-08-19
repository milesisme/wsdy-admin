package com.wsdy.saasops.modules.operate.controller;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.entity.OprNotice;
import com.wsdy.saasops.modules.operate.service.OprNoticeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Date;

@RestController
@RequestMapping("/bkapi/operate/oprnotice")
@Api(value = "OprNotice", tags = "")
public class OprNoticeController extends AbstractController {
    @Autowired
    private OprNoticeService oprNoticeService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;

    @GetMapping("/list")
    @RequiresPermissions("operate:oprnotice:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")
            ,@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R list(@ModelAttribute OprNotice oprNotice, @RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize,
                  @RequestParam(value = "orderBy", required = false) String orderBy) {
        return R.ok().putPage(oprNoticeService.queryListPage(oprNotice, pageNo, pageSize, orderBy));
    }

    @GetMapping("/info/{id}")
    @RequiresPermissions("operate:oprnotice:info")
    @ApiOperation(value = "信息", notes = "信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        OprNotice oprNotice = oprNoticeService.queryObject(id);

        return R.ok().put("oprNotice", oprNotice);
    }


    @PostMapping("/save")
    @RequiresPermissions("operate:oprnotice:save")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody OprNotice oprNotice, HttpServletRequest request) {
        oprNotice.setAvailable(Available.enable);
        oprNotice.setCreateUser(getUser().getUsername());
        oprNotice.setCreateTime(DateUtil.format(new Date(), FORMAT_18_DATE_TIME));
        oprNoticeService.save(oprNotice);

        //操作日志
        mbrAccountLogService.addOprNoticelog(oprNotice, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/update")
    @RequiresPermissions("operate:oprnotice:update")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody OprNotice oprNotice, HttpServletRequest request) {
        oprNotice.setUpdateUser(getUser().getUsername());
        oprNotice.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        oprNoticeService.update(oprNotice);
        //操作日志
        mbrAccountLogService.editOprNoticelog(oprNotice, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/available")
    @RequiresPermissions("operate:oprnotice:available")
    @ApiOperation(value = "修改状态", notes = "修改状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R available(@RequestBody OprNotice oprNoticeDto, HttpServletRequest request) {
        OprNotice oprNotice = new OprNotice();
        oprNotice.setId(oprNoticeDto.getId());
        oprNotice.setAvailable(oprNoticeDto.getAvailable());
        oprNotice.setUpdateUser(getUser().getUsername());
        oprNotice.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        oprNoticeService.update(oprNotice);
        //操作日志
        OprNotice newOprNotice = oprNoticeService.queryObject(oprNotice.getId());
        mbrAccountLogService.updateNoticeStatus(newOprNotice, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/delete")
    @RequiresPermissions("operate:oprnotice:delete")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody OprNotice oprNotice, HttpServletRequest request) {
        oprNoticeService.deleteBatch(oprNotice.getIds(), getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }
}
