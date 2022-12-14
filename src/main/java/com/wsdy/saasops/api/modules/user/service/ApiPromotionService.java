package com.wsdy.saasops.api.modules.user.service;

import com.wsdy.saasops.api.modules.user.dto.*;
import com.wsdy.saasops.api.modules.user.mapper.ApiPromotionMapper;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrPromotionMapper;
import com.wsdy.saasops.modules.member.dto.RebateDto;
import com.wsdy.saasops.modules.member.dto.RebateLevelDto;
import com.wsdy.saasops.modules.member.dto.RebateMbrDepthDto;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
@Transactional
public class ApiPromotionService {

    @Autowired
    private MbrAccountService accountService;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrPromotionMapper promotionMapper;
    @Autowired
    private ApiPromotionMapper apiPromotionMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private JsonUtil jsonUtil;

    public PromotionDto promotionInfo(Integer accountId, Integer rebateCastDepth) {
        PromotionDto promotionDto = new PromotionDto();
        int count = mbrMapper.findPromotionCountByAccountId(accountId);
        promotionDto.setCount(count);
        promotionDto.setIsClick(Constants.EVNumber.one);
        MbrPromotion promotion = new MbrPromotion();
        promotion.setAccountId(accountId);
        MbrPromotion mbrPromotion = promotionMapper.selectOne(promotion);
        if (nonNull(mbrPromotion)) {
            promotionDto.setIsClick(mbrPromotion.getIsClick());
        }
        MbrRebate rebate = new MbrRebate();
        OprActActivity actActivity = oprActActivityCastService.getRebateAct();
        MbrAccount mbrAccount = mbrMapper.findMbrLevelAndAgyInfoById(accountId);
        if (nonNull(actActivity) && StringUtils.isNotEmpty(actActivity.getRule())) {
            List<RebateMbrDepthDto> rebateDepthDtos= getRebateCatList(actActivity,mbrAccount);
            if(Collections3.isNotEmpty(rebateDepthDtos)) {
//                for (RebateMbrDepthDto depthDto : rebateDepthDtos) {
//                    List<RebateCatDto> rebateCatDtos = new ArrayList<RebateCatDto>();
//                    for (RebateCatDto rebateCatDto : depthDto.getCatDtoList()) {
//                        if (BigDecimal.ZERO.compareTo(rebateCatDto.getTopRebate()) == 0 && BigDecimal.ZERO.compareTo(rebateCatDto.getValidBet()) == 0) {
//                            continue;
//                        }
//                        rebateCatDtos.add(rebateCatDto);
//                    }
//                }
                rebate.setRebateDepthDtos(rebateDepthDtos);
            }
        }
        promotionDto.setRebate(rebate);
        //????????????
        BigDecimal yestodayRebates = apiPromotionMapper.findYestodayRebates(accountId);
        promotionDto.setYestodayRebates(yestodayRebates==null?BigDecimal.ZERO:yestodayRebates);

        //????????????
        BigDecimal totalRebates = apiPromotionMapper.findTotalRebates(accountId);
        promotionDto.setTotalRebates(totalRebates==null?BigDecimal.ZERO:totalRebates);

        //?????????????????????
        BigDecimal totalResult = apiPromotionMapper.findTotalResult(accountId,rebateCastDepth);
        promotionDto.setTotalResult(totalResult==null?BigDecimal.ZERO:totalResult);
        return promotionDto;
    }

    private List<RebateMbrDepthDto> getRebateCatList(OprActActivity actActivity, MbrAccount account) {
        RebateDto rebateDto = jsonUtil.fromJson(actActivity.getRule(),RebateDto.class);
        Optional<RebateLevelDto> optionalLevelDto = rebateDto.getLevelDtoList().stream().filter(levelDto ->
                account.getAccountLevel().equals(levelDto.getLevel())).findFirst();
        if (optionalLevelDto.isPresent()) {
            RebateLevelDto levelDto = optionalLevelDto.get();
           // return levelDto.getDepthDtoList();
        }
        return null;
    }


