package com.wsdy.saasops.modules.base.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.entity.TOprNotice;
import com.wsdy.saasops.modules.base.service.TOprNoticeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/base/oprNotice")
public class TOprNoticeController {

    @Autowired
    private TOprNoticeService oprNoticeService;

    @GetMapping("/oprNoticeListPage")
    @ApiOperation(value = "大后台公告", notes = "大后台公告")
    public R oprNoticeListPage(@RequestParam("pageNo") @NotNull Integer pageNo,
                               @RequestParam("pageSize") @NotNull Integer pageSize,
                               @RequestParam("isRead") Boolean isRead) {
        return R.ok().putPage(oprNoticeService.queryListPage(pageNo, pageSize, isRead));
    }

    @GetMapping("/oprNoticeList")
    @ApiOperation(value = "大后台公告所有", notes = "大后台公告所有")
    public R oprNoticeList() {
        return R.ok().putPage(oprNoticeService.oprNoticeList());
    }

    @PostMapping("/oprNoticeRead")
    @ApiOperation(value = "公告设置已读", notes = "公告设置已读")
    public R oprNoticeRead(@RequestBody TOprNotice oprNotice) {
        oprNoticeService.oprNoticeRead(oprNotice.getIds());
        return R.ok();
    }

}
