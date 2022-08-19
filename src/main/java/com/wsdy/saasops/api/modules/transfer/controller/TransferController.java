package com.wsdy.saasops.api.modules.transfer.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transfer.dto.DepotFailDtosDto;
import com.wsdy.saasops.api.modules.transfer.service.TransferService;
import com.wsdy.saasops.api.modules.transferNew.service.DepotService;
import com.wsdy.saasops.api.modules.user.service.DepotOperatService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.dto.DepotFailDto;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.api.modules.transferNew.service.TransferNewService.DEPOT_BALANCE;
import static java.util.Objects.isNull;

@RestController
@RequestMapping("/api/pay")
@Api(tags = "会员转账")
public class TransferController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private DepotService depotService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private DepotOperatService depotOperatService;
    @Autowired
    private MbrAccountService  mbrAccountService;

    @Login
    @PostMapping("transferIn")
    @ApiOperation(value = "会员单个平台转出，中心钱包->平台", notes = "会员单个平台转出，中心钱包->平台")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R accountTransferOut(@RequestBody BillRequestDto requestDto, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        requestDto.setAccountId(accountId);
        requestDto.setLoginName(loginName);
        requestDto.setIp(CommonUtil.getIpAddress(request));
        String dev = getTransferSourceDev(requestDto, request);
        requestDto.setDev(dev);
        String siteCode = CommonUtil.getSiteCode();
        String key = RedisConstants.ACCOUNT_DEPOT_TRANSFER + siteCode + accountId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                depotService.accountBonusTransferOut(requestDto, siteCode);
            } finally {
                redisService.del(key);
            }
        }
        return R.ok();
    }

    @Login
    @PostMapping("transferInPlatform")
    @ApiOperation(value = "会员单转入平台，中心钱包->平台", notes = "会员单转入平台，中心钱包->平台")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R transferInPlatform(@RequestBody BillRequestDto requestDto, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        requestDto.setAccountId(accountId);
        requestDto.setLoginName(loginName);
        requestDto.setIp(CommonUtil.getIpAddress(request));
        String dev = getTransferSourceDev(requestDto, request);
        requestDto.setDev(dev);
        String siteCode = CommonUtil.getSiteCode();


        // 判断跳转平台是否维护
        // 1.查询站点所有平台
        List<TGmDepot> tGmDepotList = tGmDepotService.findDepotList(accountId, requestDto.getTerminal(), siteCode, Constants.EVNumber.zero);
        // 2.获取跳转平台的维护状态
        List<Byte> listAvailableWh = tGmDepotList.stream().filter(ls -> ls.getId().equals(requestDto.getDepotId()))
                .map(TGmDepot::getAvailableWh).collect(Collectors.toList());
        // 3.获取跳转的平台名称
        List<String> listDepotName = tGmDepotList.stream().filter(ls -> ls.getId().equals(requestDto.getDepotId()))
                .map(TGmDepot::getDepotName).collect(Collectors.toList());
        // 4.跳转的平台维护，返回提示
        if (Collections3.isNotEmpty(listAvailableWh) && listAvailableWh.get(0).byteValue() == Constants.EVNumber.two) {
            throw new R200Exception("目前" + listDepotName.get(0) + "正在维护，维护期间将无法转账" + listDepotName.get(0) + "游戏，请先娱乐其他平台游戏！");
        }
        if (Collections3.isEmpty(listAvailableWh)) {
            throw new R200Exception("目前维护中");
        }

        String key = RedisConstants.ACCOUNT_DEPOT_TRANSFER + siteCode + accountId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                depotService.accountTransferOut(requestDto, siteCode);
            } finally {
                redisService.del(key);
            }
        }
        return R.ok();
    }


    @Login
    @GetMapping("transferOut")
    @ApiOperation(value = "会员单个平台转入，平台 -》中心钱包", notes = "会员单个平台转入，平台 -》中心钱包")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R accountTransferIn(BillRequestDto requestDto, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        requestDto.setAccountId(accountId);
        requestDto.setLoginName(loginName);
        requestDto.setIp(CommonUtil.getIpAddress(request));
        String dev = getTransferSourceDev(requestDto, request);
        requestDto.setDev(dev);
        String siteCode = CommonUtil.getSiteCode();
        depotService.accountTransferIn(requestDto, siteCode);
        return R.ok();
    }


    @Login
    @PostMapping("transferOutPlatform")
    @ApiOperation(value = "会员单个平台转入，平台 -》中心钱包", notes = "会员单个平台转入，平台 -》中心钱包")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R transferOutPlatform(@RequestBody  BillRequestDto requestDto, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        requestDto.setAccountId(accountId);
        requestDto.setLoginName(loginName);
        requestDto.setIp(CommonUtil.getIpAddress(request));
        String dev = getTransferSourceDev(requestDto, request);
        requestDto.setDev(dev);
        String siteCode = CommonUtil.getSiteCode();

        // 判断跳转平台是否维护
        // 1.查询站点所有平台
        List<TGmDepot> tGmDepotList = tGmDepotService.findDepotList(accountId, requestDto.getTerminal(), siteCode, Constants.EVNumber.zero);
        // 2.获取跳转平台的维护状态
        List<Byte> listAvailableWh = tGmDepotList.stream().filter(ls -> ls.getId().equals(requestDto.getDepotId()))
                .map(TGmDepot::getAvailableWh).collect(Collectors.toList());
        // 3.获取跳转的平台名称
        List<String> listDepotName = tGmDepotList.stream().filter(ls -> ls.getId().equals(requestDto.getDepotId()))
                .map(TGmDepot::getDepotName).collect(Collectors.toList());
        // 4.跳转的平台维护，返回提示
        if (Collections3.isNotEmpty(listAvailableWh) && listAvailableWh.get(0).byteValue() == Constants.EVNumber.two) {
            throw new R200Exception("目前" + listDepotName.get(0) + "正在维护，维护期间将无法转账" + listDepotName.get(0) + "游戏，请先娱乐其他平台游戏！");
        }
        if (Collections3.isEmpty(listAvailableWh)) {
            throw new R200Exception("目前维护中");
        }


        depotService.accountTransferOutPlatform(requestDto, siteCode);
        return R.ok();
    }

    @ApiOperation(value = "获取客户端", notes = "获取客户端")
    public String getTransferSourceDev(BillRequestDto requestDto, HttpServletRequest request) {
        String dev = request.getHeader("dev");
        Byte transferSource = HttpsRequestUtil.getHeaderOfDev(dev);
        requestDto.setTransferSource(transferSource);
        return dev;
    }

    @Login
    @GetMapping("/checkTransfer")
    @ApiOperation(value = "会员转账单查询", notes = "会员转账单查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R checkTransfer(@RequestParam("orderNo") Long orderNo, HttpServletRequest request) {
        Assert.isNull(orderNo, "订单号不能为空");
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        return transferService.checkTransfer(orderNo, cpSite.getSiteCode());
    }

    @GetMapping("/recoverBalance")
    @ApiOperation(value = "会员第三方账号余额回收", notes = "会员第三方账号余额回收")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R recoverBalance(HttpServletRequest request) {
        // 入参获取
        String ip = CommonUtil.getIpAddress(request);
        String dev = request.getHeader("dev");
        Byte transferSource = HttpsRequestUtil.getHeaderOfDev(dev);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);

        // 查询会员已转账平台钱包
        MbrDepotWallet wallet = new MbrDepotWallet();
        wallet.setAccountId(userId);
        wallet.setLoginName(loginName);
        List<MbrDepotWallet> depotWallets = mbrWalletService.getDepotWallet(wallet);
        if (Collections3.isEmpty(depotWallets)) {
            return R.ok("没有第三方账号需要回收余额!");
        }
        // 会员已转账平台
        List<TGmDepot> tGmDepotList = tGmDepotService.findDepotList(userId, transferSource, CommonUtil.getSiteCode(), Constants.EVNumber.zero);
        // 会员已转账平台中的维护状态的平台
        List<Integer> listDepotId = tGmDepotList.stream().filter(ls -> ls.getAvailableWh() == Constants.EVNumber.two)
                .map(TGmDepot::getId).collect(Collectors.toList());
        // 会员已转账平台中非维护状态的平台钱包
        depotWallets.removeAll(listDepotId);

        // 回收平台余额
        String key = RedisConstants.ACCOUNT_DEPOT_TRANSFER + CommonUtil.getSiteCode() + userId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, userId, 20, TimeUnit.SECONDS);
        if (isExpired) {
            DepotFailDtosDto depotFailDtosDto = new DepotFailDtosDto();
            depotFailDtosDto.setIp(ip);
            depotFailDtosDto.setDev(dev);
            depotFailDtosDto.setUserId(userId);
            depotFailDtosDto.setLoginName(loginName);
            depotFailDtosDto.setDepotWallets(depotWallets);
            depotFailDtosDto.setTransferSource(transferSource);
            depotFailDtosDto.setSiteCode(CommonUtil.getSiteCode());
            // 批量回收余额，并返回转账结果
            List<DepotFailDto> errDepotFails = mbrWalletService.getDepotFailDtos(depotFailDtosDto);
            depotFailDtosDto.getRecoverBalanceList().addAll(errDepotFails); // recoverBalanceList : 转账信息

            // 平台小于1的错误过滤掉，平台都是小于的错误不需要返回，前端已经校验，如果所有平台小于1则提示无可回收余额
            List<DepotFailDto> failList = depotFailDtosDto.getRecoverBalanceList().stream().filter(e ->
                    e.getFailError().equals(Boolean.FALSE) && !DEPOT_BALANCE.equals(e.getError())).collect(Collectors.toList());

            redisService.del(key);
            // 返回错误信息给完整错误信息给前端
            if (failList.size() > 0) {
                // 错误信息组成： 平台id , 错误类型(稽核已废弃，所以恒为0 线路错误)， 错误信息msg
                return R.halfErrorList(
                        failList.stream().map(e -> String.valueOf(e.getDepotId())).collect(Collectors.joining(",")),
                        failList.stream().map(e -> String.valueOf(e.getIsSign())).collect(Collectors.joining(",")),
                        failList.stream().map(e -> e.getError()).collect(Collectors.joining(","))
                );
            }
        }
        return R.ok("回收第三方账户余额成功!");
    }

    @PostMapping("/checkBalance")
    @ApiOperation(value = "平台会员余额查询", notes = "平台会员余额查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R balance(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrWallet mbrWallet = mbrWalletService.getBalance(userId);
        return R.ok().put("balance", mbrWallet.getBalance());
    }



    @Login
    @GetMapping("loginOut")
    @ApiOperation(value = "用户登出")
    public R playerLoginOut(HttpServletRequest request, @RequestParam("depotCode") String depotCode) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        TGmDepot tGmDepot = depotOperatService.findTGmDepotByCode(depotCode);
        if (isNull(tGmDepot)) {
            throw new R200Exception("平台不存在");
        }
        return depotOperatService.LoginOutGateway(tGmDepot.getId(), accountId, CommonUtil.getSiteCode());
    }


    @Login
    @GetMapping("freeWalletSwitch")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiOperation(value = "设置手动或者免转")
    public R freeWalletSwitch(HttpServletRequest request, @RequestParam("status") Integer status) {
        Assert.isNull(status, "状态不能为空");

        if(status != Constants.EVNumber.zero && status != Constants.EVNumber.one){
            throw new R200Exception("状态错误");
        }
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        mbrAccountService.updateFreeWalletSwitch(accountId, status);
        return  R.ok();
    }

}