    public PromotionUrlDto promotionUrl(Integer accountId, String loginName) {
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
        if(isNull(mbrAccount.getDomainCode())){
            mbrAccount.setDomainCode(getDomainCode());
            mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
        }

        MbrPromotion promotion = new MbrPromotion();
        promotion.setAccountId(accountId);
        MbrPromotion mbrPromotion = promotionMapper.selectOne(promotion);
        if (isNull(mbrPromotion)) {
            MbrPromotion promotion1 = new MbrPromotion();
            promotion1.setAccountId(accountId);
            promotion1.setLoginName(loginName);
            promotion1.setIsClick(Constants.EVNumber.zero);
            promotion1.setNumber(Constants.EVNumber.one);
            promotionMapper.insert(promotion1);
        } else {
            mbrPromotion.setIsClick(Constants.EVNumber.zero);
            mbrPromotion.setNumber(mbrPromotion.getNumber() + 1);
            promotionMapper.updateByPrimaryKey(mbrPromotion);
        }
        PromotionUrlDto promotionUrlDto = new PromotionUrlDto();
        promotionUrlDto.setPromotionUrl(accountService.promotionPCDomain(CommonUtil.getSiteCode(), mbrAccount.getDomainCode()));
        promotionUrlDto.setPromotionH5Url(accountService.promotionH5Domain(CommonUtil.getSiteCode(), mbrAccount.getDomainCode()));
        return promotionUrlDto;
    }

    public String getDomainCode() {
        Boolean flag = Boolean.TRUE;
        MbrAccount tempAcc;
        while (flag){
            long numbers = (long)(Math.random()*9*Math.pow(10,8-1)) + (long)Math.pow(10,8-1);
            tempAcc = new MbrAccount();
            tempAcc.setDomainCode(String.valueOf(numbers));
            List<MbrAccount> listAcc = mbrAccountMapper.select(tempAcc);
            if(Collections3.isNotEmpty(listAcc)){
                continue;
            }
            return numbers+"";
        }
        return "";
    }

    public String getExtendLoginNameCode(String siteCode) {
        Boolean flag = Boolean.TRUE;
        MbrAccount tempAcc;
        while (flag){
            long numbers = (long)(Math.random()*9*Math.pow(10,7-1)) + (long)Math.pow(10,7-1);
            tempAcc = new MbrAccount();
            tempAcc.setLoginName(siteCode.concat(String.valueOf(numbers)));
            List<MbrAccount> listAcc = mbrAccountMapper.select(tempAcc);
            if(Collections3.isNotEmpty(listAcc)){
                continue;
            }
            return siteCode.concat(numbers+"");
        }
        return "";
    }

    public void promotionQRCode(Integer accountId, HttpServletResponse response) {
        accountService.accountQrCode(accountId, response, CommonUtil.getSiteCode());
    }

    public void addAccount(Integer accountId, MbrAccount account) {
        account.setCodeId(accountId);
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
        account.setCagencyId(mbrAccount.getCagencyId());
        accountService.adminSave(account, null,null,null, Boolean.TRUE, Constants.EVNumber.six);
    }

    public List<RebateReportDto> rebateReport(MbrRebateReportNew rebateReport) {
        List<MbrRebateReport> rebateReports = mbrMapper.findRebateReportList(rebateReport);
        List<RebateReportDto> reportDtos = Lists.newArrayList();
        RebateReportDto reportDto = new RebateReportDto();
        reportDto.setReportList(rebateReports);
        reportDtos.add(reportDto);
        return reportDtos;
    }

    public BigDecimal getRebateTotalByDepth(MbrRebateReportNew rebateReport,Integer startDepth,Integer endDepth){
        rebateReport.setLowDepth(startDepth);
        rebateReport.setHighDepth(endDepth);
        return apiPromotionMapper.getRebateTotalByDepth(rebateReport);
    }

