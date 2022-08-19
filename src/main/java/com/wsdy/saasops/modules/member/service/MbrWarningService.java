package com.wsdy.saasops.modules.member.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.member.dto.*;
import com.wsdy.saasops.modules.member.entity.MbrWarning;
import com.wsdy.saasops.modules.member.entity.MbrWarningCondition;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.mapper.MbrWarningConditionMapper;
import com.wsdy.saasops.modules.member.mapper.MbrWarningMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;

@Service
public class MbrWarningService {

    @Autowired
    private MbrWarningMapper mbrWarningMapper;
    @Autowired
    private MbrWarningConditionMapper mbrWarningConditionMapper;
    @Autowired
    private MbrMapper mbrMapper;

    public PageUtils pageList(MbrWarningQueryDto mbrWarningQueryDto){
        PageHelper.startPage(mbrWarningQueryDto.getPageNo(), mbrWarningQueryDto.getPageSize());
        List<MbrWarningDto> list = mbrWarningMapper.list(mbrWarningQueryDto);
        return BeanUtil.toPagedResult(list);
    }


    public int dealWith(MbrWarningDealWithDto mbrWarningDealWithDto, String dealUser){
        MbrWarning w = new MbrWarning();
        w.setId(mbrWarningDealWithDto.getId());
        MbrWarning mbrWarning = mbrWarningMapper.selectOne(w);

        if(Objects.isNull(mbrWarning)){
            throw new R200Exception("该预警信息不存在!");
        }

        if(mbrWarning.getStatus() == Constants.EVNumber.one){
            throw new R200Exception("带信息已处理，无法重复处理!");
        }
        mbrWarning.setDealTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrWarning.setDealUser(dealUser);
        mbrWarning.setStatus(Constants.EVNumber.one);
        mbrWarning.setMemo(mbrWarningDealWithDto.getMemo());
        return mbrWarningMapper.updateByPrimaryKey(mbrWarning);
    }


    public List<MbrWarningConditionDto> conditionList(){
        List<MbrWarningConditionDto> list = mbrWarningConditionMapper.list();
        for (MbrWarningConditionDto mbrWarningConditionDto : list){
           String content =  String.format(mbrWarningConditionDto.getTemplate(),mbrWarningConditionDto.getParam1(), mbrWarningConditionDto.getParam2(), mbrWarningConditionDto.getParam3(), mbrWarningConditionDto.getParam4() );
            mbrWarningConditionDto.setContent(content);
        }
        return list;
    }


    public int switchCondition(SwitchConditionDto switchConditionDto, String user){
        MbrWarningCondition w = new MbrWarningCondition();
        w.setId(switchConditionDto.getId());
        MbrWarningCondition mbrWarningCondition = mbrWarningConditionMapper.selectOne(w);
        mbrWarningCondition.setUpdateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrWarningCondition.setStatus(switchConditionDto.getStatus());
        mbrWarningCondition.setUpdateUser(user);
        return mbrWarningConditionMapper.updateByPrimaryKey(mbrWarningCondition);
    }

    public String mbrWarning(String siteCode, String clacDay){
        Calendar cal = Calendar.getInstance();
        if(StringUtil.isNotEmpty(clacDay)){
            String[] date = clacDay.split("-");
            cal.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
        }

        cal.add(Calendar.DATE, -1);
        String warningDay = DateUtil.format(cal.getTime(), FORMAT_10_DATE);


        // 所有条件
        List<MbrWarningConditionDto> list = conditionList();
        for (MbrWarningConditionDto mbrWarningConditionDto: list) {
            if(mbrWarningConditionDto.getStatus()== Constants.EVNumber.one){
                switch (mbrWarningConditionDto.getType()){
                    //持续盈利
                    case Constants.EVNumber.one : {
                        List<String> usernames = mbrMapper.selectWarningMbrAccount(warningDay);
                        keepWin(warningDay, usernames, mbrWarningConditionDto);
                    }
                        break;
                        //盈利比例比较高
                    case Constants.EVNumber.two: {
                        List<String> usernames = mbrMapper.selectWarningMbrAccountWithIn(warningDay, mbrWarningConditionDto.getParam1());
                        highWin(warningDay, usernames, mbrWarningConditionDto);
                    }
                        break;

                        // 盈利金额比较大
                    case Constants.EVNumber.three:{
                        List<String> usernames = mbrMapper.selectWarningMbrAccountWithIn(warningDay, mbrWarningConditionDto.getParam1());
                        bigWin(warningDay, usernames, mbrWarningConditionDto);
                    }
                        break;
                        //优惠
                    case Constants.EVNumber.four:{
                        List<Integer> accountIds = mbrMapper.selectWarningDepositMbrAccount(warningDay, mbrWarningConditionDto.getParam1());
                        highDiscount(warningDay, accountIds, mbrWarningConditionDto);
                    }
                        break;
                        // ip
                    case Constants.EVNumber.five:{
                        warningIp(warningDay, mbrWarningConditionDto);
                    }
                        break;
                    // 设备
                    case Constants.EVNumber.six:
                    {
                        warningDevice(warningDay, mbrWarningConditionDto);
                    }
                        break;

                }
            }
        }
        return "OK";
    }


