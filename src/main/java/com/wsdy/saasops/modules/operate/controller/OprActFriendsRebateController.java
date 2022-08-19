package com.wsdy.saasops.modules.operate.controller;


import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.dto.AddFriendRebateRewardDto;
import com.wsdy.saasops.modules.operate.dto.ReduceFriendRebateRewardDto;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@RestController
@RequestMapping("/bkapi/operate/friendsRebate")
public class OprActFriendsRebateController  extends AbstractController {

    @Autowired
    private OprActActivityService oprActActivityService;

    @GetMapping("/friendRebateRewardList")
    @ApiOperation(value = "查询返利列表", notes = "好友返利")
    @RequiresPermissions("operate:friendrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R friendRebateRewardList(@RequestParam(value = "loginName", required = false) String loginName, @RequestParam(value = "groupId", required = false) Integer groupId, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime,  @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActActivityService.friendRebateRewardList( loginName, startTime,  endTime,  groupId,  pageNo,  pageSize));
    }


    @GetMapping("/friendRebateRewardDetails")
    @ApiOperation(value = "查询返利列表详情", notes = "好友返利")
    @RequiresPermissions("operate:friendrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R friendRebateRewardDetails(@RequestParam("loginName") String loginName,  @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime,  @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActActivityService.friendRebateRewardDetails( loginName, startTime,  endTime,   pageNo,  pageSize));
    }


    @GetMapping("/friendRebateRewardDetailsSummary")
    @ApiOperation(value = "查询返利列表详情汇总", notes = "好友返利")
    @RequiresPermissions("operate:friendrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R friendRebateRewardDetailsSummary(@RequestParam("loginName") String loginName,  @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime) {
        return R.ok().put( "total",oprActActivityService.friendRebateRewardDetailsSummary( loginName, startTime,  endTime));
    }


    @GetMapping("/friendRebateList")
    @ApiOperation(value = "查询玩家好友返利信息列表", notes = "好友返利")
    @RequiresPermissions("operate:friendrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R friendRebateList(@RequestParam(value = "loginName", required = false) String loginName, @RequestParam(value = "groupId", required = false) Integer groupId, @RequestParam(value = "startTime", required = false)String startTime, @RequestParam(value = "endTime", required = false)String endTime,
                              @RequestParam(value = "firstChargeStartTime", required = false)String firstChargeStartTime, @RequestParam(value = "firstChargeEndTime", required = false)String firstChargeEndTime, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActActivityService.friendRebateList( loginName, startTime,  endTime,  groupId,  pageNo,  pageSize, firstChargeStartTime, firstChargeEndTime));
    }

    @GetMapping("/friendRebatePersonalList")
    @ApiOperation(value = "个人返利列表", notes = "好友返利")
    @RequiresPermissions("operate:friendrebate:rewardlist")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R friendRebatePersonalList(@RequestParam(value = "loginName") String loginName, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActActivityService.friendRebatePersonalList( loginName, pageNo,  pageSize));
    }


    //@GetMapping("/addFriendRebateReward")
    @ApiOperation(value = "手动增加", notes = "好友返利")
    @RequiresPermissions("operate:friendrebate:addReward")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R addFriendRebateReward(@ModelAttribute AddFriendRebateRewardDto addFriendRebateRewardDto) {
        Assert.isNull(addFriendRebateRewardDto.getLoginName(), "发送不能为空");
        Assert.isNumeric(addFriendRebateRewardDto.getAmount(), "金额只能为数字,并且长度不能大于12位", 12);
        Assert.isMax(BigDecimal.ZERO,addFriendRebateRewardDto.getAmount(), "金额必须大于0");
        Assert.isNull(addFriendRebateRewardDto.getRewardType(), "返利类型不能为空");
        Assert.isNull(addFriendRebateRewardDto.getSubLoginName(),"绑定好友不能为空");
        if(addFriendRebateRewardDto.getRewardType() > 6 || addFriendRebateRewardDto.getRewardType() < 3){
            return R.error("返利类型错误");
        }
        if(addFriendRebateRewardDto.getAudit()!= null && addFriendRebateRewardDto.getAudit()> 0){
            if(addFriendRebateRewardDto.getAuditMultiple() == null || addFriendRebateRewardDto.getAuditMultiple() <= 0){
                return R.error("请填上正确的稽核倍数");
            }
        }
        oprActActivityService.addFriendRebateReward(addFriendRebateRewardDto,  getUser().getUsername());
        return R.ok();
    }


    //@GetMapping("/reduceFriendRebateReward")
    @ApiOperation(value = "手动减少", notes = "好友返利")
    @RequiresPermissions("operate:friendrebate:reduceReward")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R reduceFriendRebateReward(@ModelAttribute ReduceFriendRebateRewardDto reduceFriendRebateRewardDto, HttpServletRequest request) {
        Assert.isNull(reduceFriendRebateRewardDto.getLoginName(), "会员不能为空");
        Assert.isNumeric(reduceFriendRebateRewardDto.getAmount(), "金额只能为数字,并且长度不能大于12位", 12);
        Assert.isMax( BigDecimal.ZERO, reduceFriendRebateRewardDto.getAmount(), "金额必须大于0");
        Assert.isNull(reduceFriendRebateRewardDto.getRewardType(), "返利类型不能为空");
        if(reduceFriendRebateRewardDto.getRewardType() > 6 || reduceFriendRebateRewardDto.getRewardType() < 3){
            return R.error("返利类型错误");
        }
        String ip =   CommonUtil.getIpAddress(request);
        oprActActivityService.reduceFriendRebateReward(reduceFriendRebateRewardDto, getUser().getUsername(), ip);
        return R.ok();
    }
}
