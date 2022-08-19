package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.operate.dao.TGmDepotcatMapper;
import com.wsdy.saasops.modules.operate.entity.TGameLogo;
import com.wsdy.saasops.modules.operate.entity.TGmDepotcat;
import com.wsdy.saasops.modules.operate.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class TGmDepotcatService extends BaseService<TGmDepotcatMapper, TGmDepotcat> {

    @Autowired
    private GameMapper gameMapper;

    public List<TGmDepotcat> catDepotList(Integer catId,Byte terminal) {
        TGameLogo gameLogo = new TGameLogo();
        gameLogo.setCatId(catId);
        gameLogo.setTerminal(terminal);
        gameLogo.setSiteCode(CommonUtil.getSiteCode());
        List<TGameLogo> list = gameMapper.listtGameLogo(gameLogo);
        if (Collections3.isNotEmpty(list)) {
            return list.stream().map(ls -> {
                TGmDepotcat depotcat = new TGmDepotcat();
                depotcat.setDepotName(ls.getDepotName());
                depotcat.setDepotId(ls.getDepotId());
                depotcat.setCatId(ls.getCatId());
                depotcat.setCatName(ls.getCatName());
                return depotcat;
            }).collect(Collectors.toList());
        }
        return null;
    }

    public List<TGameLogo> catDepotListBiz(Integer catId, Byte terminal, Integer depotId, String siteCode) {
        TGameLogo gameLogo = new TGameLogo();
        gameLogo.setCatId(catId);
        gameLogo.setTerminal(terminal);
        gameLogo.setSiteCode(siteCode);
        gameLogo.setDepotId(depotId);
        List<TGameLogo> list = gameMapper.listtGameLogos(gameLogo);
        return list;
    }
}