    public Integer getMbrWarningCount(String loginName){
        return mbrWarningMapper.getMbrWarningCount(loginName);
    }

    private void keepWin(String warningDay, List<String> usernames, MbrWarningConditionDto mbrWarningConditionDto) {
        if(usernames.size() > 0){
            // 统计
            Map<String, Integer>  warningBetInfoMap = new HashMap<>();
            List<MbrWarningBetInfoDto>  warningBetInfoDtoList = mbrMapper.selectWarningMbrBetInfo(warningDay, mbrWarningConditionDto.getParam1(), usernames);
            for (MbrWarningBetInfoDto mbrWarningBetInfoDto :warningBetInfoDtoList) {
                if(mbrWarningBetInfoDto.getPayout().compareTo(new BigDecimal(mbrWarningConditionDto.getParam2())) >= 0){
                   Integer count =  warningBetInfoMap.get(mbrWarningBetInfoDto.getUserName());
                   if(count == null){
                       count = 0 ;
                   }
                    count ++ ;
                   warningBetInfoMap.put(mbrWarningBetInfoDto.getUserName(), count);
                }
            }

            // 写入预警
            List<MbrWarning> mbrWarnings = new ArrayList<>();
            for (Map.Entry<String, Integer> entry: warningBetInfoMap.entrySet()) {
               String username =  entry.getKey();
               Integer  count = entry.getValue();
                if(count ==  mbrWarningConditionDto.getParam1()){
                    MbrWarning mbrWarning  = new MbrWarning();
                    mbrWarning.setContent(mbrWarningConditionDto.getContent());
                    mbrWarning.setStatus(Constants.EVNumber.zero);
                    mbrWarning.setWarningDate(warningDay);
                    mbrWarning.setType(mbrWarningConditionDto.getType());
                    mbrWarning.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                    mbrWarning.setLoginName(username);
                    mbrWarnings.add(mbrWarning);
                }
            }
            if(mbrWarnings.size() > 0){
                mbrWarningMapper.insertList(mbrWarnings);
            }
        }
    }


