package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.user.dto.TransferRequestDto;
import com.wsdy.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.wsdy.saasops.api.modules.user.service.DepotWalletService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;


@RestController
@RequestMapping("/api/depotWallet")
@Api(tags = "平台服务控制器")
public class DepotWalletController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private DepotWalletService depotWalletService;

    @Login
    @GetMapping("/depotBalance")
    @ApiOperation(value = "平台会员余额", notes = "平台会员余额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R depotBalance(HttpServletRequest request, @RequestParam("depotId") Integer depotId) {
        Assert.isNumeric(depotId, "平台Id不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, cpSite.getSiteCode());

        String dev = request.getHeader("dev");
        Byte terminal = HttpsRequestUtil.getHeaderOfDev(dev);
        if (Objects.isNull(gmApi)) {
            throw new RRException("对不起，暂无此游戏线路");
        }
        //TODO 判断该游戏平台是否是维护，如果是就不执行下面操作
        List<TGmDepot> tGmDepotList = tGmDepotService.findDepotList(userId, terminal, cpSite.getSiteCode(), Constants.EVNumber.zero);
        List<Byte> listAvailableWh = tGmDepotList.stream().filter(ls -> ls.getId().equals(depotId))
                .map(TGmDepot::getAvailableWh).collect(Collectors.toList());
        List<String> listDepotName = tGmDepotList.stream().filter(ls -> ls.getId().equals(depotId))
                .map(TGmDepot::getDepotName).collect(Collectors.toList());
        if (listAvailableWh.get(0).byteValue() == Constants.EVNumber.two) {
            throw new RRException(listDepotName.get(0) + "平台正在维护！");
        }
        return R.ok().put(depotWalletService.findDepotBalance(userId, gmApi)).put("depotId", depotId);
    }

    @Login
    @GetMapping("/findMbrAccountBalance")
    @ApiOperation(value = "查询账户总余额", notes = "查询账户总余额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R findMbrAccountBalance(HttpServletRequest request, @RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setAccountId(userId);
        MbrWallet mbrWal = mbrWalletMapper.selectOne(mbrWallet);
        String depotTotalBalance = RedisConstants.DEPOT_TOTAL_BALANCE + userId;
        List<UserBalanceResponseDto> userBalanceResponseDtoList = depotWalletService.findDepotAllBalance(userId, terminal, cpSite);
        Boolean isBo = redisService.setRedisExpiredTimeBo(depotTotalBalance, "userId" + userId, 60, TimeUnit.SECONDS);
        Double depotBalance;
        if (isBo) {
            depotBalance = userBalanceResponseDtoList.stream().filter(w -> nonNull(w.getBalance()))
                    .mapToDouble(w -> w.getBalance().doubleValue()).sum();
        } else {
            throw new R200Exception("玩命加载中,请稍等...");
        }
        return R.ok().put(mbrWal.getBalance().add(BigDecimal.valueOf(depotBalance)));
    }

    @Login
    @GetMapping("/transferList")
    @ApiOperation(value = "平台会员转账记录", notes = "平台会员转账记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R findTransferList(@ModelAttribute TransferRequestDto requestDto, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        requestDto.setAccountId(userId);
        return R.ok().put(depotWalletService.findTransferList(requestDto, pageNo, pageSize));
    }

}
