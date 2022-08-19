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
        //昨日返点
        BigDecimal yestodayRebates = apiPromotionMapper.findYestodayRebates(accountId);
        promotionDto.setYestodayRebates(yestodayRebates==null?BigDecimal.ZERO:yestodayRebates);

        //累计收益
        BigDecimal totalRebates = apiPromotionMapper.findTotalRebates(accountId);
        promotionDto.setTotalRebates(totalRebates==null?BigDecimal.ZERO:totalRebates);

        //当月好友总输赢
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
        // loginTime 最近  rebate 返点   registerTime z最近添加
        if (!StringUtils.isEmpty(rebateReport.getOrder())) {
            PageHelper.orderBy(rebateReport.getOrder()+" desc,accountid asc");  // 加accountid asc 避免rebate相同时随机排序
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

    // 校验会员返利比例是否符合规则
    public void verifyRebateRatio(MbrAccount mbrAccount){
        MbrAccount parent = findParentInfo(mbrAccount);             // 上级
        List<MbrAccount> childsList = findChildsInfo(mbrAccount);   // 下级

        // 没有上级和下级
        if(Objects.isNull(parent) && Objects.isNull(childsList)){
            return;
        }
        // 上级的返利为null，则置为0
        if(Objects.nonNull(parent) && Objects.isNull(parent.getRebateRatio())){
            parent.setRebateRatio(new BigDecimal(Constants.EVNumber.zero));
        }
        // 下级的返利为null，则置为0
        if(Objects.nonNull(childsList)){
            childsList.stream().forEach(child -> {
                if(Objects.nonNull(child) && Objects.isNull(child.getRebateRatio())){
                    child.setRebateRatio(new BigDecimal(Constants.EVNumber.zero));
                }
            });
        }

        // 上级/下级/待修改  三者都为0,允许修改
        if(mbrAccount.getRebateRatio().compareTo(new BigDecimal(Constants.EVNumber.zero)) == 0){    // 修改者为0
            if(Objects.nonNull(parent) && parent.getRebateRatio().compareTo(new BigDecimal(Constants.EVNumber.zero)) == 0){    // 上级为0
                // 下级的返利为null，则置为0
                if(Objects.nonNull(childsList)){
                    boolean flag = true;
                    for(MbrAccount child : childsList){
                        if(Objects.nonNull(child) && child.getRebateRatio().compareTo(new BigDecimal(Constants.EVNumber.zero)) != 0){
                            flag = false;
                        }
                    }
                    if(flag){   // 所有下级为0
                        return ;
                    }
                }
            }
        }

        // 校验上级返利比例
        if(Objects.nonNull(parent)){
            if(mbrAccount.getRebateRatio().compareTo(parent.getRebateRatio())>= 0){
                throw new R200Exception("会员三公比例不能高于或等于上级会员！");
            }
        }
        // 校验下级返利比例
        if(Objects.nonNull(childsList)){
            childsList.stream().forEach(child -> {
                if(child.getRebateRatio().compareTo(mbrAccount.getRebateRatio())>= 0){
                    log.info("会员三公比例不能低于或等于现有下级会员！如会员：" +  child.getLoginName() + "(" + child.getRebateRatio() + "%)");
                    throw new R200Exception("会员三公比例不能低于或等于现有下级会员！" );
                }
            });
        }
    }

    // 查询会员父推广会员信息
    public MbrAccount findParentInfo(MbrAccount mbrAccount) {
        List<MbrAccount>  parentMbrList = apiPromotionMapper.findParentInfo(mbrAccount);
        if(Objects.isNull(parentMbrList) || parentMbrList.size() == Constants.EVNumber.zero){
            return null;
        }
        return parentMbrList.get(0);
    }
    // 查询会员下级推广会员信息
    public List<MbrAccount>  findChildsInfo(MbrAccount mbrAccount) {
        List<MbrAccount>  ChildsMbrList = apiPromotionMapper.findChildsInfo(mbrAccount);
        if(Objects.isNull(ChildsMbrList) || ChildsMbrList.size() == Constants.EVNumber.zero){
            return null;
        }
        return ChildsMbrList;
    }
    // 查询会员深度
    public Integer getMbrTreeDepth(MbrAccount mbrAccount) {
        Integer maxDepth = apiPromotionMapper.getMbrTreeDepth(mbrAccount);
        return maxDepth;
    }

    // 获得下级会员数据
    public PageUtils getSubAccRebateRatio(RebateAccSanGongDto rebateAccSanGongDto) {
        PageHelper.startPage(rebateAccSanGongDto.getPageNo(), rebateAccSanGongDto.getPageSize());
        List<RebateAccSanGongDto> resultList = apiPromotionMapper.getSubAccRebateRatio(rebateAccSanGongDto);
        return BeanUtil.toPagedResult(resultList);
    }
    // 校验上下级关系
    public boolean verifyMbrRelation(Integer childNodeId, Integer parentId) {
        MbrTree mbrTree = apiPromotionMapper.verifyMbrRelation(childNodeId, parentId);
        if(Objects.isNull(mbrTree)){
            return false;
        }
        return true;
    }
    // 下级会员返利收益总览数据
    public RebateAccSanGongSumDto getSubAccRebateSum(RebateAccSanGongSumDto rebateAccSanGongSumDto) {
        RebateAccSanGongSumDto sum = apiPromotionMapper.getSubAccRebateSum(rebateAccSanGongSumDto);
        // 查出该上级会员对他的上级会员的贡献
        BigDecimal  totalAmountForParent = apiPromotionMapper.getTotalRebateForParent(rebateAccSanGongSumDto);
        sum.setTotalAmountForParent(totalAmountForParent);
        return sum;
    }

    // 下级会员返利收益明细数据
    public PageUtils getSubAccRebateDetail(RebateAccSanGongDetailDto rebateAccSanGongDetailDto) {
        PageHelper.startPage(rebateAccSanGongDetailDto.getPageNo(), rebateAccSanGongDetailDto.getPageSize());
        List<RebateAccSanGongDetailDto> resultList = apiPromotionMapper.getSubAccRebateDetail(rebateAccSanGongDetailDto);
        return BeanUtil.toPagedResult(resultList);
    }

    // 获得昨日收益和累计收益
    public PromotionDto getRebateInfo(Integer accountId) {
        PromotionDto promotionDto = new PromotionDto();
        //昨日返点
        BigDecimal yestodayRebates = apiPromotionMapper.findYestodayRebates(accountId);
        promotionDto.setYestodayRebates(yestodayRebates==null?BigDecimal.ZERO:yestodayRebates);

        //累计收益
        BigDecimal totalRebates = apiPromotionMapper.findTotalRebates(accountId);
        promotionDto.setTotalRebates(totalRebates==null?BigDecimal.ZERO:totalRebates);

        // 获得下级会员数
        Integer count = apiPromotionMapper.getChildCount(accountId);
        promotionDto.setCount(count);
        return promotionDto;
    }
}
