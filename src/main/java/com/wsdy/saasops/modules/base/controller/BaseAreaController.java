package com.wsdy.saasops.modules.base.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.entity.BaseArea;
import com.wsdy.saasops.modules.base.service.BaseAreaService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/base/baseArea")
@Api(value = "BaseArea", tags = "地区基本信息")
public class BaseAreaController {
    @Autowired
    private BaseAreaService baseAreaService;

    @RequestMapping("/listProv")
    @ApiOperation(value="地区基本信息省-列表", notes="显中国所有省份")
    public R listProv() {
    	BaseArea sysBaseArea = new BaseArea();
        return R.ok().put("provs",baseAreaService.findArea(sysBaseArea));
    }


    @RequestMapping("/listCity")
    @ApiOperation(value="地区基本信息城市-列表", notes="显中国某一省份下有的城市")
    public R info(@RequestBody BaseArea baseAreaModel) {
    	
    	BaseArea sysBaseArea = new BaseArea();
			sysBaseArea.setProv(baseAreaModel.getProv());
			return R.ok().put("citys",baseAreaService.findArea(sysBaseArea));

    }

}
