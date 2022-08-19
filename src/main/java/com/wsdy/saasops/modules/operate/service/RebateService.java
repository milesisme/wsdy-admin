
package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.operate.entity.RebateCat;
import com.wsdy.saasops.modules.operate.entity.RebateInfo;
import com.wsdy.saasops.modules.operate.mapper.RebateMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@Transactional
public class RebateService {

    @Autowired
    private RebateMapper rebateMapper;

    public PageUtils queryListAll(RebateInfo rebateInfo, Integer pageNo, Integer pageSize) {
        log.info("好友返利查询开始"+System.currentTimeMillis());
        PageHelper.startPage(pageNo, pageSize);
        List<RebateInfo> resultList = rebateMapper.queryListAll(rebateInfo);
        log.info("好友返利查询结束"+System.currentTimeMillis());
        if(resultList.size() != 0) {
            //获取小计
            resultList.add(getListSubtotal(resultList));
            log.info("好友返利小计结束"+System.currentTimeMillis());
            //获取总计
            resultList.add(getListTotal(rebateInfo));
            log.info("好友返利合计结束"+System.currentTimeMillis());
        }
        PageUtils pageUtils = BeanUtil.toPagedResult(resultList);
        return pageUtils;
    }

    /**
     * 返回首页小计
     */
    private RebateInfo getListSubtotal(List<RebateInfo> list) {
        RebateInfo subTotal = new RebateInfo();
        BigDecimal rebateAmount = BigDecimal.ZERO;
        for (RebateInfo model : list) {
            if (model.getRebateAmount() != null) {
                rebateAmount = rebateAmount.add(model.getRebateAmount());
            }
        }
        subTotal.setFinancialCode("小计");
        subTotal.setRebateAmount(rebateAmount);
        return subTotal;
    }

    //首页总计
    private RebateInfo getListTotal(RebateInfo model){
        RebateInfo allTotal = new RebateInfo();
        BigDecimal totalAmount = rebateMapper.findTotalAmount(model);
        BigDecimal rebateAmount = BigDecimal.ZERO;
        if (totalAmount != null) {
            rebateAmount = rebateAmount.add(totalAmount);
        }
        allTotal.setFinancialCode("总计");
        allTotal.setRebateAmount(rebateAmount);
        return allTotal;
    }

    public PageUtils refferListEgSanGong(RebateInfo rebateInfo, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<RebateInfo> rebateList = rebateMapper.refferListEgSanGong(rebateInfo);
        if(rebateList.size() != 0) {
            //获取小计
            rebateList.add(getRefferListSubtotalEgSanGong(rebateList));
            //获取总计
            rebateList.add(getRefferListTotalEgSanGong(rebateInfo));
        }
        PageUtils pageUtils = BeanUtil.toPagedResult(rebateList);
        return pageUtils;
    }


    public PageUtils refferList(RebateInfo rebateInfo, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<RebateInfo> rebateList = rebateMapper.refferList(rebateInfo);
        for (RebateInfo rebate:rebateList) {
            List<RebateCat> rebateCatList = new ArrayList<>();
            String catIds = rebate.getCatIds();
            String validbets = (null==rebate.getValidbets()?BigDecimal.ZERO.toString():rebate.getValidbets());
            if(-1 == catIds.indexOf(",")){
                RebateCat rebateCat = new RebateCat();
                rebateCat.setCatId(Integer.parseInt(catIds));
                rebateCat.setValidBet(new BigDecimal(validbets));
                rebateCat.setAccountId(rebate.getAccountId());
                rebateCatList.add(rebateCat);
            }else {
                String [] catArr = catIds.split(",");
                String [] validbetsArr = validbets.split(",");
                for (int i=0;i<catArr.length;i++) {
                    RebateCat rebateCat = new RebateCat();
                    rebateCat.setCatId(Integer.parseInt(catArr[i]));
                    try {
                        rebateCat.setValidBet(new BigDecimal(validbetsArr[i]));
                    } catch (ArrayIndexOutOfBoundsException e){
                        rebateCat.setValidBet(BigDecimal.ZERO);
                    }
                    rebateCat.setAccountId(rebate.getAccountId());
                    rebateCatList.add(rebateCat);
                }
            }
            rebate.setRebateCatList(rebateCatList);
        }

        if(rebateList.size() != 0) {
            //获取小计
            rebateList.add(getRefferListSubtotal(rebateList, rebateInfo));
            //获取总计
            rebateList.add(getRefferListTotal(rebateInfo));
        }
        PageUtils pageUtils = BeanUtil.toPagedResult(rebateList);
        return pageUtils;
    }