    public List<RebateReportDto> rebateContributeReport(MbrRebateReportNew rebateReport) {
        List<MbrRebateReport> rebateReports = mbrMapper.rebateContributeReport(rebateReport);
        List<RebateReportDto> reportDtos = Lists.newArrayList();
        RebateReportDto reportDto = new RebateReportDto();
        reportDto.setReportList(rebateReports);
        reportDtos.add(reportDto);
        return reportDtos;
    }

    public int findActiveUserCount(MbrRebateReportNew rebateReport) {
        int activeCount = apiPromotionMapper.findActiveUserCount(rebateReport);
        return activeCount;
    }

    /*public BigDecimal findTotalBalance(MbrRebateReport rebateReport) {
        BigDecimal totalBalance = apiPromotionMapper.findTotalBalance(rebateReport);
        return totalBalance==null?BigDecimal.ZERO:totalBalance;
    }*/

    public BigDecimal findValidBetTotal(RebateAccountDto rebateAccountDto) {
        BigDecimal activeCount = apiPromotionMapper.findValidBetTotal(rebateAccountDto);
        return activeCount==null?BigDecimal.ZERO:activeCount;
    }

    public PageUtils recentlyActive(MbrRebateReportNew rebateReport) {
        PageHelper.startPage(rebateReport.getPageNo(), rebateReport.getPageSize());
        // loginTime ??????  rebate ??????   registerTime z????????????
        if (!StringUtils.isEmpty(rebateReport.getOrder())) {
            PageHelper.orderBy(rebateReport.getOrder()+" desc,accountid asc");  // ???accountid asc ??????rebate?????????????????????
        }
        List<RebateAccountDto> resultList = apiPromotionMapper.recentlyActive(rebateReport.getAccountId(),rebateReport.getDepth());
        PageUtils p = BeanUtil.toPagedResult(resultList);
        return p;
    }

    public RebateAccountDto rebateInfo(Integer accountId) {
        RebateAccountDto resultInfo = apiPromotionMapper.rebateInfo(accountId);
        if(null==resultInfo.getRebate()){
            resultInfo.setRebate(BigDecimal.ZERO);
        }
        return resultInfo;
    }

    // ??????????????????????????????????????????
    public void verifyRebateRatio(MbrAccount mbrAccount){
        MbrAccount parent = findParentInfo(mbrAccount);             // ??????
        List<MbrAccount> childsList = findChildsInfo(mbrAccount);   // ??????

        // ?????????????????????
        if(Objects.isNull(parent) && Objects.isNull(childsList)){
            return;
        }
        // ??????????????????null????????????0
        if(Objects.nonNull(parent) && Objects.isNull(parent.getRebateRatio())){
            parent.setRebateRatio(new BigDecimal(Constants.EVNumber.zero));
        }
        // ??????????????????null????????????0
        if(Objects.nonNull(childsList)){
            childsList.stream().forEach(child -> {
                if(Objects.nonNull(child) && Objects.isNull(child.getRebateRatio())){
                    child.setRebateRatio(new BigDecimal(Constants.EVNumber.zero));
                }
            });
        }

        // ??????/??????/?????????  ????????????0,????????????
        if(mbrAccount.getRebateRatio().compareTo(new BigDecimal(Constants.EVNumber.zero)) == 0){    // ????????????0
            if(Objects.nonNull(parent) && parent.getRebateRatio().compareTo(new BigDecimal(Constants.EVNumber.zero)) == 0){    // ?????????0
                // ??????????????????null????????????0
                if(Objects.nonNull(childsList)){
                    boolean flag = true;
                    for(MbrAccount child : childsList){
                        if(Objects.nonNull(child) && child.getRebateRatio().compareTo(new BigDecimal(Constants.EVNumber.zero)) != 0){
                            flag = false;
                        }
                    }
                    if(flag){   // ???????????????0
                        return ;
                    }
                }
            }
        }

        // ????????????????????????
        if(Objects.nonNull(parent)){
            if(mbrAccount.getRebateRatio().compareTo(parent.getRebateRatio())>= 0){
                throw new R200Exception("??????????????????????????????????????????????????????");
            }
        }
        // ????????????????????????
        if(Objects.nonNull(childsList)){
            childsList.stream().forEach(child -> {
                if(child.getRebateRatio().compareTo(mbrAccount.getRebateRatio())>= 0){
                    log.info("????????????????????????????????????????????????????????????????????????" +  child.getLoginName() + "(" + child.getRebateRatio() + "%)");
                    throw new R200Exception("????????????????????????????????????????????????????????????" );
                }
            });
        }
    }

