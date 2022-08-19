package com.wsdy.saasops.modules.member.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

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

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.service.MbrDepotWalletService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/member/mbrwallet")
@Api(value = "MbrWallet", tags = "会员钱包")
public class MbrWalletController extends AbstractController {
	
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrDepotWalletService mbrDepotWalletService;

    @GetMapping("/list")
    @RequiresPermissions("member:mbrwallet:list")
    @ApiOperation(value="会员钱包", notes="会员钱包—列表信息")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R list(@ModelAttribute MbrDepotWallet mbrWallet, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,@RequestParam("userId") Integer userId,@RequestParam(value="orderBy",required=false) String orderBy) {
    	MbrDepotWallet depotwallet = new MbrDepotWallet();
    	depotwallet.setAccountId(userId);
		PageUtils pageUtils=mbrDepotWalletService.queryListPage(depotwallet,pageNo,pageSize,orderBy);
		return R.ok().put("page", pageUtils).put("mbrWallet",mbrWalletService.queryById(userId));
    }
    
    @GetMapping("/balances/{accountId}")
    @RequiresPermissions("member:mbrwallet:list")
    @ApiOperation(value="会员钱包", notes="会员钱包—列表信息")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R balancelist(@PathVariable("accountId") Integer accountId) {
		return R.ok().put("page", mbrWalletService.balancelist(accountId));
    }

    @GetMapping("/info/{accountId}")
    @RequiresPermissions("member:mbrwallet:info")
    @ApiOperation(value="会员钱包", notes="会员钱包—根据会员Id查询会员主账户信息")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
    public R info(@PathVariable("accountId") Integer accountId) {
        return R.ok().put("mbrWallet", mbrWalletService.queryById(accountId));
    }
	
	
	 /*会员资金人工减少*/
	 
	@GetMapping("SubtractView")
	@RequiresPermissions("member:mbrwallet:update")
	@ApiOperation(value="会员钱包", notes="会员钱包—增加一条会员钱包余额减少记录初始化")
	@SysLog(module = "会员钱包模块",methodText = "会员资金 人工减少申请")
	public R walletSubtractView(@RequestParam("loginName") String loginName, @RequestParam("accountId") Integer accountId) {
		MbrWallet record=new MbrWallet();
		record.setAccountId(accountId);
		record=mbrWalletService.queryObjectCond(record);
		SnowFlake snowFlake = new SnowFlake(2, 3);
		MbrBillDetail detail = new MbrBillDetail();
		detail.setAccountId(accountId);
		detail.setLoginName(loginName);
		detail.setOrderNo(snowFlake.nextId()+"");
		detail.setOrderPrefix(OrderConstants.FUND_ORDER_CODE_AM);
		return R.ok().put("detail", detail).put("wallet", record.getBalance());
	}
	
	/*会员资金人工减少记录*/
	@PostMapping("/walletSubtract")
	@RequiresPermissions("member:mbrwallet:update")
	@ApiOperation(value="会员钱包", notes="会员钱包—减少会员账户余额")
	@SysLog(module = "会员钱包模块",methodText = "会员资金 人工减少")
	public R walletSubtract(@RequestBody MbrBillDetail record) {
		MbrWallet mbrWallet =new MbrWallet();
		mbrWallet.setBalance(record.getAmount());
		mbrWalletService.walletSubtract(mbrWallet,record);
		return R.ok();
	}
	
	 /*会员资金批量增加*/
	 
	@GetMapping("addBatchView")
	@RequiresPermissions("member:mbrwallet:update")
	@ApiOperation(value="会员钱包", notes="会员钱包—批量增加会员余额")
	@SysLog(module = "会员钱包模块",methodText = "会员资金批量增加")
	public R walletAddBatchView(@RequestParam("accountId") Integer[] accountIds){
		//FIXME 此处应该与资金管理中调整 报表是一致
		SnowFlake snowFlake = new SnowFlake(2, 3);
		MbrBillDetail detail = new MbrBillDetail();
		List<MbrAccount> lists = mbrWalletService.listAccName(accountIds);
		if (lists != null) {
			for (int i = 0; i < lists.size(); i++) {
				MbrAccount account = lists.get(i);
				detail.getAccountIds()[i] = account.getId();
				detail.getLoginNames()[i] = account.getLoginName();
				detail.getOrderNos()[i] = snowFlake.nextId()+"";
			}
		}
		detail.setOrderPrefix(OrderConstants.FUND_ORDER_CODE_AA);
		return R.ok().put("detail", detail);
	}

}