    private void highWin(String warningDay, List<String> usernames, MbrWarningConditionDto mbrWarningConditionDto) {
        if(usernames.size() > 0){
            // 统计
            Map<String, HighWinDto>  warningBetInfoMap = new HashMap<>();
            List<MbrWarningBetInfoDto>  warningBetInfoDtoList = mbrMapper.selectWarningMbrBetInfo(warningDay, mbrWarningConditionDto.getParam1(), usernames);
            for (MbrWarningBetInfoDto mbrWarningBetInfoDto :warningBetInfoDtoList) {
                if(mbrWarningBetInfoDto.getBet().compareTo(BigDecimal.ZERO) == 1){
                    HighWinDto highWinDto =  warningBetInfoMap.get(mbrWarningBetInfoDto.getUserName());
                    if(highWinDto == null){
                        highWinDto = new HighWinDto();
                        warningBetInfoMap.put(mbrWarningBetInfoDto.getUserName(), highWinDto);
                    }
                    highWinDto.setBet(highWinDto.getBet().add(mbrWarningBetInfoDto.getBet()));
                    highWinDto.setPayout(highWinDto.getPayout().add(mbrWarningBetInfoDto.getPayout()));
                }
            }

            // 写入预警
            List<MbrWarning> mbrWarnings = new ArrayList<>();
            for (Map.Entry<String, HighWinDto> entry: warningBetInfoMap.entrySet()) {
                String username =  entry.getKey();
                HighWinDto  highWinDto = entry.getValue();
                if(highWinDto.getBet().compareTo(BigDecimal.ZERO) ==0){
                    continue;
                }
                BigDecimal rt = highWinDto.getPayout().divide(highWinDto.getBet(), 2, BigDecimal.ROUND_DOWN);

                rt = rt.multiply(new BigDecimal(100)).subtract(new BigDecimal(mbrWarningConditionDto.getParam3()));
                if(highWinDto.getBet().compareTo(new BigDecimal(mbrWarningConditionDto.getParam2())) >= 0  && rt.compareTo(BigDecimal.ZERO) >= 0){
                    MbrWarning mbrWarning  = new MbrWarning();
                    mbrWarning.setContent(mbrWarningConditionDto.getContent());
                    mbrWarning.setStatus(Constants.EVNumber.zero);
                    mbrWarning.setWarningDate(warningDay);
                    mbrWarning.setType(mbrWarningConditionDto.getType());
                    mbrWarning.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                    mbrWarning.setLoginName(username);
                    mbrWarnings.add(mbrWarning);
                }
            }
            if(mbrWarnings.size() > 0){
                mbrWarningMapper.insertList(mbrWarnings);
            }
        }
    }

    private void bigWin(String warningDay, List<String> usernames, MbrWarningConditionDto mbrWarningConditionDto) {
        if(usernames.size() > 0){
            // 统计
            Map<String, BigWinDto>  warningBetInfoMap = new HashMap<>();
            List<MbrWarningBetInfoDto>  warningBetInfoDtoList1 = mbrMapper.selectWarningBigBetInfo(warningDay, mbrWarningConditionDto.getParam1(), usernames);
            List<MbrWarningBetInfoDto>  warningBetInfoDtoList2 = mbrMapper.selectWarningBigBetInfo(warningDay, mbrWarningConditionDto.getParam3(), usernames);

            for (MbrWarningBetInfoDto mbrWarningBetInfoDto :warningBetInfoDtoList1) {
                if(mbrWarningBetInfoDto.getPayout().compareTo(new BigDecimal(mbrWarningConditionDto.getParam2())) == 1){
                    BigWinDto bigWinDto =  warningBetInfoMap.get(mbrWarningBetInfoDto.getUserName());
                    if(bigWinDto == null){
                        bigWinDto = new BigWinDto();
                        warningBetInfoMap.put(mbrWarningBetInfoDto.getUserName(), bigWinDto);
                    }
                    bigWinDto.setPayout1(bigWinDto.getPayout1().add(mbrWarningBetInfoDto.getPayout()));
                }
            }

            for (MbrWarningBetInfoDto mbrWarningBetInfoDto :warningBetInfoDtoList2) {
                if(mbrWarningBetInfoDto.getPayout().compareTo(new BigDecimal(mbrWarningConditionDto.getParam2())) == 1){
                    BigWinDto bigWinDto =  warningBetInfoMap.get(mbrWarningBetInfoDto.getUserName());
                    if(bigWinDto == null){
                        bigWinDto = new BigWinDto();
                        warningBetInfoMap.put(mbrWarningBetInfoDto.getUserName(), bigWinDto);
                    }
                    bigWinDto.setPayout2(bigWinDto.getPayout2().add(mbrWarningBetInfoDto.getPayout()));
                }
            }
            // 写入预警
            List<MbrWarning> mbrWarnings = new ArrayList<>();
            for (Map.Entry<String, BigWinDto> entry: warningBetInfoMap.entrySet()) {
                String username =  entry.getKey();
                BigWinDto  bigWinDto = entry.getValue();

                if(bigWinDto.getPayout1().compareTo(new BigDecimal(mbrWarningConditionDto.getParam2())) >=0  || bigWinDto.getPayout2().compareTo(new BigDecimal(mbrWarningConditionDto.getParam4())) >=0){
                    MbrWarning mbrWarning  = new MbrWarning();
                    mbrWarning.setContent(mbrWarningConditionDto.getContent());
                    mbrWarning.setStatus(Constants.EVNumber.zero);
                    mbrWarning.setWarningDate(warningDay);
                    mbrWarning.setType(mbrWarningConditionDto.getType());
                    mbrWarning.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                    mbrWarning.setLoginName(username);
                    mbrWarnings.add(mbrWarning);
                }
            }
            if(mbrWarnings.size() > 0){
                mbrWarningMapper.insertList(mbrWarnings);
            }
        }
    }


