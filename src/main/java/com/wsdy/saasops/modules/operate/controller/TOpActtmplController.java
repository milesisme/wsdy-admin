package com.wsdy.saasops.modules.operate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.service.TOpActtmplService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/bkapi/operate/topacttmpl")
@Api(value = "TOpActtmpl", tags = "点击活动增加 显示活动的模板信息")
public class TOpActtmplController {

    @Autowired
    private TOpActtmplService tOpActtmplService;

    @GetMapping("/list")
    @ApiOperation(value = "查询所有", notes = "查询所有")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list() {
        TOpActtmpl tOpActtmpl = new TOpActtmpl();
        tOpActtmpl.setAvailable(Available.enable);
        List<TOpActtmpl>  acttmpls = tOpActtmplService.queryListCond(tOpActtmpl);
        Collections.sort(acttmpls, Comparator.comparing(TOpActtmpl::getSortId));
        return R.ok().put("page", acttmpls);
    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "根据id查询信息", notes = "根据id查询信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        return R.ok().put(tOpActtmplService.queryObject(id));
    }

}
