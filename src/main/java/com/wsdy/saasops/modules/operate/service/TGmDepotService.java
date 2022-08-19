package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApiprefix;
import com.wsdy.saasops.api.modules.transferNew.service.TransferNewService;
import com.wsdy.saasops.api.modules.user.dto.AiRecommondSevenDto;
import com.wsdy.saasops.api.modules.user.dto.ElecGameDto;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.entity.TGmDepotcat;
import com.wsdy.saasops.modules.operate.mapper.OperateMapper;
import com.wsdy.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TGmDepotService extends BaseService<TGmDepotMapper, TGmDepot> {

    @Autowired
    private OperateMapper operateMapper;
    @Autowired
    private SysSettingMapper sysSettingMapper;

    public List<TGmDepot> finfTGmDepotList() {
        TGmDepot tGmDepot = new TGmDepot();
        tGmDepot.setAvailable(Constants.Available.enable);
        return super.queryListCond(tGmDepot);
    }

    public List<TGmDepot> findSiteTGmDepotList(String siteCode) {
        return operateMapper.findSiteTGmDepotList(siteCode);
    }

    public List<TGmDepot> findelecDepotList(Byte terminal, String siteCode) {
        List<TGmDepot> depotList = operateMapper.findelecDepotList(terminal, siteCode);
        if (Collections3.isNotEmpty(depotList)) {
            depotList.stream().forEach(st -> {
                st.setIsTry(Constants.EVNumber.zero);
                ElecGameDto elecGameDto = new ElecGameDto();
                elecGameDto.setDepotId(st.getId());
                elecGameDto.setTerminal(terminal);
                int count = operateMapper.findDepotIstryCount(elecGameDto);
                if (count > 0) {
                    st.setIsTry(Constants.EVNumber.one);
                }
            });
        }
        return depotList;
    }

    public List<TGmDepot> findChessDepotList(Byte terminal, String siteCode) {
        List<TGmDepot> depotList = operateMapper.findChessDepotList(terminal, siteCode);
        if (Collections3.isNotEmpty(depotList)) {
            depotList.stream().forEach(st -> {
                st.setIsTry(Constants.EVNumber.zero);
                ElecGameDto elecGameDto = new ElecGameDto();
                elecGameDto.setDepotId(st.getId());
                elecGameDto.setTerminal(terminal);
                int count = operateMapper.findDepotIstryCount(elecGameDto);
                if (count > 0) {
                    st.setIsTry(Constants.EVNumber.one);
                }
            });
        }
        return depotList;
    }

    public List<TGmDepot> findLotteryDepotList(Byte terminal, String siteCode) {
        List<TGmDepot> depotList = operateMapper.findLotteryDepotList(terminal, siteCode);
        if (Collections3.isNotEmpty(depotList)) {
            depotList.stream().forEach(st -> {
                st.setIsTry(Constants.EVNumber.zero);
                ElecGameDto elecGameDto = new ElecGameDto();
                elecGameDto.setDepotId(st.getId());
                elecGameDto.setTerminal(terminal);
                int count = operateMapper.findDepotIstryCount(elecGameDto);
                if (count > 0) {
                    st.setIsTry(Constants.EVNumber.one);
                }
            });
        }
        return depotList;
    }

    public List<TGmCat> findelecCatList(Byte terminal, String siteCode) {
        return operateMapper.findelecCatList(terminal, siteCode);
    }

    public List<TGmDepot> catchFishDepoList(Byte terminal, String siteCode) {
        return operateMapper.catchFishDepoList(terminal, siteCode);
    }

    public List<TGmDepotcat> findDepotCatList() {
        return operateMapper.findDepotcatAll();
    }

    public List<TGmDepotcat> findCatDepot(Integer catId) {
        return operateMapper.findCatDepot(catId);
    }

    public List<TGmDepot> findDepotBalanceList(Integer accountId) {
        return operateMapper.findDepotBalanceList(accountId);
    }

    /**
     * 查询会员的平台列表
     * @param accountId     会员id
     * @param terminal      0:pc端 1:mobile端
     * @param siteCode      站点code
     * @param flag          0 所有平台 1 转账过的平台
     * @return
     */
    public List<TGmDepot> findDepotList(Integer accountId, Byte terminal, String siteCode, Integer flag) {
        List<TGmDepot> list = new ArrayList<>();
        if (flag.equals(Constants.EVNumber.zero)) {       // 所有平台
            list = operateMapper.findDepotList(accountId, terminal, siteCode);
        } else if (flag.equals(Constants.EVNumber.one)) {  // 转账过的平台
            list = operateMapper.findDepotList(accountId, terminal, siteCode);
            if (Objects.isNull(list)) {
                return list;
            }
            // 过滤未曾转账
            list = list.stream().filter(ls
                    -> String.valueOf(Constants.EVNumber.one).equals(String.valueOf(ls.getHasTransfer()))).collect(Collectors.toList());
        }
        //平台分类名通过/拼接给模板前端使用
        list.forEach(e -> {
            if (StringUtil.isNotEmpty(e.getCatNames())) {
                e.setCatNames(e.getCatNames().replace(",", "/"));
            }
            if(TransferNewService.noTransferDepotCodes.contains(e.getDepotCode())){
                e.setIsSingleOne(Constants.EVNumber.one);
            }else{
                e.setIsSingleOne(Constants.EVNumber.zero);
            }
        });
        return list;
    }

    public List<TGmDepot> findCatListBySiteCode(String siteCode) {
        return operateMapper.findCatOrDepotListBySiteCode(Boolean.TRUE, siteCode);
    }

    public List<TGmApiprefix> getTGmApiprefix(Integer depotId,String siteCode){
        return operateMapper.getTGmApiprefix(depotId,siteCode);
    }

    public List<AiRecommondSevenDto> getAiRecommendSeven(Integer userId){
        SysSetting ss = sysSettingMapper.selectByPrimaryKey(SystemConstants.AI_RECOMMEND);
        List<AiRecommondSevenDto> list= new ArrayList<>();
        if (ss.getSysvalue().equals("0")){
            String time = DateUtil.getPastDate(7,"yyyy-MM-dd");
            list=  operateMapper.getAiRecommendSeven(userId,time);
        }
        return list;
    }

}