    private void highDiscount(String warningDay, List<Integer> accountIds, MbrWarningConditionDto mbrWarningConditionDto) {
        if(accountIds.size() > 0){
            // 统计
            Map<String, HighDiscountDto>  highDiscountDtoHashMap = new HashMap<>();
            List<MbrWarningDiscountInfoDto>  mbrWarningDiscountInfoDtoList1 = mbrMapper.selectWarningDeposit(warningDay, mbrWarningConditionDto.getParam1(), accountIds);
            for (MbrWarningDiscountInfoDto mbrWarningDiscountInfoDto :mbrWarningDiscountInfoDtoList1) {

                HighDiscountDto highDiscountDto =  highDiscountDtoHashMap.get(mbrWarningDiscountInfoDto.getUserName());
                if(highDiscountDto == null){
                    highDiscountDto = new HighDiscountDto();
                    highDiscountDtoHashMap.put(mbrWarningDiscountInfoDto.getUserName(), highDiscountDto);
                }
                highDiscountDto.setDeposit(highDiscountDto.getDeposit().add(mbrWarningDiscountInfoDto.getDeposit()) );
            }


            List<MbrWarningDiscountInfoDto>  mbrWarningDiscountInfoDtoList2 = mbrMapper.selectWarningDiscount(warningDay, mbrWarningConditionDto.getParam1(), accountIds);
            for (MbrWarningDiscountInfoDto mbrWarningDiscountInfoDto :mbrWarningDiscountInfoDtoList2) {

                HighDiscountDto highDiscountDto =  highDiscountDtoHashMap.get(mbrWarningDiscountInfoDto.getUserName());
                if(highDiscountDto == null){
                    highDiscountDto = new HighDiscountDto();
                    highDiscountDtoHashMap.put(mbrWarningDiscountInfoDto.getUserName(), highDiscountDto);
                }
                highDiscountDto.setDiscount(highDiscountDto.getDiscount().add(mbrWarningDiscountInfoDto.getDiscount()) );
            }

            // 写入预警
            List<MbrWarning> mbrWarnings = new ArrayList<>();
            for (Map.Entry<String, HighDiscountDto> entry: highDiscountDtoHashMap.entrySet()) {
                String username =  entry.getKey();
                HighDiscountDto  highDiscountDto = entry.getValue();

                if(highDiscountDto.getDiscount() != null && highDiscountDto.getDeposit() !=null  && highDiscountDto.getDeposit().compareTo(BigDecimal.ZERO) != 0){
                    BigDecimal rt = highDiscountDto.getDiscount().divide(highDiscountDto.getDeposit(), 2, BigDecimal.ROUND_DOWN);
                    rt = rt.multiply(new BigDecimal(100)).subtract(new BigDecimal(mbrWarningConditionDto.getParam3()));
                    if(highDiscountDto.getDeposit().compareTo(new BigDecimal(mbrWarningConditionDto.getParam2())) >= 0  && rt.compareTo(BigDecimal.ZERO) >= 0){
                        MbrWarning mbrWarning  = new MbrWarning();
                        mbrWarning.setContent(mbrWarningConditionDto.getContent());
                        mbrWarning.setStatus(Constants.EVNumber.zero);
                        mbrWarning.setWarningDate(warningDay);
                        mbrWarning.setType(mbrWarningConditionDto.getType());
                        mbrWarning.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                        mbrWarning.setLoginName(username);
                        mbrWarnings.add(mbrWarning);
                    }
                }

            }
            if(mbrWarnings.size() > 0){
                mbrWarningMapper.insertList(mbrWarnings);
            }
        }
    }


