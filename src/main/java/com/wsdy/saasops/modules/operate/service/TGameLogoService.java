package com.wsdy.saasops.modules.operate.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.operate.dao.TGameLogoMapper;
import com.wsdy.saasops.modules.operate.entity.SetGmGame;
import com.wsdy.saasops.modules.operate.entity.TGameLogo;
import com.wsdy.saasops.modules.operate.mapper.GameMapper;

@Service
public class TGameLogoService extends BaseService<TGameLogoMapper, TGameLogo> {

    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;

    @Transactional
    public int update(TGameLogo tGameLogo, String userName, String ip) {
        SetGmGame setGmGame = new SetGmGame();
        setGmGame.setDepotId(tGameLogo.getDepotId());
        setGmGame.setMemo(tGameLogo.getMemoDepot());
        setGmGame.setGameLogoId(tGameLogo.getId());
        setGmGame.setDepotName(tGameLogo.getDepotName());
        setGmGame.setSortId(tGameLogo.getSortIdDepot());
        setGmGame.setEnableDepotPc(tGameLogo.getEnableDepotPc());
        setGmGame.setEnableDepotMb(tGameLogo.getEnableDepotMb());
        setGmGame.setEnableDepotApp(tGameLogo.getEnableDepotApp());
        setGmGame.setRate(tGameLogo.getRate());     // 平台费率
        setGmGame.setWaterrate(tGameLogo.getWaterrate());

        int count = gameMapper.saveOrUpdataSetGmGame(setGmGame);
        if (count > 0) {
        	//添加操作日志
        	mbrAccountLogService.addDepotPCAvailable(tGameLogo, userName, ip);
        }
        
        return count;
    }


    public int updateByBiz(TGameLogo tGameLogo) {
        SetGmGame setGmGame = new SetGmGame();
        setGmGame.setDepotId(tGameLogo.getDepotId());
        setGmGame.setMemo(tGameLogo.getMemoDepot());
        setGmGame.setGameLogoId(tGameLogo.getId());
        setGmGame.setRate(BigDecimal.ZERO);         // 平台费率
        setGmGame.setSortId(tGameLogo.getSortIdDepot());
        setGmGame.setEnableDepotPc(Constants.EVNumber.zero);
        setGmGame.setEnableDepotMb(Constants.EVNumber.zero);
        setGmGame.setEnableDepotApp(Constants.EVNumber.zero);

        return gameMapper.saveOrUpdataSetGmGame(setGmGame);
    }
}