    // ?????????????????????????????????
    public MbrAccount findParentInfo(MbrAccount mbrAccount) {
        List<MbrAccount>  parentMbrList = apiPromotionMapper.findParentInfo(mbrAccount);
        if(Objects.isNull(parentMbrList) || parentMbrList.size() == Constants.EVNumber.zero){
            return null;
        }
        return parentMbrList.get(0);
    }
    // ????????????????????????????????????
    public List<MbrAccount>  findChildsInfo(MbrAccount mbrAccount) {
        List<MbrAccount>  ChildsMbrList = apiPromotionMapper.findChildsInfo(mbrAccount);
        if(Objects.isNull(ChildsMbrList) || ChildsMbrList.size() == Constants.EVNumber.zero){
            return null;
        }
        return ChildsMbrList;
    }
    // ??????????????????
    public Integer getMbrTreeDepth(MbrAccount mbrAccount) {
        Integer maxDepth = apiPromotionMapper.getMbrTreeDepth(mbrAccount);
        return maxDepth;
    }

    // ????????????????????????
    public PageUtils getSubAccRebateRatio(RebateAccSanGongDto rebateAccSanGongDto) {
        PageHelper.startPage(rebateAccSanGongDto.getPageNo(), rebateAccSanGongDto.getPageSize());
        List<RebateAccSanGongDto> resultList = apiPromotionMapper.getSubAccRebateRatio(rebateAccSanGongDto);
        return BeanUtil.toPagedResult(resultList);
    }
    // ?????????????????????
    public boolean verifyMbrRelation(Integer childNodeId, Integer parentId) {
        MbrTree mbrTree = apiPromotionMapper.verifyMbrRelation(childNodeId, parentId);
        if(Objects.isNull(mbrTree)){
            return false;
        }
        return true;
    }
    // ????????????????????????????????????
    public RebateAccSanGongSumDto getSubAccRebateSum(RebateAccSanGongSumDto rebateAccSanGongSumDto) {
        RebateAccSanGongSumDto sum = apiPromotionMapper.getSubAccRebateSum(rebateAccSanGongSumDto);
        // ???????????????????????????????????????????????????
        BigDecimal  totalAmountForParent = apiPromotionMapper.getTotalRebateForParent(rebateAccSanGongSumDto);
        sum.setTotalAmountForParent(totalAmountForParent);
        return sum;
    }

    // ????????????????????????????????????
    public PageUtils getSubAccRebateDetail(RebateAccSanGongDetailDto rebateAccSanGongDetailDto) {
        PageHelper.startPage(rebateAccSanGongDetailDto.getPageNo(), rebateAccSanGongDetailDto.getPageSize());
        List<RebateAccSanGongDetailDto> resultList = apiPromotionMapper.getSubAccRebateDetail(rebateAccSanGongDetailDto);
        return BeanUtil.toPagedResult(resultList);
    }

    // ?????????????????????????????????
    public PromotionDto getRebateInfo(Integer accountId) {
        PromotionDto promotionDto = new PromotionDto();
        //????????????
        BigDecimal yestodayRebates = apiPromotionMapper.findYestodayRebates(accountId);
        promotionDto.setYestodayRebates(yestodayRebates==null?BigDecimal.ZERO:yestodayRebates);

        //????????????
        BigDecimal totalRebates = apiPromotionMapper.findTotalRebates(accountId);
        promotionDto.setTotalRebates(totalRebates==null?BigDecimal.ZERO:totalRebates);

        // ?????????????????????
        Integer count = apiPromotionMapper.getChildCount(accountId);
        promotionDto.setCount(count);
        return promotionDto;
    }
}