    /**
     * 好友详情小计
     */
    private RebateInfo getRefferListSubtotal(List<RebateInfo> list, RebateInfo info) {
        RebateInfo subTotal = new RebateInfo();
        BigDecimal totalResult = BigDecimal.ZERO;
        BigDecimal contributeAmount = BigDecimal.ZERO;

        HashMap<String, BigDecimal> subTotalMap = new HashMap(16);
        for (RebateInfo model : list) {
            if (model.getTotalResult() != null) {
                totalResult = totalResult.add(model.getTotalResult());
            }
            if(model.getContributeAmount() != null){
                contributeAmount = contributeAmount.add(model.getContributeAmount());
            }

            List<RebateCat> catList = model.getRebateCatList();
            for (RebateCat rebateCat:catList) {
                if(null == subTotalMap.get(rebateCat.getCatId().toString())){
                    subTotalMap.put(rebateCat.getCatId().toString(), rebateCat.getValidBet());
                } else {
                    BigDecimal totalBet = subTotalMap.get(rebateCat.getCatId().toString()).add(rebateCat.getValidBet());
                    subTotalMap.put(rebateCat.getCatId().toString(), totalBet);
                }
            }
        }
        List<RebateCat> subCatList = new ArrayList<>();
        Iterator iter = subTotalMap.entrySet().iterator();
        while (iter.hasNext()) {
            RebateCat rebateCat = new RebateCat();
            Map.Entry entry = (Map.Entry) iter.next();
            rebateCat.setCatId(Integer.parseInt((String) entry.getKey()));
            rebateCat.setValidBet((BigDecimal) entry.getValue());
            subCatList.add(rebateCat);
        }
        //统计投注列表
        //List<RebateCat> subCatList = rebateMapper.findCatValidbetList(info);

        subTotal.setReferrer("小计");
        subTotal.setTotalResult(totalResult);
        subTotal.setContributeAmount(contributeAmount);
        subTotal.setRebateCatList(subCatList);
        return subTotal;
    }


    //好友详情总计
    private RebateInfo getRefferListTotal(RebateInfo model){
        List<RebateInfo> rebateList = rebateMapper.refferList(model);

        RebateInfo allTotal = new RebateInfo();
        BigDecimal totalResult = BigDecimal.ZERO;
        BigDecimal contributeAmount = BigDecimal.ZERO;

        for (RebateInfo rebateInfo:rebateList) {
            if(null != rebateInfo.getTotalResult()){
                totalResult = totalResult.add(rebateInfo.getTotalResult());
            }
            if(null != rebateInfo.getContributeAmount()){
                contributeAmount = contributeAmount.add(rebateInfo.getContributeAmount());
            }
        }
        List<RebateCat> catValidbetList = rebateMapper.findCatValidbetList(model);

        allTotal.setReferrer("总计");
        allTotal.setTotalResult(totalResult);
        allTotal.setContributeAmount(contributeAmount);
        allTotal.setRebateCatList(catValidbetList);
        return allTotal;
    }


    /**
     * 好友详情小计
     */
    private RebateInfo getRefferListSubtotalEgSanGong(List<RebateInfo> list) {
        RebateInfo subTotal = new RebateInfo();
        BigDecimal totalResult = BigDecimal.ZERO;
        BigDecimal contributeAmount = BigDecimal.ZERO;
        List<RebateCat> subCatList = new ArrayList<>();

        for (RebateInfo model : list) {
            if(model.getContributeAmount() != null){
                contributeAmount = contributeAmount.add(model.getContributeAmount());
            }
            RebateCat rebateCat = new RebateCat();
            if(model.getCatId() != null){
                rebateCat.setCatId(model.getCatId());
            }
            if(model.getValidbet() != null){
                rebateCat.setValidBet(model.getValidbet());
            }
            subCatList.add(rebateCat);
        }

        subTotal.setReferrer("小计");
        subTotal.setTotalResult(totalResult);
        subTotal.setContributeAmount(contributeAmount);
        subTotal.setRebateCatList(subCatList);
        return subTotal;
    }

    //好友详情总计
    private RebateInfo getRefferListTotalEgSanGong(RebateInfo rebateInfo){
        List<RebateInfo> rebateList = rebateMapper.refferListEgSanGong(rebateInfo);

        RebateInfo allTotal = new RebateInfo();
        BigDecimal totalResult = BigDecimal.ZERO;
        BigDecimal contributeAmount = BigDecimal.ZERO;
        List<RebateCat> catValidbetList = new ArrayList<>();

        for (RebateInfo model : rebateList) {
            if(model.getContributeAmount() != null){
                contributeAmount = contributeAmount.add(model.getContributeAmount());
            }
            RebateCat rebateCat = new RebateCat();
            if(model.getCatId() != null){
                rebateCat.setCatId(model.getCatId());
            }
            if(model.getValidbet() != null){
                rebateCat.setValidBet(model.getValidbet());
            }
            catValidbetList.add(rebateCat);
        }

        allTotal.setReferrer("总计");
        allTotal.setTotalResult(totalResult);
        allTotal.setContributeAmount(contributeAmount);
        allTotal.setRebateCatList(catValidbetList);
        return allTotal;
    }
}
