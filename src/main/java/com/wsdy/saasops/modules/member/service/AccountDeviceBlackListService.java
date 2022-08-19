package com.wsdy.saasops.modules.member.service;

import com.google.common.collect.Lists;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccountDevice;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
public class AccountDeviceBlackListService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrAccountDeviceService mbrAccountDeviceService;
    @Autowired
    private MbrGroupMapper mbrGroupMapper;


    public void accountBlackList(String siteCode,String key){
        log.info("accountBlackList==" +siteCode + "==start" );
        try{
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            // 获取黑名单会员组
            MbrGroup group = new MbrGroup();
            group.setIsBlackGroup(Integer.valueOf(Constants.EVNumber.one).byteValue());
            List<MbrGroup> groupList = mbrGroupMapper.select(group);
            if(Collections3.isEmpty(groupList)){
                log.info("accountBlackList==" +siteCode + "==无黑名单会员组设置" );
                return;
            }
            Integer groupId = groupList.get(0).getId();

            // 查询同设备大于4的会员数据
            int num = 4;
            List<MbrAccountDevice> list = mbrAccountDeviceService.getSameDeviceMbrList(num);
            if(Collections3.isEmpty(list)){
                log.info("accountBlackList==" +siteCode + "==查无数据" );
                return;
            }

            log.info("accountBlackList==" +siteCode + "==list.size==" + list.size() );

            // 对数据按uuid分组,sql中已经按绑定时间正序排序，获得有序分组
            LinkedHashMap<String,List<MbrAccountDevice>> groupby = list.stream().collect(Collectors.groupingBy(MbrAccountDevice::getDeviceUuid, LinkedHashMap::new,Collectors.toList()));
            log.info("accountBlackList==" +siteCode + "==groupby.size==" + groupby.size() );

            // 黑名单会员会员组更新
            List<Integer> blackList = new ArrayList<>();
            // 会员设备表更新总数据
            List<Integer> setList = new ArrayList<>();
            // 分组获取需要拉入黑名单的数据
            for(String deviceUuid : groupby.keySet()){
                List<MbrAccountDevice> deviceList = groupby.get(deviceUuid);
                // 获取从第4个之后的数据
                for(int i = num; i< deviceList.size(); i++){
                    MbrAccountDevice device = deviceList.get(i);
                    // 加入会员设备表更新list
                    setList.add(device.getId());
                    // 加入黑名单会员更新list
//                    MbrAccount black = new MbrAccount();
//                    black.setId(device.getAccountId());
//                    black.setGroupId(group.getId());
                    blackList.add(device.getAccountId());
                }
            }

            // 批量更新会员组
            Long startTime = System.currentTimeMillis();
            log.info("accountBlackList==" +siteCode + "==blackList.size==" + blackList.size() + "==startTime==" + getCurrentDate(FORMAT_18_DATE_TIME));
            List<List<Integer>> partBlackList = Lists.partition(blackList,50);
            partBlackList.stream().forEach(
                    tmp -> {
                        mbrAccountDeviceService.batchUpdateMbrGroup(tmp,groupId);
                    }
            );
            long costTime = System.currentTimeMillis() - startTime;
            log.info("accountBlackList==" +siteCode + "==blackList.size==" + blackList.size() + "==endTime==" + getCurrentDate(FORMAT_18_DATE_TIME) + "==costTime==" + costTime);

            // 批量更新会员黑名单
            startTime = System.currentTimeMillis();
            log.info("accountBlackList==" +siteCode + "==setList.size==" + setList.size() + "==startTime==" + getCurrentDate(FORMAT_18_DATE_TIME));
            List<List<Integer>> partsetList = Lists.partition(setList,50);
            partsetList.stream().forEach(
                    tmp -> {
                        mbrAccountDeviceService.batchUpdateMbrDevice(tmp);
                    }
            );
            costTime = System.currentTimeMillis() - startTime;
            log.info("accountBlackList==" +siteCode + "==setList.size==" + setList.size() + "==endTime==" + getCurrentDate(FORMAT_18_DATE_TIME) + "==costTime==" + costTime);

        }catch (Exception e){
            log.info("accountBlackList==" +siteCode + "==error==" + e );
        }finally {
            redisService.del(key);
        }
        log.info("accountBlackList==" +siteCode + "==end" );
    }


}
