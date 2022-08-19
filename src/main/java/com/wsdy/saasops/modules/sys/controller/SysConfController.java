package com.wsdy.saasops.modules.sys.controller;


import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.entity.BaseArea;
import com.wsdy.saasops.modules.base.service.BaseAreaService;
import com.wsdy.saasops.modules.base.service.BaseBankService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 列权限接口类
 */
@RestController
@RequestMapping("/bkapi/sys/conf")
public class SysConfController {

    @Autowired
    private BaseAreaService baseAreaService;

    @Autowired
    private BaseBankService baseBankService;
    @GetMapping("/banks")
    @ApiOperation(value = "取款银行列表", notes = "取款银行列表")
    public R getBanks() {
        return R.ok().put("banks", baseBankService.selectAll());
    }


    @GetMapping("/provs")
    @ApiOperation(value = "地址省份", notes = "地址省份")
    public R getProvs() {
        BaseArea sysBaseArea = new BaseArea();
        return R.ok().put("provs", baseAreaService.findArea(sysBaseArea));
    }

    @GetMapping("/citys")
    @ApiOperation(value = "地址城市", notes = "地址城市")
    public R getCitys(@RequestParam("prov") String prov) {
        BaseArea sysBaseArea = new BaseArea();
        sysBaseArea.setProv(prov);
        return R.ok().put("citys", baseAreaService.findArea(sysBaseArea));
    }
}
