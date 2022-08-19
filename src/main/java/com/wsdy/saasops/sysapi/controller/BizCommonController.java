package com.wsdy.saasops.sysapi.controller;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.QiNiuYunUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.entity.TGameLogo;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import com.wsdy.saasops.modules.operate.service.TGameLogoService;
import com.wsdy.saasops.modules.operate.service.TGmDepotcatService;
import com.wsdy.saasops.modules.operate.service.TGmGameService;
import com.wsdy.saasops.sysapi.dto.SptvBonusDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

@Slf4j
@RestController
@RequestMapping("/sysapi")
@Api(value = "大后台项目", tags = "提供给大后台项目的服务")
public class BizCommonController {

	@Autowired
	private QiNiuYunUtil qiNiuYunUtil;
	@Autowired
	private TCpSiteService tCpSiteService;
	@Autowired
	private TGameLogoService tGameLogoService;
	@Autowired
	private TGmDepotcatService tGmDepotcatService;
	@Autowired
	private TGmGameService tGmGameService;
	@Autowired
	private TGmApiService tGmApiService;

	@RequestMapping("/uploadpic")
	@ApiOperation(value = "上传图片", notes = "上传图片")
	public R depotBalance(@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile) {
		Assert.isNull(uploadFile, "上传文件不能为空");
		byte[] fileBuff = null;
		try {
			fileBuff = IOUtils.toByteArray(uploadFile.getInputStream());
		} catch (IOException e) {
			log.error("depotBalance" + e);
		}
		String uploadFileName = uploadFile.getOriginalFilename();
		String fileName = qiNiuYunUtil.uploadFile(fileBuff, uploadFileName);
		Map<String, String> map = new HashMap<>(2);
		map.put("url", tGmApiService.queryGiniuyunUrl());
		map.put("fileName", fileName);
		return R.ok().put(map);
	}

	@RequestMapping("/disableDepotGameCat")
	@ApiOperation(value = "根据站点、类别、平台关闭游戏", notes = "根据站点、类别、平台关闭游戏")
	public R disableDepotGameCat(@RequestParam("siteCode") String siteCode,
								 @RequestParam("depotId") Integer depotId,
								 @RequestParam("catId") Integer catId,
								 HttpServletRequest request) {
		Assert.isNull(siteCode, "siteCode不能为空");
		Assert.isNull(depotId, "depotId不能为空");
		Assert.isNull(catId, "catId不能为空");

		// 设置站点
		request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
		log.info("disableDepotGameCat-params:"  + "siteCode:" + siteCode + "depotId:" + depotId + "catId:" + catId);
		// 获取数据
		List<TGameLogo> tGmLogos = tGmDepotcatService.catDepotListBiz(catId, ApiConstants.Terminal.pc, depotId, siteCode);
		if(isNull(tGmLogos) || tGmLogos.size() == 0){
			return R.error("该站点该平台该类别已关闭");
		}
		log.info("disableDepotGameCat-tGmLogos:" + JSON.toJSON(tGmLogos.get(0)).toString());
		// 更新数据
		tGameLogoService.updateByBiz(tGmLogos.get(0));
		return R.ok();
	}

	@RequestMapping("/disableDepotGame")
	@ApiOperation(value = "根据站点、平台、游戏Id关闭游戏（捕鱼）", notes = "根据站点、平台、游戏Id关闭游戏（捕鱼）")
	public R disableDepotGame(@RequestParam("siteCode") String siteCode,
							  @RequestParam("depotId") Integer depotId,
							  @RequestParam("gameId") Integer gameId,
							  HttpServletRequest request) {
		Assert.isNull(siteCode, "siteCode不能为空");
		Assert.isNull(depotId, "depotId不能为空");
		Assert.isNull(gameId, "gameId不能为空");

		// 设置站点
		request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(siteCode));
		log.info("disableDepotGame-params:" + "siteCode:" + siteCode + "depotId:" + depotId + "gameId:" + gameId);
		// 获取数据: 固定的捕鱼8
//		List<TGameLogo> tGmLogos = tGmDepotcatService.catDepotListBiz(Constants.EVNumber.eight, ApiConstants.Terminal.pc, depotId, siteCode);
//		if(Objects.isNull(tGmLogos) || tGmLogos.size() == 0){
//			return R.error("该站点下该平台无开启状态的捕鱼类别");
//		}
//		log.info("disableDepotGame-tGmLogos:" + JSON.toJSON(tGmLogos).toString());
		// 获取游戏数据
		TGmGame tGmGame = new TGmGame();
		tGmGame.setDepotIds(String.valueOf(depotId));
		tGmGame.setCatId(Constants.EVNumber.eight);
		tGmGame.setId(gameId);

		List<TGmGame> tGmGames = tGmGameService.findGameListByBiz(tGmGame);
		if(isNull(tGmGames) || tGmGames.size() == 0){
			return R.error("该站点下该平台该捕鱼游戏已关闭");
		}
		log.info("disableDepotGame-tGmGames:" + JSON.toJSON(tGmGames.get(0)).toString());
		// 修改数据
		tGmGameService.updateByBiz(tGmGames.get(0));

		return R.ok();
	}
}
