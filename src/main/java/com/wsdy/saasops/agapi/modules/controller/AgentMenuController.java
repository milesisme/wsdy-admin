package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentMenuService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.base.entity.BaseArea;
import com.wsdy.saasops.modules.base.service.BaseAreaService;
import com.wsdy.saasops.modules.base.service.BaseBankService;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysMenuTree;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/agapi/n2")
public class AgentMenuController {

    @Autowired
    private AgentMenuService sysMenuService;
    @Autowired
    private BaseAreaService baseAreaService;
    @Autowired
    private BaseBankService baseBankService;

    /**
     * 导航菜单
     */
    @AgentLogin
    @GetMapping("/saasopsTree")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saasopsTree(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        String roles = sysMenuService.queryRoleList(account.getAgyAccount()).toString().replace("[", "").replace("]", "");
        List<SysMenuTree> menuList = sysMenuService.selectTreeByRole(roles, account);
        return R.ok().put("menuList", menuList);
    }

    /**
     * 导航菜单
     */
    @AgentLogin
    @GetMapping("/nav")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R nav(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        List<SysMenuEntity> menuList = sysMenuService.getUserMenuList(account);
        //Set<String> permissions = sysMenuService.getUserPermissions(account.getAgyAccount());
        return R.ok().put("menuList", menuList);
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

    @GetMapping("/banks")
    @ApiOperation(value = "取款银行列表", notes = "取款银行列表")
    public R getBanks() {
        return R.ok().put("banks", baseBankService.selectAll());
    }
}