    private void warningIp(String warningDay, MbrWarningConditionDto mbrWarningConditionDto){
        List<MbrLoginWarningInfoDto> mbrLoginWarningInfoDtoList =  mbrMapper.selectLoginByIp( warningDay, mbrWarningConditionDto.getParam1() * 30 , mbrWarningConditionDto.getParam2());

        // 写入预警
        List<MbrWarning> mbrWarnings = new ArrayList<>();
        if(mbrLoginWarningInfoDtoList.size() > 0){
            for(MbrLoginWarningInfoDto mbrLoginWarningInfoDto :mbrLoginWarningInfoDtoList){
                mbrLoginWarningInfoDto.setLoginName(sortString(mbrLoginWarningInfoDto.getLoginName()));
                Integer count =  mbrWarningMapper.getWarningInfoCount(warningDay, mbrWarningConditionDto.getType(), mbrLoginWarningInfoDto.getExContent(), mbrLoginWarningInfoDto.getLoginName(), mbrWarningConditionDto.getParam1() * 30);
                if(count != null && count > 0){
                    continue;
                }
                MbrWarning mbrWarning  = new MbrWarning();
                mbrWarning.setContent(mbrWarningConditionDto.getContent());
                mbrWarning.setExContent(mbrLoginWarningInfoDto.getExContent());
                mbrWarning.setStatus(Constants.EVNumber.zero);
                mbrWarning.setWarningDate(warningDay);
                mbrWarning.setType(mbrWarningConditionDto.getType());
                mbrWarning.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                mbrWarning.setLoginName(mbrLoginWarningInfoDto.getLoginName());
                mbrWarnings.add(mbrWarning);
            }
        }
        if(mbrWarnings.size() > 0){
            mbrWarningMapper.insertList(mbrWarnings);
        }

    }


    private void warningDevice(String warningDay, MbrWarningConditionDto mbrWarningConditionDto){
        List<MbrLoginWarningInfoDto> mbrLoginWarningInfoDtoList = mbrMapper.selectLoginByDeviceuuid( warningDay, mbrWarningConditionDto.getParam1() * 30 , mbrWarningConditionDto.getParam2());

        // 写入预警
        List<MbrWarning> mbrWarnings = new ArrayList<>();
        if(mbrLoginWarningInfoDtoList.size() > 0){
            for(MbrLoginWarningInfoDto mbrLoginWarningInfoDto :mbrLoginWarningInfoDtoList){
                mbrLoginWarningInfoDto.setLoginName(sortString(mbrLoginWarningInfoDto.getLoginName()));
                Integer count =  mbrWarningMapper.getWarningInfoCount(warningDay, mbrWarningConditionDto.getType(), mbrLoginWarningInfoDto.getExContent(), mbrLoginWarningInfoDto.getLoginName(), mbrWarningConditionDto.getParam1() * 30);
                if(count != null && count > 0){
                    continue;
                }
                MbrWarning mbrWarning  = new MbrWarning();
                mbrWarning.setContent(mbrWarningConditionDto.getContent());
                mbrWarning.setExContent(mbrLoginWarningInfoDto.getExContent());
                mbrWarning.setStatus(Constants.EVNumber.zero);
                mbrWarning.setWarningDate(warningDay);
                mbrWarning.setType(mbrWarningConditionDto.getType());
                mbrWarning.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
                mbrWarning.setLoginName(mbrLoginWarningInfoDto.getLoginName());
                mbrWarnings.add(mbrWarning);
            }
        }
        if(mbrWarnings.size() > 0){
            mbrWarningMapper.insertList(mbrWarnings);
        }
    }

    private String sortString(String loginName){
        String[] loginNames = loginName.split(",");
        List<String> loginNamesList = Arrays.asList(loginNames);
        Collections.sort(loginNamesList);
        return  StringUtils.join(loginNamesList.toArray(), ",");
    }

}
