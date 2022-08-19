package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrMessage;
import com.wsdy.saasops.modules.member.entity.MbrMessageInfo;
import com.wsdy.saasops.modules.member.service.MbrMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/bkapi/member/message")
@Api(value = "message", tags = "会员留言板")
@Slf4j
public class MbrMessageController extends AbstractController {

    @Autowired
    private MbrMessageService mbrMessageService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private JsonUtil jsonUtil;

    @GetMapping("messageList")
    @RequiresPermissions("member:message:list")
    @ApiOperation(value = "会员留言列表", notes = "会员留言列表")
    public R messageList(@ModelAttribute MbrMessage mbrMessage,
                         @RequestParam("pageNo") @NotNull Integer pageNo,
                         @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(mbrMessageService.messageList(mbrMessage, pageNo, pageSize, getUserId()));
    }


    @GetMapping("messageInfo")
    @RequiresPermissions("member:message:list")
    @ApiOperation(value = "会话留言查询单个", notes = "会话留言查询单个")
    public R messageInfo(@ModelAttribute MbrMessageInfo mbrMessageInfo) {
        Assert.isNull(mbrMessageInfo.getMessageId(), "消息ID不能为空");
        return R.ok().put(mbrMessageService.messageInfo(mbrMessageInfo));
    }

    @PostMapping("messageSend")
    @RequiresPermissions(value = {"member:message:send", "member:message:newSend", "member:mbraccount:sendMsg"}, logical = Logical.OR)
    @ApiOperation(value = "会话留言回复", notes = "会话留言回复")
    public R messageSend(@ModelAttribute MbrMessageInfo mbrMessageInfo,
                         @RequestParam(value = "uploadMessageFile", required = false) MultipartFile uploadMessageFile) {
        // 检查消息内容
        checkoutMessage(mbrMessageInfo);
        mbrMessageInfo.setCreateUser(getUser().getUsername());
        // 发送消息
        // 通过对话框不做异步
        if (Objects.nonNull(mbrMessageInfo.getMessageId())) {
            return mbrMessageService.messageSend(mbrMessageInfo, uploadMessageFile);
        }
        // 通过新建对话发送
        String key = RedisConstants.ACCOUNT_MESSAGE_KEY_BATCH + CommonUtil.getSiteCode() + mbrMessageInfo.getCreateUser();
//        key += "-" + mbrMessageInfo.getLoginNameList() + "-" + mbrMessageInfo.getAgyList() + "-" + mbrMessageInfo.getGroupList();
        log.info("messageSend==param==" + jsonUtil.toJson(mbrMessageInfo));
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, key, 30, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            // 异步发送消息
            R ret = mbrMessageService.messageSendBatch(mbrMessageInfo, uploadMessageFile, key);
            return ret;
        } else {
            return R.error(500, "后台正在发送消息，请耐心等待！");
        }
    }

    @PostMapping("messageUpdate")
    @RequiresPermissions("member:message:update")
    @ApiOperation(value = "会话留言编辑", notes = "会话留言编辑")
    public R messageUpdate(@ModelAttribute MbrMessageInfo mbrMessageInfo) {
        checkoutMessage(mbrMessageInfo);
        Assert.isNull(mbrMessageInfo.getId(), "ID不能为空");
        mbrMessageInfo.setCreateUser(getUser().getUsername());
        mbrMessageService.messageUpdate(mbrMessageInfo);
        return R.ok();
    }

    private void checkoutMessage(MbrMessageInfo mbrMessageInfo) {
        if (Objects.nonNull(mbrMessageInfo) && Objects.nonNull((mbrMessageInfo.getTextContent()))) {
            Assert.isLenght(mbrMessageInfo.getTextContent(), "发送内容最大长度为1000", 0, 1000);
            // 屏蔽emoj表情
            if (StringUtil.isHasEmoji(mbrMessageInfo.getTextContent())) {
                throw new R200Exception("不支持发送表情！");
            }
        }
        //Assert.isNull(mbrMessageInfo.getMessageId(), "消息ID不能为空");
    }

    @GetMapping("/messageCountByIsRevert")
    @RequiresPermissions("member:message:list")
    @ApiOperation(value = "会员留言统计", notes = "根据状态统会员留言")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R messageCountByIsRevert(@ModelAttribute MbrMessage mbrMessage) {

        return R.ok(mbrMessageService.messageCountByIsRevert(mbrMessage));
    }

}
