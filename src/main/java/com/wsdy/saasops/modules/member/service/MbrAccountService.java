package com.wsdy.saasops.modules.member.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.waterRebatesCode;
import static com.wsdy.saasops.modules.sys.service.SysEncryptionService.PREFIX_SPLICE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.beust.jcommander.internal.Lists;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.reflect.TypeToken;
import com.wsdy.saasops.api.config.ApiConfig;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.dao.TCpSiteMapper;
import com.wsdy.saasops.api.modules.pay.service.PayInfoService;
import com.wsdy.saasops.api.modules.user.dto.PwdDto;
import com.wsdy.saasops.api.modules.user.dto.UserDto;
import com.wsdy.saasops.api.modules.user.service.ApiPromotionService;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.modules.user.service.SendSmsSevice;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ColumnAuthConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.GroupByConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CheckIpUtils;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.EncryptioUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.QrCodeCreateUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgyDomainMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.analysis.mapper.AnalysisMapper;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.fund.service.FundReportService;
import com.wsdy.saasops.modules.fund.service.FundWithdrawService;
import com.wsdy.saasops.modules.log.dao.LogMbrregisterMapper;
import com.wsdy.saasops.modules.log.entity.LogMbrLogin;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.log.service.LogMbrloginService;
import com.wsdy.saasops.modules.member.dao.MbrAccountCallrecordMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrActivityLevelMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepositCountMapper;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.dao.MbrUseDeviceMapper;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.dao.MbrWithdrawalCondMapper;
import com.wsdy.saasops.modules.member.dao.SysEncryptMapper;
import com.wsdy.saasops.modules.member.dto.BatchUpdateActLevelDto;
import com.wsdy.saasops.modules.member.dto.ItemDto;
import com.wsdy.saasops.modules.member.dto.MbrAccountLastBetDate;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountCallrecord;
import com.wsdy.saasops.modules.member.entity.MbrAccountDevice;
import com.wsdy.saasops.modules.member.entity.MbrAccountMobile;
import com.wsdy.saasops.modules.member.entity.MbrAccountOnline;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevel;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrDepositCount;
import com.wsdy.saasops.modules.member.entity.MbrFundTotal;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.entity.MbrUseDevice;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.entity.MbrWithdrawalCond;
import com.wsdy.saasops.modules.member.entity.SysEncrypt;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.entity.OprActRule;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.sys.dto.ColumnAuthTreeDto;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.ColumnAuthProviderService;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.wsdy.saasops.modules.task.entity.TaskBonus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MbrAccountService extends BaseService<MbrAccountMapper, MbrAccount> {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private MbrGroupMapper mbrGroupMapper;
    @Autowired
    private LogMbrregisterMapper logMbrregisterMapper;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Autowired
    private IpService ipService;
    @Autowired
    private ApiConfig apiConfig;
    @Autowired
    private LogMbrloginService logLoginService;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private FundWithdrawService withdrawService;
    @Autowired
    private FundDepositService depositService;
    @Autowired
    private FundReportService reportService;
    @Autowired
    private MbrAccountLogService accountLogService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Value("${api.regDeaultCagencyId}")
    private int cagencyId;
    @Autowired
    private ColumnAuthProviderService columnAuthProviderService;
    @Autowired
    private LogMbrloginService logMbrloginService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrActivityLevelMapper mbrActivityLevelMapper;
    @Autowired
    private AgyDomainMapper agyDomainMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private MbrAccountCallrecordMapper mbrAccountCallrecordMapper;
    @Value("${recommend.register.H5}")
    private String registerH5;
    @Value("${recommend.register.pc}")
    private String registerPC;

    @Value("${callCenter.url}")
    private String callCenterUrl;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private OperateActivityMapper activityMapper;
    @Autowired
    public TCpSiteMapper tCpSiteMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ApiPromotionService promotionService;
    @Autowired
    private SendSmsSevice sendSmsSevice;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private AnalysisMapper analysisMapper;
    @Autowired
    private SysEncryptMapper sysEncryptMapper;
    @Autowired
    private MbrCryptoCurrenciesService mbrCryptoCurrenciesService;
    @Autowired
    private MbrWithdrawalCondMapper withdrawalCondMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private MbrAccountMobileService accountMobileService;
    @Autowired
    private MbrDepositCountMapper mbrDepositCountMapper;
    @Autowired
    private PayInfoService payInfoService;
    @Autowired
    private CheckIpUtils checkIpUtils;
    @Autowired
    private MbrUseDeviceMapper mbrUseDeviceMapper;
    @Autowired
    private MbrDepositLockLogService mbrDepositLockLogService;

    /**
     * check.iplink
     *
     * @param mbrAccount
     * @param pageNo
     * @param pageSize
     * @param orderBy
     * @return
     */
    @Transactional
    public PageUtils queryListPage(MbrAccount mbrAccount, Integer pageNo, Integer pageSize, String orderBy, Integer roleId) {
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuthByFlag(roleId, ColumnAuthConstants.MEMBER_LIST_VIEW_MENU_ID);
        if (Collections3.isEmpty(menuList)) {
            throw new AuthorizationException("没有权限，请联系管理员授权");
        }

        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            if ("cagencyId".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add("agyAccount");
                columnSets.add("tagencyId");
            } else if ("groupId".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add("groupName");
            } else if ("ManualAdjustment".equals(columnAuthTreeDto.getColumnName()) || "ManualReduceAdjustment".equals(columnAuthTreeDto.getColumnName())) {
                continue;
            } else if ("actLevelId".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add("actLevelId");
                columnSets.add("tierName");
                columnSets.add("isActivityLock");
            } else {
                columnSets.add(columnAuthTreeDto.getColumnName());
            }
        }
        columnSets.add("id");
        mbrAccount.setColumnSets(columnSets);
        if (nonNull(mbrAccount) && StringUtil.isNotEmpty(mbrAccount.getMobile())) {
            mbrAccount.setMobileEncrypt(getMobileEncrypt(mbrAccount.getMobile()));
        }
        PageHelper.startPage(pageNo, pageSize);
        if (StringUtil.isNotEmpty(mbrAccount.getLoginName())) {
            orderBy = "convert(loginName using gbk) collate gbk_chinese_ci asc";
        }
        PageHelper.orderBy(orderBy);
		// 会员列表
		List<MbrAccount> list = mbrMapper.findAccountList(mbrAccount);
		if (CollectionUtils.isNotEmpty(list)) {
			Set<String> loginNames = list.stream().map(MbrAccount::getLoginName).collect(Collectors.toSet());
			// 会员的最后投注日（投注大于100的最近一天）
			List<MbrAccountLastBetDate> lastBetDateAccounts = mbrMapper.lastBetDate(loginNames);
			for (MbrAccount targetAccount : list) {
				Optional<MbrAccountLastBetDate> findFirst = lastBetDateAccounts.stream()
						.filter(t -> t.getLoginName().equals(targetAccount.getLoginName())).findFirst();
				if (findFirst.isPresent()) {
					targetAccount.setBetDate(findFirst.get().getBetDate());
				}
				// 优化-登录日志表过大，去掉连接查询，单独查询
                if (columnSets.contains("loginType") || columnSets.contains("loginArea")) {
                    MbrAccount lastLogin = mbrMapper.findAccountLastLogin(targetAccount);
                    if (columnSets.contains("loginArea")) {
                        targetAccount.setLoginArea(lastLogin==null?null:lastLogin.getLoginArea());
                    }
                    if (columnSets.contains("loginType")) {
                        targetAccount.setLoginType(lastLogin==null?null:lastLogin.getLoginType());
                    }
                }
			}
		}
        return BeanUtil.toPagedResult(list);
    }

    public Integer countOnline(){
        return mbrMapper.countOnline();
    }


    public PageUtils queryAgentMbrListPage(MbrAccount mbrAccount, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy(orderBy);
        List<MbrAccount> list = mbrMapper.queryAgentMbrListPage(mbrAccount);
        return BeanUtil.toPagedResult(list);
    }


    public List<ColumnAuthTreeDto> querySeachCondition(Integer roleId, Long typeId) {
        //获取用户列权限   id为4查询列表显示字段,为5查询搜索栏显示字段
        List<ColumnAuthTreeDto> resultList = new ArrayList<>();
        if (typeId == 4) {    // 本次改造后，type=4不存在，使用字段isColum控制，1表示为列显示字段
            resultList = columnAuthProviderService.getRoleColumnAuthByFlag(roleId, ColumnAuthConstants.MEMBER_LIST_VIEW_MENU_ID);

        }
        if (typeId == 5) {    // 原逻辑不变
            resultList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_LIST_MENU_ID, typeId);
        }
        return resultList;
    }


    public List<ColumnAuthTreeDto> columnFrameList(Integer roleId) {
        List<ColumnAuthTreeDto> allList = columnAuthProviderService.getAllColumnAuth(ColumnAuthConstants.MEMBER_LIST_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE, roleId);
        Set<Long> set = new HashSet<>();
        getMenuChildIds(set, allList);
        ColumnAuthTreeDto paramDto = new ColumnAuthTreeDto();
        paramDto.setParamList(set);
        paramDto.setMenuId(ColumnAuthConstants.MEMBER_LIST_MENU_ID);
        paramDto.setRoleId(roleId);
        List<ColumnAuthTreeDto> resultList = columnAuthProviderService.getRoleAuth(paramDto);
        return resultList;
    }

    private void getMenuChildIds(Set<Long> set, List<ColumnAuthTreeDto> list) {
        if (Collections3.isNotEmpty(list)) {
            for (ColumnAuthTreeDto columnTreeDto : list) {
                set.add(columnTreeDto.getMenuId());
                getMenuChildIds(set, columnTreeDto.getChildList());
            }
        }
    }

    // 根据用户Id 查询
    public MbrAccount getAccountInfo(Integer userId) {
        MbrAccount info = new MbrAccount();
        info.setId(userId);
        return queryObjectCond(info);
    }

    // 根据用户名 查询
    public MbrAccount getAccountInfo(String loginName) {
        MbrAccount info = new MbrAccount();
        info.setLoginName(loginName);
        return queryObjectCond(info);

    }

    // 根据手机号 查询
    public List<MbrAccount> getAccountInfoByMobile(String mobile, Integer isVerifyMoblie) {
        List<String> mobileEncrypt = getMobileEncrypt(mobile);
        return mbrMapper.queryAccountMobileEncrypt(mobile, mobileEncrypt, isVerifyMoblie);
    }

    // 根据手机号 查询
    public List<MbrAccount> getMtAccountInfoByMobile(String mobile, Integer isVerifyMoblie, Integer tagencyId) {
        List<String> mobileEncrypt = getMobileEncrypt(mobile);
        return mbrMapper.queryMtAccountMobileEncrypt(mobile, mobileEncrypt, isVerifyMoblie, tagencyId);
    }

    // 查询当前用户表最大ID
    public Integer getAccountMaxId() {
        return mbrMapper.getAccountMaxId();
    }

    public List<String> getMobileEncrypt(String mobile) {
        List<SysEncrypt> encrypts = sysEncryptMapper.findEncryptDescDel();
        if (encrypts.size() > 0) {
            List<String> stringList = Lists.newArrayList();
            encrypts.stream().forEach(es -> {
                String str = PREFIX_SPLICE + EncryptioUtil.encrypt(mobile, es.getDessecretkey());
                stringList.add(str);
            });
            return stringList;
        }
        return null;
    }

    public MbrAccount getAccountInfoByMobileOne(String mobile, Integer isVerifyMoblie) {
        List<MbrAccount> accountList = getAccountInfoByMobile(mobile, isVerifyMoblie);
        if (accountList.size() > 0) {
            return accountList.get(0);
        }
        return null;
    }

    public MbrAccount getMtAccountInfoByMobileOne(String mobile, Integer isVerifyMoblie, Integer tagencyId) {
        List<MbrAccount> accountList = getMtAccountInfoByMobile(mobile, isVerifyMoblie, tagencyId);
        if (accountList.size() > 0) {
            return accountList.get(0);
        }
        return null;
    }


    public int getAccountNum(String loginName) {
        MbrAccount info = new MbrAccount();
        info.setLoginName(loginName);
        return selectCount(info);
    }

    /**
     * 返回在线会员
     *
     * @param mbrAccountOnline
     * @param pageNo
     * @param pageSize
     * @param orderBy
     * @return
     */
    public PageUtils queryListOnlinePage(MbrAccountOnline mbrAccountOnline, Integer pageNo, Integer pageSize,
                                         String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        orderBy = GroupByConstants.getOrderBy(GroupByConstants.onlineAccountMod, orderBy);
        PageHelper.orderBy(orderBy);
        List<MbrAccountOnline> list = mbrMapper.findAccountOnlineList(mbrAccountOnline);
        list.forEach(e1 -> {
            String  strOnlineTime = StringUtil.formatOnlineTime(e1.getOnlineTime());
            e1.setOnlineTimeStr(strOnlineTime.length() ==0 ? "0分" : strOnlineTime);
            String selectOnlineTimeStr =  StringUtil.formatOnlineTime(e1.getSelectOnlineTime());
            e1.setSelectOnlineTimeStr(selectOnlineTimeStr.length() == 0 ? "0分": selectOnlineTimeStr);
        });
        return BeanUtil.toPagedResult(list);
    }

    public int changeMbrGroup(MbrAccount mbrAccount, String userName, String ip) {
        int returnValue = mbrMapper.changeMbrGroup(mbrAccount);
        //添加操作日志
        MbrGroup mbrGroup = mbrGroupMapper.selectByPrimaryKey(mbrAccount.getGroupId());
        List<MbrAccount> accountList = mbrMapper.findMbrAccountList(mbrAccount);
        for (MbrAccount tempAccount : accountList) {
            mbrAccountLogService.changeMbrGroupLog(tempAccount, mbrGroup.getGroupName(), userName, ip);
        }
        return returnValue;
    }

    /**
     * 后端注册
     * 1. 推荐好友新增会员
     * 2. v2后台：会员列表新增和代理列表新增；
     * 3. 代理后台：新增会员；
     *
     * @param mbrAccount   注册数据
     * @param agentAccount 注册代理数据
     * @param userName     管理后台用户名
     * @param ip           注册ip
     * @param isSign       false: agentAccount 有值，表示代理后台注册；true: agentAccount 为 null
     * @param source       调用接口来源 1管理后端(v2)  5代理后台  6帮好友注册
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void adminSave(MbrAccount mbrAccount, AgentAccount agentAccount, String userName, String ip, Boolean isSign, int source) {
        if (Boolean.FALSE.equals(mbrAccount.getAddAgent())) {
            accountMobileService.checkAccountMobile(mbrAccount.getMobile());
            checkoutUsername(mbrAccount.getLoginName());
        }
        // 此处增加EG三公校验
        checkoutEgSanGong(mbrAccount);
        accountMobileService.checkAccountMobile(mbrAccount.getMobile());
        // SDY废弃：此处增加EG三公校验
//        checkoutEgSanGong(mbrAccount);
        setAdminMbrAccount(mbrAccount, agentAccount, isSign);
        String salt = RandomStringUtils.randomAlphanumeric(20);
        mbrAccount.setLoginPwd(new Sha256Hash(mbrAccount.getLoginPwd(), salt).toHex());
        mbrAccount.setSalt(salt);
        String currentDate = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
        mbrAccount.setRegisterTime(currentDate);
        mbrAccount.setModifyTime(currentDate);
        mbrAccount.setLoginTime(currentDate);
        mbrAccount.setDomainCode(getDomainCode());
        // 昵称默认使用loginname
        mbrAccount.setNickName(mbrAccount.getLoginName());
        mbrAccount.setDepositLock(Constants.EVNumber.zero); // 存款锁定状态
        mbrAccount.setAgyflag(Constants.EVNumber.zero);     // 全民代理标志 0非代理会员
        // 设置手机区号默认中国区号
        if (!StringUtil.isEmpty(mbrAccount.getMobile())) {
            if (StringUtil.isEmpty(mbrAccount.getMobileAreaCode())) {
                mbrAccount.setMobileAreaCode("86");
            }
        }
        mbrAccount.setLabelid(Constants.EVNumber.one);
        super.save(mbrAccount);
        mbrWalletMapper.insert(getMbrWallet(mbrAccount));
        setMbrNode(mbrAccount, mbrAccount.getCodeId(), Boolean.FALSE);
        accountMobileService.addMbrAccountMobile(mbrAccount, userName);
        logMbrregisterMapper.insert(getlogRegister(mbrAccount, Integer.valueOf(source).byteValue()));

        //添加操作日志
        if (StringUtils.isNotEmpty(userName)) {
            accountLogService.addAccountLog(mbrAccount, userName, ip);
        }
    }

    private void setAdminMbrAccount(MbrAccount mbrAccount, AgentAccount agentAccount, Boolean isSign) {
        mbrAccount.setId(null);
        mbrAccount.setIsVerifyMoblie(Available.disable);
        mbrAccount.setIsAllowMsg(Available.disable);
        mbrAccount.setIsLock(Available.disable);
        mbrAccount.setAvailable(Available.enable);
        mbrAccount.setIsVerifyEmail(Available.disable);
        mbrAccount.setIsAllowEmail(Available.enable);
        mbrAccount.setIsOnline(Available.disable);
        mbrAccount.setActLevelId(Constants.EVNumber.one);
        mbrAccount.setIsActivityLock(Constants.EVNumber.zero);
        // 代理处理逻辑
        if (Boolean.TRUE.equals(isSign)) {  // 非代理后台注册
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(mbrAccount.getMobile())) {
                mbrAccount.setIsVerifyMoblie(Available.enable);
                mbrAccount.setIsAllowMsg(Available.enable);
            }
            AgentAccount agentAccount1 = agentAccountMapper.selectByPrimaryKey(mbrAccount.getCagencyId());
            if (agentAccount1.getAttributes() == 1) {
                mbrAccount.setCagencyId(agentAccount1.getSuperiorCloneId());
                mbrAccount.setSubCagencyId(agentAccount1.getId());

                AgentAccount agentAccount2 = agentAccountMapper.selectByPrimaryKey(agentAccount1.getSuperiorCloneId());
                AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount2);
                mbrAccount.setTagencyId(tAgent.getId());
            } else {
                mbrAccount.setCagencyId(agentAccount1.getId());
                // 获取总代-无限层级
                AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount1);
                mbrAccount.setTagencyId(tAgent.getId());
            }
            // 关联组处理逻辑
            if (isNull(mbrAccount.getGroupId())) {  // 代理列表新增&推荐好友注册
                if (Objects.isNull(agentAccount1.getGroupId())) {     // 无关联则默认代理
                    mbrAccount.setGroupId(apiConfig.getRegDeaultGroup());
                } else {  // 存在关联则设置关联
                    mbrAccount.setGroupId(agentAccount1.getGroupId());
                }
            }
        } else {    // 代理后台注册
            mbrAccount.setCagencyId(agentAccount.getId());
            mbrAccount.setTagencyId(agentAccount.getParentId());

            // 关联组处理逻辑
            if (isNull(mbrAccount.getGroupId())) {  // 无关联默认代理
                mbrAccount.setGroupId(apiConfig.getRegDeaultGroup());
            }
        }
    }

    /**
     * 会员注册
     * 1. 会员注册
     *
     * @param mbrAccount
     * @param userDto
     * @return
     */
    @Transactional
    public MbrAccount webSave(MbrAccount mbrAccount, UserDto userDto) {
        // 校验用户名是否唯一
        checkoutUsername(userDto.getLoginName());
        // 构造用户数据
        setWebMbrAccount(mbrAccount, userDto);
        // 会员组处理：上级(一级代理)代理有设置关联会员组跟随上级代理，没有设置null则使用默认会员组
        Integer groupId = null;
        if (StringUtils.isNotEmpty(mbrAccount.getSpreadCode())) {
        	// 当前推广码对应的代理
            AgentAccount agent = new AgentAccount();
            agent.setSpreadCode(mbrAccount.getSpreadCode());
            AgentAccount agentAccount = agentAccountMapper.selectOne(agent);
            if (Objects.nonNull(agentAccount)) {
                mbrAccount.setCagencyId(agentAccount.getId());
                // 获取总代-无限层级
                AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount);
                mbrAccount.setTagencyId(tAgent.getId());
                // 如果代理的下级默认会员组不为空
                if (tAgent.getDefaultGroupId() != null) {
                	groupId = agentAccount.getDefaultGroupId();
                } else {
                	groupId = agentAccount.getGroupId();
                }
            }
        }
        if (nonNull(userDto.getCodeId())) {
            // SDY全民代理通过会员推广注册，使用默认会员组和域名绑定代理，所以下列屏蔽
//            MbrAccount reqMbrAccount = new MbrAccount();
//            reqMbrAccount.setDomainCode(userDto.getCodeId().toString());
//            MbrAccount mbrAccount1 = mbrAccountMapper.selectOne(reqMbrAccount);

//            mbrAccount.setCagencyId(mbrAccount1.getCagencyId());
//            mbrAccount.setTagencyId(mbrAccount1.getTagencyId());
//            // 好友链接注册也要到代理的关联组里
//            AgentAccount agent = new AgentAccount();
//            agent.setId(mbrAccount1.getCagencyId());
//            AgentAccount agentAccount = agentAccountMapper.selectOne(agent);
//            if (Objects.nonNull(agentAccount)) {
//                groupId = agentAccount.getGroupId();
//            }
        }
        // 注册域名处理: 上送参数不带有https//这类，或者要与配置在表里的一致
        if (StringUtils.isNotEmpty(userDto.getMainDomain()) && isNull(userDto.getCodeId())) {
            if ("www.".equals(userDto.getMainDomain().substring(0, 4).toLowerCase())) {
                userDto.setMainDomain(userDto.getMainDomain().substring(4, userDto.getMainDomain().length()));
            }
            if ("m.".equals(userDto.getMainDomain().substring(0, 2).toLowerCase())) {
                userDto.setMainDomain(userDto.getMainDomain().substring(2, userDto.getMainDomain().length()));
            }
            AgyDomain agyDomain = new AgyDomain();
            agyDomain.setDomainUrl(userDto.getMainDomain());
            agyDomain.setStatus(1);
            List<AgyDomain> domainList = agyDomainMapper.select(agyDomain);
            // 处理代理和会员组
            if (Collections3.isNotEmpty(domainList)) {
                AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(domainList.get(0).getAccountId());
                if (Objects.nonNull(agentAccount)) {
                    if (nonNull(agentAccount.getSuperiorCloneId()) && !Constants.SITECODE_GOC.equalsIgnoreCase(CommonUtil.getSiteCode()) &&
                            nonNull(agentAccount.getAttributes()) && agentAccount.getAttributes() == 1) {
                        mbrAccount.setCagencyId(agentAccount.getSuperiorCloneId());
                        mbrAccount.setSubCagencyId(agentAccount.getId());

                        AgentAccount agentAccount2 = agentAccountMapper.selectByPrimaryKey(agentAccount.getSuperiorCloneId());
                        AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount2);
                        mbrAccount.setTagencyId(tAgent.getId());
                    } else {
                        mbrAccount.setCagencyId(agentAccount.getId());
                        AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount);
                        mbrAccount.setTagencyId(tAgent.getId());
                        // 如果代理的下级默认会员组不为空
                        if (agentAccount.getDefaultGroupId() != null) {
                        	groupId = agentAccount.getDefaultGroupId();
                        } else {
                        	groupId = agentAccount.getGroupId();
                        }
                        if (agentAccount.getAttributes() == 1) {
                            mbrAccount.setSubCagencyId(agentAccount.getId());
                        }
                    }
                }
            }
        }
        // 如果会员组状态正常，设置会员组
        if (nonNull(groupId)) {
            MbrGroup mbrGroup = mbrGroupMapper.selectByPrimaryKey(groupId);
            if (nonNull(mbrGroup) && new Byte("1").equals(mbrGroup.getAvailable())) {
                mbrAccount.setGroupId(mbrGroup.getId());
            }
        }
        mbrAccount.setIsOnline(Available.enable);
        String salt = RandomStringUtils.randomAlphanumeric(20);
        mbrAccount.setLoginPwd(new Sha256Hash(mbrAccount.getLoginPwd(), salt).toHex());
        mbrAccount.setSalt(salt);
        String currentDate = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
        mbrAccount.setRegisterTime(currentDate);
        mbrAccount.setModifyTime(currentDate);
        mbrAccount.setLoginTime(currentDate);
        mbrAccount.setDomainCode(getDomainCode());
        // 昵称默认使用loginname
        mbrAccount.setNickName(mbrAccount.getLoginName());
        mbrAccount.setLabelid(Constants.EVNumber.one);
        mbrAccount.setDepositLock(Constants.EVNumber.zero); // 存款锁定状态
        super.save(mbrAccount);
        // 处理钱包
        mbrWalletMapper.insert(getMbrWallet(mbrAccount));
        // 处理会员上级，插入会员树
        setMbrNode(mbrAccount, userDto.getCodeId(), Boolean.TRUE);
        if (StringUtil.isNotEmpty(mbrAccount.getMobile())) {
            // 保存会员手机号注册历史
            accountMobileService.addMbrAccountMobile(mbrAccount, null);
        }
        return mbrAccount;
    }

    public void checkoutUsername(String loginName) {
        int count = findAccountOrAgentByName(loginName);
        if (count > 0) {
            throw new R200Exception("用户已经存在");
        }
    }

    public MbrAccount mtRegister(String cpNumber, AgentAccount agentAccount, String ip){

        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setGroupId(apiConfig.getRegDeaultGroup());           // 默认会员组


        Integer groupId = null;
        // 如果代理的下级默认会员组不为空
        if (agentAccount.getDefaultGroupId() != null) {
            groupId = agentAccount.getDefaultGroupId();
        } else {
            groupId = agentAccount.getGroupId();
        }

        // 如果会员组状态正常，设置会员组
        if (nonNull(groupId)) {
            MbrGroup mbrGroup = mbrGroupMapper.selectByPrimaryKey(groupId);
            if (nonNull(mbrGroup) && new Byte("1").equals(mbrGroup.getAvailable())) {
                mbrAccount.setGroupId(mbrGroup.getId());
            }
        }


        mbrAccount.setMobile(cpNumber);
        mbrAccount.setCagencyId(agentAccount.getId());
        mbrAccount.setTagencyId(agentAccount.getId());
        mbrAccount.setIsOnline(Available.enable);
        mbrAccount.setRegisterIp(ip);
        String salt = RandomStringUtils.randomAlphanumeric(20);


        mbrAccount.setIsAllowEmail(Available.enable);
        mbrAccount.setIsAllowMsg(Available.enable);
        mbrAccount.setAvailable(Available.enable);
        mbrAccount.setIsVerifyEmail(Available.disable);
        mbrAccount.setIsVerifyMoblie(Available.enable);

        mbrAccount.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccount.setRegisterTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccount.setIsLock(Available.disable);
        mbrAccount.setActLevelId(Constants.EVNumber.one);
        mbrAccount.setIsActivityLock(Constants.EVNumber.zero);
        mbrAccount.setRebateRatio(new BigDecimal(Constants.EVNumber.zero)); // 三公返利默认设置为0
        mbrAccount.setAgyflag(Constants.EVNumber.zero);                     // 全民代理标志 0非代理会员

        String loginName = "mt"+ RandomStringUtils.randomAlphanumeric(6);

        while(getAccountNum(loginName) > 0){
            loginName = "mt"+ RandomStringUtils.randomAlphanumeric(6);
        }
        mbrAccount.setLoginName( loginName);
        mbrAccount.setLoginPwd(RandomStringUtils.randomAlphanumeric(8));
        mbrAccount.setLoginPwd(new Sha256Hash(mbrAccount.getLoginPwd(), salt).toHex());
        mbrAccount.setSalt(salt);
        String currentDate = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
        mbrAccount.setRegisterTime(currentDate);
        mbrAccount.setModifyTime(currentDate);
        mbrAccount.setLoginTime(currentDate);
        mbrAccount.setDomainCode(getDomainCode());
        // 昵称默认使用loginname
        mbrAccount.setNickName(mbrAccount.getLoginName());
        mbrAccount.setLabelid(Constants.EVNumber.one);
        mbrAccount.setDepositLock(Constants.EVNumber.zero); // 存款锁定状态
        super.save(mbrAccount);
        // 处理钱包
        mbrWalletMapper.insert(getMbrWallet(mbrAccount));
        // 处理会员上级，插入会员树
        if (StringUtil.isNotEmpty(mbrAccount.getMobile())) {
            // 保存会员手机号注册历史
            accountMobileService.addMbrAccountMobile(mbrAccount, null);
        }

        return  mbrAccount;
    }

    public void checkoutEgSanGong(MbrAccount childAccount) {
        // 站点存在EG三公模式则校验
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.EG_SANGONG_FLG);
        if (Objects.nonNull(setting) && Objects.nonNull(setting.getSysvalue()) && String.valueOf(Constants.EVNumber.one).equals(setting.getSysvalue())) {
            // 校验返利比例
            if (nonNull(childAccount.getCodeId())) {    // 前端好友新增好友时，设置codeId为父节点好友的id
                MbrAccount parentAccount = mbrAccountMapper.selectByPrimaryKey(childAccount.getCodeId());
                // 判空校验
                if (Objects.isNull(parentAccount.getRebateRatio())) {
                    parentAccount.setRebateRatio(new BigDecimal(Constants.EVNumber.zero));
                }

                // 反水比例格式校验
                Assert.isPercent(childAccount.getRebateRatio(), "请输入正确的反水比例");
                if (childAccount.getRebateRatio().compareTo(parentAccount.getRebateRatio()) >= Constants.EVNumber.zero
                        && childAccount.getRebateRatio().compareTo((new BigDecimal(Constants.EVNumber.zero))) > Constants.EVNumber.zero) {
                    // 下级返利比例不得大于上级同时排出两者都为0的情况
                    if (!(parentAccount.getRebateRatio().equals(new BigDecimal(Constants.EVNumber.zero))
                            && childAccount.getRebateRatio().equals(new BigDecimal(Constants.EVNumber.zero)))) {
                        throw new R200Exception("会员三公比例不能高于或等于上级会员！");
                    }
                }
            }
        }
    }


    public void setMbrNode(MbrAccount mbrAccount, Integer codeId, Boolean flag) {
        mbrMapper.addMbrNode(Constants.EVNumber.zero, mbrAccount.getId());
        if (nonNull(codeId)) {
            MbrAccount account = null;
            if (flag) {
                MbrAccount account1 = new MbrAccount();
                account1.setDomainCode(String.valueOf(codeId));
                account = mbrAccountMapper.selectOne(account1);
            } else {
                account = mbrAccountMapper.selectByPrimaryKey(codeId);
            }
            if (nonNull(account)) {
                mbrMapper.addMbrNode(account.getId(), mbrAccount.getId());
            }
        }
    }

    private void setWebMbrAccount(MbrAccount mbrAccount, UserDto userDto) {
        mbrAccount.setId(null);
        mbrAccount.setTagencyId(apiConfig.getRegDeaultTagencyId());     // 默认总代
        mbrAccount.setCagencyId(apiConfig.getRegDeaultCagencyId());     // 默认上级代理
        mbrAccount.setGroupId(apiConfig.getRegDeaultGroup());           // 默认会员组
        mbrAccount.setIsAllowEmail(Available.enable);
        mbrAccount.setIsAllowMsg(Available.enable);
        mbrAccount.setAvailable(Available.enable);
        mbrAccount.setIsVerifyEmail(Available.disable);
        mbrAccount.setPromoteType(userDto.getPromoteType());

        // 注册时若上送手机号，则当做校验过该手机号，并绑定
        mbrAccount.setIsVerifyMoblie(Available.disable);
        if (!StringUtils.isEmpty(userDto.getMobile())) {
            mbrAccount.setMobile(userDto.getMobile());
            mbrAccount.setIsVerifyMoblie(Available.enable);
        }
        mbrAccount.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccount.setRegisterTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccount.setIsLock(Available.disable);
        mbrAccount.setSpreadCode(userDto.getAgentId());
        mbrAccount.setActLevelId(Constants.EVNumber.one);
        mbrAccount.setIsActivityLock(Constants.EVNumber.zero);
        mbrAccount.setRebateRatio(new BigDecimal(Constants.EVNumber.zero)); // 三公返利默认设置为0
        mbrAccount.setAgyflag(Constants.EVNumber.zero);                     // 全民代理标志 0非代理会员
    }

    public void asyncLogoInfo(MbrAccount mbrAccount, String siteCode, Boolean isInsert, Boolean isMobile, Boolean isMobileCaptchareg) {
        CompletableFuture future = CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            // 获取ip的风控等级
            String checkip = checkIpUtils.getCheckIp(mbrAccount.getLoginIp(), siteCode);
            mbrAccount.setCheckip(checkip);
            if (Boolean.TRUE.equals(isInsert)) {
                logMbrregisterMapper.insert(getlogRegister(mbrAccount, mbrAccount.getLoginSource()));
            }
            //设置之前未有登出的会员登出时间
            logMbrloginService.setLoginOffTime(mbrAccount.getLoginName());
            //保存登录日志时，判断是否异地登录，是则发短信通知，暂未上线
            //LogMbrLogin lastLoginLog = logMbrloginService.findMemberLoginLastOne(mbrAccount);
            LogMbrLogin logMbrlogin = logMbrloginService.saveLoginLog(mbrAccount);
            /*if (!lastLoginLog.getLoginArea().substring(0, 4).equals(logMbrlogin.getLoginArea().substring(0, 4))) {
                sendSmsSevice.sendYidiLoginSms(mbrAccount.getMobile(), null, true, null, Constants.EVNumber.two);
            }*/
            setMemLineState(mbrAccount.getId(), Available.enable, isMobile, isMobileCaptchareg, mbrAccount.getLoginTime());
        });
        try {
            future.get();
        } catch (Exception e) {
            log.error("asyncLogoInfo==异常", e);
        }
    }

    public void setMemLineState(Integer userId, Byte lineState, Boolean isMobile, Boolean isMobileCaptchareg, String loginTime) {
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setIsOnline(lineState);
        if (isMobile.equals(Boolean.TRUE) && isMobileCaptchareg.equals(Boolean.TRUE)) {
            account.setIsVerifyMoblie(Available.enable);
        }
        if (Available.enable == lineState) {
            account.setLoginTime(loginTime);
        }
        update(account);
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void updateAccountRest(MbrAccount mbrAccount, BizEvent bizEvent, String userName, String ip) {
        MbrAccount account = getAccountInfo(mbrAccount.getId());
        if (account.getTagencyId().equals(apiConfig.getTestAgentId())
                && !mbrAccount.getTagencyId().equals(apiConfig.getTestAgentId())) {
            throw new R200Exception("测试组代理下的会员不能移到其它代理下!");
        }
        // 前端允许为空，需要做处理
        if (nonNull(mbrAccount.getAvailable()) && (!account.getAvailable().equals(mbrAccount.getAvailable()))) {
            if (account.getAvailable().compareTo(new Byte("0")) == 0 && mbrAccount.getAvailable().compareTo(new Byte("2")) == 0) {
                bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_REFUSE);
            }
            if (account.getAvailable().compareTo(new Byte("1")) == 0 && mbrAccount.getAvailable().compareTo(new Byte("2")) == 0) {
                bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_REFUSE);
            }
            if (account.getAvailable().compareTo(new Byte("0")) == 0 && mbrAccount.getAvailable().compareTo(new Byte("1")) == 0) {
                bizEvent.setEventType(BizEventType.MEMBER_ACCOUNT_START);
            }
        }
        // 插入日志
        accountLogService.updateAccountRest(account, mbrAccount, userName, ip);

        // 前端允许为空，需要做处理
        MbrAccount newAccount = new MbrAccount();
        newAccount.setId(mbrAccount.getId());
        if (nonNull(mbrAccount.getGroupId())) {
            newAccount.setGroupId(mbrAccount.getGroupId());
        }
        if (nonNull(mbrAccount.getAvailable())) {
            newAccount.setAvailable(mbrAccount.getAvailable());
        }
        if (nonNull(mbrAccount.getActLevelId())) {
            newAccount.setActLevelId(mbrAccount.getActLevelId());
        }
        if (nonNull(mbrAccount.getLabelid())) {
        	newAccount.setLabelid(mbrAccount.getLabelid());
        }

        accountMapper.updateByPrimaryKeySelective(newAccount);
        if (Objects.nonNull(mbrAccount.getAvailable()) && (mbrAccount.getAvailable() == Constants.EVNumber.zero)) {
            kickLine(mbrAccount.getId());
        }
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void updateDepositLockStatus(MbrAccount mbrAccount) {
        MbrAccount newAccount = new MbrAccount();
        newAccount.setId(mbrAccount.getId());

        // 修改会员存款锁定状态
        if (nonNull(mbrAccount.getDepositLock())) {
            if (Integer.valueOf(Constants.EVNumber.zero).equals(mbrAccount.getDepositLock())) {   // 解锁
                // 锁定次数重置
                resetDepositLockNum(mbrAccount.getId());
            }
            // 修改会员存款锁定状态
            newAccount.setDepositLock(mbrAccount.getDepositLock());
        }

        accountMapper.updateByPrimaryKeySelective(newAccount);

        // 插入日志
        MbrAccount account = getAccountInfo(mbrAccount.getId());
        mbrAccount.setLoginName(account.getLoginName());
        accountLogService.updateAccountRestDepositLock(mbrAccount);

    }

    /**
     * 	登录接口返回值
     * 
     * @param loginName
     * @return
     */
    public LinkedHashMap<String, Object> webUserInfo(String loginName) {
        LinkedHashMap<String, Object> userInfo = new LinkedHashMap<String, Object>(64);
        MbrWallet wallet = new MbrWallet();
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setLoginName(loginName);
        wallet.setLoginName(loginName);
        wallet = mbrWalletMapper.selectOne(wallet);
        mbrAccount = queryObjectCond(mbrAccount);

        Byte userInfoMeasure = getUserInfoMeasure(mbrAccount);
        userInfo.put("gender", mbrAccount.getGender());
        userInfo.put("birthday", mbrAccount.getBirthday());
        userInfo.put("userId", mbrAccount.getId());
        userInfo.put("loginName", mbrAccount.getLoginName());
        userInfo.put("nickName", mbrAccount.getNickName());     // 昵称
        userInfo.put("depositLock", mbrAccount.getDepositLock());     // 存款锁定状态  0正常 1冻结
        userInfo.put("realName", mbrAccount.getRealName());
        userInfo.put("registerTime", mbrAccount.getRegisterTime());
        userInfo.put("balance", wallet.getBalance());
        userInfo.put("available", mbrAccount.getAvailable());
        userInfo.put("mobile", mbrAccount.getMobile());
        userInfo.put("email", mbrAccount.getEmail());
        userInfo.put("weChat", mbrAccount.getWeChat());
        userInfo.put("loginPwd", mbrAccount.getLoginPwd());
        userInfo.put("securePwd", mbrAccount.getSecurePwd());
        userInfo.put("qq", mbrAccount.getQq());
        userInfo.put("userInfoMeasure", userInfoMeasure);
        userInfo.put("groupName", "");
        MbrActivityLevel mbrActivityLevel = mbrActivityLevelMapper.selectByPrimaryKey(mbrAccount.getActLevelId());
        userInfo.put("mbrLevel", mbrActivityLevel.getAccountLevel());
        userInfo.put("levelName", mbrActivityLevel.getTierName());

        userInfo.put("freeWalletSwitch", mbrAccount.getFreeWalletSwitch()== null ? Constants.EVNumber.one: mbrAccount.getFreeWalletSwitch());

        // 新增返回自助申请状态
        Integer flag = isAutoWater(mbrAccount);
        if (flag == Constants.EVNumber.zero) {
            userInfo.put("settlementSwitch", Constants.EVNumber.zero);
            userInfo.put("settlementBtnShow", Constants.EVNumber.zero);
        }
        if (flag == Constants.EVNumber.one) {
            userInfo.put("settlementSwitch", Constants.EVNumber.one);
            userInfo.put("settlementBtnShow", Constants.EVNumber.zero);
        }
        if (flag == Constants.EVNumber.two) {
            userInfo.put("settlementSwitch", Constants.EVNumber.one);
            userInfo.put("settlementBtnShow", Constants.EVNumber.one);
        }
        // 增加返回是否显示EG三公标志  egSanGongFlg  0不显示  1显示
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.EG_SANGONG_FLG);
        if (Objects.nonNull(setting) && Objects.nonNull(setting.getSysvalue()) && String.valueOf(Constants.EVNumber.one).equals(setting.getSysvalue())) {
            // 站点配置三公的基础上，只允许总代会员，一级会员，显示三公，即深度<=1
            Integer maxDepth = promotionService.getMbrTreeDepth(mbrAccount);
            if (maxDepth.compareTo(Constants.EVNumber.one) <= 0) {
                userInfo.put("egSanGongFlg", Constants.EVNumber.one);
            } else {
                userInfo.put("egSanGongFlg", Constants.EVNumber.zero);
            }
            // 推广链接显示与否,只允许总代会员，一级会员，二级会员显示三公，即深度<=2
            if (maxDepth.compareTo(Constants.EVNumber.two) <= 0) {
                userInfo.put("egSanGongPromotionUrl", Constants.EVNumber.one);
            } else {
                userInfo.put("egSanGongPromotionUrl", Constants.EVNumber.zero);
            }

            userInfo.put("egSanGong", Constants.EVNumber.one);
        } else {
            userInfo.put("egSanGongFlg", Constants.EVNumber.zero);
            userInfo.put("egSanGong", Constants.EVNumber.zero);
            userInfo.put("egSanGongPromotionUrl", Constants.EVNumber.one);  // 没有配置三公，默认都显示
        }
        getPaySetting(userInfo);
        // 获取会员取款稽核
        MbrWithdrawalCond withdrawal = new MbrWithdrawalCond();
        withdrawal.setGroupId(mbrAccount.getGroupId());
        withdrawal = withdrawalCondMapper.selectOne(withdrawal);
        if (Objects.nonNull(withdrawal)) {
            userInfo.put("withDrawalAudit", withdrawal.getWithDrawalAudit());
        }
//        // 获取当前用户的邀请码
//        AgentAccount agentAccount = new AgentAccount();
//        agentAccount.setAgyAccount(loginName);
//        List<AgentAccount> agentAccountList = agentAccountMapper.select(agentAccount);
//        if (CollectionUtils.isNotEmpty(agentAccountList)) {
//        	userInfo.put("spreadCode", agentAccountList.get(0).getSpreadCode());
//        }
        
        //登录成功删除图形校验判断的redis key
        String keyLogin = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + loginName;
        redisService.del(keyLogin);
        return userInfo;
    }

    private void getPaySetting(LinkedHashMap<String, Object> userInfo) {
        List<String> keys = Lists.newArrayList(SystemConstants.DEPOSIT_CONDITION, SystemConstants.WITHDRAW_CONDITION);
        List<SysSetting> sysSettingList = sysSettingService.getSysSettingList(keys);
        sysSettingList.stream().forEach(sysSetting -> {
            Type jsonType = new TypeToken<List<Integer>>() {
            }.getType();
            List<Integer> con = jsonUtil.fromJson(sysSetting.getSysvalue(), jsonType);
            userInfo.put(sysSetting.getSyskey(), con);
        });
    }

    /**
     * 获取洗码/申请按钮 状态
     *
     * @param mbrAccount
     * @return 0 洗码状态关闭；1 洗码状态开启，申请状态关闭  2 洗码状态开启，申请状态开启
     */
    private Integer isAutoWater(MbrAccount mbrAccount) {

        if (oprActActivityService.isBlackList(mbrAccount.getId(), waterRebatesCode)) {
            return Constants.EVNumber.zero;
        }
        List<OprActRule> rules = activityMapper.findRuleRate();
        if (Collections3.isEmpty(rules) || isNull(rules.get(0).getIsSelfHelp())
                || rules.get(0).getIsSelfHelp() == Constants.EVNumber.zero) {
            return Constants.EVNumber.zero;
        }
        // 新增返回自助申请状态
        if (isNull(rules.get(0).getIsSelfHelpShow())
                || rules.get(0).getIsSelfHelpShow() != Constants.EVNumber.one) {
            return Constants.EVNumber.one;
        }
        return Constants.EVNumber.two;
    }

    public LinkedHashMap<String, Object> webFindPwdUserInfo(String loginName) {
        LinkedHashMap<String, Object> userInfo = new LinkedHashMap<String, Object>();
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setLoginName(loginName);
        mbrAccount = queryObjectCond(mbrAccount);
        userInfo.put("loginName", mbrAccount.getLoginName());
        userInfo.put("email", StringUtil.mail(mbrAccount.getEmail()));
        userInfo.put("EmailValidateStatus", mbrAccount.getIsVerifyEmail());
        userInfo.put("phone", StringUtil.phone(mbrAccount.getMobile()));
        userInfo.put("PhoneValidateStatus", mbrAccount.getIsVerifyMoblie());
        return userInfo;
    }

    public void updateActivityLevel(MbrAccount mbrAccount) {
        MbrAccount mbrAccountOld = accountMapper.selectByPrimaryKey(mbrAccount.getId());
        mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
        mbrAccountLogService.updateActivityLevel(mbrAccount, mbrAccountOld);
    }

    public void kickLine(Integer accountId) {
        String siteCode = CommonUtil.getSiteCode();
        MbrAccount mbrAccount = getAccountInfo(accountId);
        apiUserService.rmLoginTokenCache(siteCode, mbrAccount.getLoginName());
        updateOffline(mbrAccount.getLoginName());
    }

    public MbrAccount viewAccount(Integer roleId, Long userId, Integer id, String siteCode) {
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        if (Collections3.isEmpty(menuList)) {
            throw new AuthorizationException("没有权限，请联系管理员授权");
        }

        MbrAccount paramAccount = new MbrAccount();
        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            if (null != columnAuthTreeDto.getColumnName() && !"".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add(columnAuthTreeDto.getColumnName());
            }
        }
        columnSets.add("id");
        columnSets.add("domainCode");
        columnSets.add("depositLock");      // 存款锁定状态
        paramAccount.setColumnSets(columnSets);
        paramAccount.setId(id);

        MbrAccount mbrAccount = mbrMapper.viewAccount(paramAccount);
        String perms = mbrMapper.findAccountContact(userId, Constants.ACCOUNT_CONTACT);
        if (null != perms) {
            if (!perms.contains("email") && StringUtils.isNotEmpty(mbrAccount.getEmail())) {
                mbrAccount.setEmail(StringUtil.mail(mbrAccount.getEmail()));
            }
            if (!perms.contains("mobile") && StringUtils.isNotEmpty(mbrAccount.getMobile())) {
                mbrAccount.setMobile(StringUtil.phone(mbrAccount.getMobile()));
            }
            if (!perms.contains("qq") && StringUtils.isNotEmpty(mbrAccount.getQq())) {
                mbrAccount.setQq(StringUtil.QQ(mbrAccount.getQq()));
            }
            if (!perms.contains("wechat") && StringUtils.isNotEmpty(mbrAccount.getWeChat())) {
                mbrAccount.setWeChat(StringUtil.QQ(mbrAccount.getWeChat()));
            }
            if (!perms.contains("realname") && StringUtils.isNotEmpty(mbrAccount.getRealName())) {
                mbrAccount.setRealName(StringUtil.realName(mbrAccount.getRealName()));
            }
        } else {
            mbrAccount.setEmail(StringUtil.mail(mbrAccount.getEmail()));
            mbrAccount.setMobile(StringUtil.phone(mbrAccount.getMobile()));
            mbrAccount.setQq(StringUtil.QQ(mbrAccount.getQq()));
            mbrAccount.setWeChat(StringUtil.QQ(mbrAccount.getWeChat()));
            mbrAccount.setRealName(StringUtil.realName(mbrAccount.getRealName()));
        }

        if (isNull(mbrAccount.getDomainCode())) {
            MbrAccount account = new MbrAccount();
            account.setId(mbrAccount.getId());
            account.setDomainCode(getDomainCode());
            mbrAccountMapper.updateByPrimaryKeySelective(account);
            mbrAccount.setDomainCode(account.getDomainCode());
        }
//        mbrAccount.setPromotionUrl(promotionPCDomain(siteCode, mbrAccount.getDomainCode()));
//        mbrAccount.setPromotionH5Url(promotionH5Domain(siteCode, mbrAccount.getDomainCode()));
        // SDY不使用后台设置的推广域名，所以此处仅显示推广码
        mbrAccount.setPromotionUrl(mbrAccount.getDomainCode());
        mbrAccount.setPromotionH5Url(mbrAccount.getDomainCode());

        // 会员登录锁定状态处理
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(mbrAccount.getId());
        // 会员名登录错误计数器 key
        String userLogin = RedisConstants.REDIS_USER_LOGIN + account.getLoginName().toLowerCase() + "_" + siteCode;
        // 手机号登录错误计数器 key
        String mobileLogin = RedisConstants.REDIS_MOBILE_LOGIN + account.getMobile() + "_" + siteCode;

        // 登录错误次数 手机和会员名登录分开
        Integer userNum = 5;
        Integer mobileNum = 5;
        if (!redisService.booleanRedis(mobileLogin)) {  // 存在错误次数
            mobileNum = Integer.parseInt(redisService.getRedisValus(mobileLogin).toString());
        }

        if (!redisService.booleanRedis(userLogin)) {    // 存在错误次数
            userNum = Integer.parseInt(redisService.getRedisValus(userLogin).toString());
        }
        if (userNum > 0 && mobileNum > 0) {   // 未锁定
            mbrAccount.setLoginLock(Constants.EVNumber.zero);
        } else {  // 手机号或会员登录锁定,就是锁定
            mbrAccount.setLoginLock(Constants.EVNumber.one);
        }

        return mbrAccount;
    }

    /**
     * 	账户资料
     * 
     * @param roleId
     * @param userId
     * @param id
     * @return
     */
    public MbrAccount viewOtherAccount(Integer roleId, Long userId, Integer id) {
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_OTHER_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        if (Collections3.isEmpty(menuList)) {
            throw new AuthorizationException("没有权限，请联系管理员授权");
        }

        MbrAccount paramAccount = new MbrAccount();
        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            if ("actLevelId".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add("actLevelId");
                columnSets.add("tierName");
                columnSets.add("isActivityLock");
            } else if (null != columnAuthTreeDto.getColumnName() && !"".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add(columnAuthTreeDto.getColumnName());
            }
        }
        columnSets.add("id");
        paramAccount.setColumnSets(columnSets);
        paramAccount.setId(id);

        MbrAccount mbrAccount = mbrMapper.viewOtherAccount(paramAccount);
        String perms = mbrMapper.findAccountContact(userId, Constants.ACCOUNT_CONTACT);
        if (null != perms) {
            if (!perms.contains("email") && StringUtils.isNotEmpty(mbrAccount.getEmail())) {
                mbrAccount.setEmail(StringUtil.mail(mbrAccount.getEmail()));
            }
            if (!perms.contains("mobile") && StringUtils.isNotEmpty(mbrAccount.getMobile())) {
                mbrAccount.setMobile(StringUtil.phone(mbrAccount.getMobile()));
            }
            if (!perms.contains("qq") && StringUtils.isNotEmpty(mbrAccount.getQq())) {
                mbrAccount.setQq(StringUtil.QQ(mbrAccount.getQq()));
            }
            if (!perms.contains("wechat") && StringUtils.isNotEmpty(mbrAccount.getWeChat())) {
                mbrAccount.setWeChat(StringUtil.QQ(mbrAccount.getWeChat()));
            }
        } else {
            mbrAccount.setEmail(StringUtil.mail(mbrAccount.getEmail()));
            mbrAccount.setMobile(StringUtil.phone(mbrAccount.getMobile()));
            mbrAccount.setQq(StringUtil.QQ(mbrAccount.getQq()));
            mbrAccount.setWeChat(StringUtil.QQ(mbrAccount.getWeChat()));
        }
        return mbrAccount;
    }

    public int updateGroupBatch(Integer[] id, Integer groupId) {
        return mbrMapper.updateGroupBatch(id, groupId);
    }

    public MbrAccount updateAvailable(Integer id, Byte available, String userName, String ip) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setAvailable(available);
        accountLogService.updateAccountAvailable(id, mbrAccount, userName, ip);
        // add modifyTime
        mbrAccount.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        super.update(mbrAccount);
        if (available == Constants.EVNumber.zero) {
            kickLine(mbrAccount.getId());
        }
        return getAccountInfo(id);
    }

    @CachePut(cacheNames = ApiConstants.REDIS_USER_CACHE_KEY, key = "#siteCode+''+#id")
    public MbrAccount updateRealName(Integer id, String realName, String siteCode, String ip) {
        MbrAccount mbrAccount = getAccountInfo(id);
        if (StringUtils.isEmpty(mbrAccount.getRealName())) {
            MbrAccount mbrAccount1 = new MbrAccount();
            mbrAccount1.setId(id);
            mbrAccount1.setRealName(realName);
            // add modifyTime
            mbrAccount1.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
            super.update(mbrAccount1);
            mbrAccount.setRealName(realName);
        }
        return mbrAccount;
    }


    public void updatePwd(Integer id, PwdDto pwdDto) {
        MbrAccount mbrAccount = getAccountInfo(id);
        String lastPwdHash = new Sha256Hash(pwdDto.getLastPwd(), mbrAccount.getSalt()).toHex();
        if (!lastPwdHash.equals(mbrAccount.getLoginPwd())) {
            throw new R200Exception("旧密码不正确 请重新输入！");
        }
        String newPwdHash = new Sha256Hash(pwdDto.getPwd(), mbrAccount.getSalt()).toHex();
        if (newPwdHash.equals(mbrAccount.getLoginPwd())) {
            throw new R200Exception("新密码不能与旧密码一致 请重新输入！");
        }
        if (newPwdHash.equals(mbrAccount.getSecurePwd())) {
            throw new R200Exception("被修改的密码不能与提款密码相同!");
        }
        mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setLoginPwd(newPwdHash);
        // add modifyTime
        mbrAccount.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        super.update(mbrAccount);
    }

    public boolean verifyPwd(Integer id, PwdDto pwdDto) {
        MbrAccount mbrAccount = getAccountInfo(id);
        String lastPwdHash = new Sha256Hash(pwdDto.getLastPwd(), mbrAccount.getSalt()).toHex();
        if (!lastPwdHash.equals(mbrAccount.getLoginPwd())) {
            return false;
        } else {
            return true;
        }
    }

    public void updateScPwd(Integer id, PwdDto pwdDto) {
        MbrAccount mbrAccount = getAccountInfo(id);
        String lastPwdHash = new Sha256Hash(pwdDto.getLastPwd(), mbrAccount.getSalt()).toHex();
        if (!lastPwdHash.equals(mbrAccount.getSecurePwd())) {
            throw new R200Exception("旧密码不正确 请重新输入！");
        }
        String newPwdHash = new Sha256Hash(pwdDto.getPwd(), mbrAccount.getSalt()).toHex();
        if (newPwdHash.equals(mbrAccount.getSecurePwd())) {
            throw new R200Exception("新密码不能与旧密码一致 请重新输入!");
        }
        if (newPwdHash.equals(mbrAccount.getLoginPwd())) {
            throw new R200Exception("被修改的密码不能与登陆相同!");
        }
        mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setSecurePwd(newPwdHash);
        // add modifyTime
        mbrAccount.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        super.update(mbrAccount);
    }

    public boolean updateOrCheckScPwd(Integer id, String pwd) {
        MbrAccount mbrAccount = getAccountInfo(id);
        if (StringUtil.isEmpty(mbrAccount.getRealName())) {
            throw new R200Exception("请先设置真实姓名");
        }
        if (mbrAccount.getAvailable() == MbrAccount.Status.LOCKED) {
            throw new R200Exception("余额已冻结,不能申请取款");
        }
        return Boolean.TRUE;
    }

    public int updateMail(Integer id, String email, String ip) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setEmail(email);
        mbrAccount.setIsAllowEmail(Available.enable);
        mbrAccount.setIsVerifyEmail(Available.enable);
        accountLogService.updateApiAccountMail(mbrAccount, email, ip);
        mbrAccount.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        return super.update(mbrAccount);
    }

    public int updateMobile(Integer id, String mobile, String ip, String loginName) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setMobile(mobile);
        mbrAccount.setIsVerifyMoblie(Available.enable);
        mbrAccount.setIsAllowMsg(Available.enable);
        mbrAccount.setLoginName(loginName);
        mbrAccount.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        //插入记录表
        accountMobileService.addMbrAccountMobile(mbrAccount, loginName);
        return super.update(mbrAccount);
    }

    public MbrAccount queryObject(Integer id, String siteCode) {
        return super.queryObject(id);
    }

    public MbrWallet getMbrWallet(MbrAccount mbrAccount) {
        MbrWallet wallet = new MbrWallet();
        wallet.setLoginName(mbrAccount.getLoginName());
        wallet.setBalance(Constants.DEAULT_ZERO_VALUE);
        wallet.setHuPengBalance(Constants.DEAULT_ZERO_VALUE);
        wallet.setAccountId(mbrAccount.getId());
        return wallet;
    }

    private LogMbrRegister getlogRegister(MbrAccount mbrAccount, Byte source) {
        LogMbrRegister logRegister = new LogMbrRegister();
        logRegister.setRegisterIp(mbrAccount.getLoginIp());
        logRegister.setRegisterSource(source);
        logRegister.setRegisterUrl(mbrAccount.getRegisterUrl());
        logRegister.setLoginName(mbrAccount.getLoginName());
        logRegister.setAccountId(mbrAccount.getId());
        logRegister.setCheckip(mbrAccount.getCheckip()); //添加记录
        logRegister.setRegisterTime(getCurrentDate(FORMAT_18_DATE_TIME));
        logRegister.setRegArea(ipService.getIpArea(logRegister.getRegisterIp()));
        return logRegister;
    }

    public LinkedHashMap<String, Object> webUserVfyInfo(Integer userId) {
        LinkedHashMap<String, Object> userInfo = new LinkedHashMap<String, Object>();
        MbrAccount mbrAccount = getAccountInfo(userId);
        userInfo.put("loginPwdVfy", true);
        userInfo.put("scPwdVfy", !StringUtils.isEmpty(mbrAccount.getSecurePwd()));
        userInfo.put("emailVfy", mbrAccount.getIsVerifyEmail() == 1);
        userInfo.put("phoneVfy", mbrAccount.getIsVerifyMoblie() == 1);
        // 获取会员银行卡+钱包总数
        int sum = mbrCryptoCurrenciesService.qryBankAndWalletSumById(userId);
        userInfo.put("bankCardNo", sum);
        userInfo.put("email", StringUtil.mail(mbrAccount.getEmail()));
        userInfo.put("phone", StringUtil.phone(mbrAccount.getMobile()));
        return userInfo;
    }

    public void updateMbrAccount(MbrAccount account, String userName, String ip) {
        BizEvent realNameBizEvent = null;
        BizEvent mobileBizEvent = null;
        BizEvent emailBizEvent = null;
        if (nonNull(account.getRealName()) && !"".equals(account.getRealName())) {
            ValidRegUtils.validRealName(account, SysSetting.SysValueConst.require);
        }
        if (nonNull(account.getQq()) && !"".equals(account.getQq())) {
            ValidRegUtils.validQQ(account, SysSetting.SysValueConst.visible);
        }
        if (nonNull(account.getWeChat()) && !"".equals(account.getWeChat())) {
            ValidRegUtils.validWeChat(account, SysSetting.SysValueConst.visible);
        }
        MbrAccount oldAccount = getAccountInfo(account.getId());
        if (StringUtil.isNotEmpty(account.getRealName())) {
            ValidRegUtils.validRealName(account, SysSetting.SysValueConst.visible);
            if (StringUtil.isEmpty(oldAccount.getRealName()) || !account.getRealName().equals(oldAccount.getRealName())) {
                realNameBizEvent = new BizEvent(this, CommonUtil.getSiteCode(), account.getId(), BizEventType.MEMBER_MODIFY_REALNAME);
            }
        }

        if (StringUtil.isNotEmpty(account.getEmail())) {
            if (Boolean.FALSE.equals(account.getEmail().contains("*"))) {
                ValidRegUtils.validEmail(account, SysSetting.SysValueConst.visible);
                account.setIsVerifyEmail(Available.enable);
                account.setIsAllowEmail(Available.enable);
                MbrAccount accountTemp = new MbrAccount();
                accountTemp.setEmail(account.getEmail());
                List<MbrAccount> resultList = mbrAccountMapper.select(accountTemp);
                if (Collections3.isNotEmpty(resultList) && !resultList.get(0).getId().equals(account.getId())) {
                    throw new RRException("此邮箱已绑定，请更换邮箱!");
                }
                if (StringUtil.isEmpty(oldAccount.getEmail()) || !account.getEmail().equals(oldAccount.getEmail())) {
                    emailBizEvent = new BizEvent(this, CommonUtil.getSiteCode(), account.getId(), BizEventType.MEMBER_MODIFY_EMAIL);
                }
            } else {
                account.setEmail(null);
            }
        } else if ("".equals(account.getEmail())) {   // 置空逻辑， 前端置空会送""， 不是修改送null
            account.setIsVerifyEmail(Available.disable);
            account.setIsAllowEmail(Available.disable);
        }
        if (StringUtil.isNotEmpty(account.getMobile())) {
            if (Boolean.FALSE.equals(account.getMobile().contains("*"))) {
                if (StringUtil.isEmpty(oldAccount.getMobile()) || !account.getMobile().equals(oldAccount.getMobile())) {
                    account.setLoginName(oldAccount.getLoginName());//历史记录中填充用户名
                    mobileBizEvent = new BizEvent(this, CommonUtil.getSiteCode(), account.getId(), BizEventType.MEMBER_MODIFY_MOBILE);
                }
                ValidRegUtils.validPhone(account, SysSetting.SysValueConst.visible);

                if (!account.getMobile().equals(oldAccount.getMobile())) {
                    // 判断是否修改的手机号已被其他账号绑定
                    List<MbrAccount> mbrAccountList = getAccountInfoByMobile(account.getMobile(), null);
                    if (mbrAccountList.size() > 0) {
                        throw new R200Exception("修改的手机号已被绑定！");
                    }
                    //改成只要其他账户没绑定就能绑定2021-09-23例行发布
                    //accountMobileService.checkAccountMobile(account.getMobile());
                }
                account.setIsVerifyMoblie(Available.enable);
                account.setIsAllowMsg(Available.enable);
            } else {
                account.setMobile(null);
            }
        } else if ("".equals(account.getMobile())) {
            account.setIsVerifyMoblie(Available.disable);
            account.setIsAllowMsg(Available.disable);
            account.setMobile("");
            accountMobileService.addMbrAccountMobile(account, userName);
        }
        // 修改会员基本信息
        mbrAccountMapper.updateByPrimaryKeySelective(account);
        //super.update(account);

        // 添加会员信息修改日志
        accountLogService.updateAccountInfo(oldAccount, account, userName, ip);

        // 同步修改银行卡用户名
        if (null != account.getRealName() && "".equals(account.getRealName())) {
            mbrBankcardService.updateBankCardNameByAccId(account.getId(), account.getRealName());
        }

        if (Objects.nonNull(realNameBizEvent)) {
            applicationEventPublisher.publishEvent(realNameBizEvent);
        }
        if (Objects.nonNull(emailBizEvent)) {
            applicationEventPublisher.publishEvent(emailBizEvent);
        }
        if (Objects.nonNull(mobileBizEvent)) {
            accountMobileService.addMbrAccountMobile(account, userName);
            applicationEventPublisher.publishEvent(mobileBizEvent);
        }
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public int updateOffline(String loginName) {
        if (StringUtils.isNotEmpty(loginName)) {
            int resu = mbrMapper.updateOffline(loginName);
            if (resu > 0) {
                logLoginService.setLoginOffTime(loginName);
            }
            return resu;
        }
        return Constants.EVNumber.zero;
    }


    public int updateOfflineById(Integer accountId){
        return mbrMapper.updateOfflineById(accountId);
    }

    /**
     * 	会员详情，资产信息
     * 
     * @param accountId
     * @param roleId
     * @return
     */
    public MbrFundTotal findMbrTotal(Integer accountId, Integer roleId) {
        //查询用户具备得列权限功能
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_ASSET_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        if (Collections3.isEmpty(menuList)) {
            throw new AuthorizationException("没有权限，请联系管理员授权");
        }

        MbrAccount mbrAccount = new MbrAccount();
        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            if ("bet".equals(columnAuthTreeDto.getColumnName()) || "payout".equals(columnAuthTreeDto.getColumnName()) || "validBet".equals(columnAuthTreeDto.getColumnName())) {
                continue;
            }
            if (!StringUtil.isEmpty(columnAuthTreeDto.getColumnName())) {
                columnSets.add(columnAuthTreeDto.getColumnName());
            }
        }
        // 避免sql报错
        if (Collections3.isEmpty(columnSets)) {
            return null;
        }
        mbrAccount.setId(accountId);
        mbrAccount.setColumnSets(columnSets);
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setAccountId(accountId);
        MbrWallet selectOne = mbrWalletMapper.selectOne(mbrWallet);
        
        MbrFundTotal mbrFundsTotal = mbrMapper.mbrFundsTotal(mbrAccount);
        mbrFundsTotal.setAdjustment(selectOne.getAdjustment());
        return mbrFundsTotal;
    }

    public List<Map> queryAccountAuditList(Integer accountId) {
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(getUser().getRoleId(), ColumnAuthConstants.MEMBER_RISK_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        if (Collections3.isEmpty(menuList)) {
            throw new AuthorizationException("没有权限，请联系管理员授权");
        }

        MbrAccount mbrAccount = new MbrAccount();
        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            columnSets.add(columnAuthTreeDto.getColumnName());
        }
        mbrAccount.setId(accountId);
        mbrAccount.setColumnSets(columnSets);
        return mbrMapper.selectRiskControlAudit(mbrAccount);
    }

    public PageUtils queryAccountAuditInfo(Integer accountId, String keys, String item, Integer pageNo,
                                           Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<ItemDto> list = mbrMapper.queryAccountAuditInfo(accountId, keys, item);
        return BeanUtil.toPagedResult(list);
    }

    public List<Map> queryAccountBonusReporList(Integer accountId) {
        return mbrMapper.queryAccountBonusReporList(accountId);
    }

    public PageUtils bonusList(Integer accountId, Integer pageNo, Integer pageSize) {
        return oprActActivityService.findAccountBonusList(null, null, accountId, pageNo, pageSize, Constants.EVNumber.one,null);
    }

    public PageUtils withdrawList(Integer accountId, Integer pageNo, Integer pageSize) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setAccountId(accountId);
        return withdrawService.queryAccListPage(accWithdraw, pageNo, pageSize, Constants.EVNumber.zero);
    }

    public PageUtils depositList(Integer accountId, Integer pageNo, Integer pageSize) {
        FundDeposit deposit = new FundDeposit();
        deposit.setAccountId(accountId);
        return depositService.queryListPage(deposit, pageNo, pageSize, Constants.EVNumber.zero);
    }

    public PageUtils manageList(Integer accountId, Integer pageNo, Integer pageSize) {
        MbrBillManage mbrBillManage = new MbrBillManage();
        mbrBillManage.setAccountId(accountId);
        return reportService.queryListPage(mbrBillManage, pageNo, pageSize);
    }

    public PageUtils taskList(Integer accountId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TaskBonus> list = mbrMapper.taskList(accountId);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils fundList(Integer accountId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrBillDetail> list = mbrMapper.queryAccountFundList(accountId);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils accountLogList(Integer accountId, Integer pageNo, Integer pageSize, Long userId) {
        return accountLogService.accountLogList(accountId, pageNo, pageSize, userId);
    }

    public MbrAccount findAccountByName(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        account.setId(mbrAccount.getId());
        return account;
    }

    public MbrAccount findMbrLevelAndAgyInfoByName(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);

        if (mbrAccount == null) {
            return null;
        }
        MbrAccount mbrAccountInfo = mbrMapper.findMbrLevelAndAgyInfoById(mbrAccount.getId());
        return mbrAccountInfo;
    }

    public List<MbrAccount> findMbrLevelAndAgyInfoByLoginNames(List<String> loginNames) {
//        MbrAccount account = new MbrAccount();
//        account.setLoginName(loginName);
        List<MbrAccount> mbrAccounts = accountMapper.findMbrAccountByLoginNames(loginNames);

        if (CollectionUtils.isEmpty(mbrAccounts)) {
            return null;
        }
        List<Integer> ids = mbrAccounts.stream().map(ma -> ma.getId()).collect(Collectors.toList());
        List<MbrAccount> mbrAccountInfo = mbrMapper.findMbrLevelAndAgyInfoByIds(ids);
        return mbrAccountInfo;
    }

    public List<Map> findHomePageCount() {
        String startday = getCurrentDate(FORMAT_10_DATE);
        return mbrMapper.findHomePageCount(startday);
    }

    public List<Map> findHomePageCountEx(String startTime, String endTime) {
        return mbrMapper.findHomePageCountEx(startTime, endTime);
    }

    public void saveQQOrWeChat(MbrAccount mbrAccount) {
        accountMapper.updateByPrimaryKeySelective(mbrAccount);
    }

    //计算完整度 realName,mobile,email,weChat,securePwd,qq,bankCard
    private Byte getUserInfoMeasure(MbrAccount mbrAccount) {
        List<MbrBankcard> mbrBankcardsList = mbrBankcardService.listCondBankCard(mbrAccount.getId());

        byte userInfoMeasure = 0;
        //真实姓名
        if (StringUtil.isNotEmpty(mbrAccount.getRealName())) {
            userInfoMeasure++;
        }
        //手机号码
        if (StringUtil.isNotEmpty(mbrAccount.getMobile())) {
            userInfoMeasure++;
        }
        //邮箱
        if (StringUtil.isNotEmpty(mbrAccount.getEmail())) {
            userInfoMeasure++;
        }
        //微信号
        if (StringUtil.isNotEmpty(mbrAccount.getWeChat())) {
            userInfoMeasure++;
        }
        //securePwd
        if (StringUtil.isNotEmpty(mbrAccount.getSecurePwd())) {
            userInfoMeasure++;
        }
        //qq
        if (StringUtil.isNotEmpty(mbrAccount.getQq())) {
            userInfoMeasure++;
        }
        //bankCard
        if (mbrBankcardsList.size() != 0) {
            userInfoMeasure++;
        }
        if (userInfoMeasure == 0) {
            return Constants.userInfoMeasure.zero;
        }
        if (userInfoMeasure == 7) {
            return Constants.userInfoMeasure.full;
        }
        BigDecimal full = new BigDecimal(Constants.userInfoMeasure.full);
        Byte result = ((full.divide(new BigDecimal(Constants.userInfoMeasure.userInfoConut), 3, RoundingMode.HALF_UP).multiply(new BigDecimal(userInfoMeasure))).setScale(0, BigDecimal.ROUND_HALF_UP)).byteValue();
        return result;
    }

    public void setSecurePwdOfFirst(MbrAccount mbrAccount) {
        MbrAccount account = accountMapper.selectByPrimaryKey(mbrAccount.getId());
        if (StringUtil.isNotEmpty(account.getSecurePwd())) {
            throw new R200Exception("提款密码已设置，请通过修改方式进行修改");
        }
        String newPwd = mbrAccount.getSecurePwd();
        if (StringUtil.isEmpty(newPwd)) {
            throw new R200Exception("提款密码不能为空");
        }
        String newPwdHash = new Sha256Hash(newPwd, account.getSalt()).toHex();
        if (newPwdHash.equals(account.getLoginPwd())) {
            throw new R200Exception("提款密码不能与登陆密码相同!");
        }
        account.setSecurePwd(newPwdHash);
        accountMapper.updateByPrimaryKeySelective(account);
    }

    public String getDomainCode() {
        Boolean flag = Boolean.TRUE;
        MbrAccount tempAcc;
        while (flag) {
            long numbers = (long) (Math.random() * 9 * Math.pow(10, 8 - 1)) + (long) Math.pow(10, 8 - 1);
            tempAcc = new MbrAccount();
            tempAcc.setDomainCode(String.valueOf(numbers));
            List<MbrAccount> listAcc = accountMapper.select(tempAcc);
            if (Collections3.isNotEmpty(listAcc)) {
                continue;
            }
            return numbers + "";
        }
        return "";
    }


    public void accountQrCode(Integer accountId, HttpServletResponse response, String siteCode) {
        try {
            MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
            if (isNull(mbrAccount.getDomainCode())) {
                mbrAccount.setDomainCode(getDomainCode());
                mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
            }

            String content = promotionH5Domain(siteCode, mbrAccount.getDomainCode());
            if (nonNull(content)) {
                BufferedImage image = QrCodeCreateUtil.createQrCode(150, 150, content);
                response.setContentType("image/png");
                OutputStream os = response.getOutputStream();
                ImageIO.write(image, "png", os);
            }
        } catch (Exception e) {
            throw new RRException("生成二维码报错");
        }
    }

    public String promotionH5Domain(String siteCode, String domainCode) {
        String url = sysSettingService.getPromotionUrl(siteCode);
        if (nonNull(url)) {
            return url + registerH5 + domainCode;
        }
        return null;
    }

    public String promotionPCDomain(String siteCode, String domainCode) {
        String url = sysSettingService.getPromotionUrl(siteCode);
        if (isNull(url)) {
            return null;
        }
        return url + registerPC + domainCode;
    }

    public List<MbrAccount> recommendAccounts(Integer accountId) {
        return mbrMapper.findRecommendAccounts(accountId);
    }

    public Integer findAccountOrAgentByName(String loginName) {
        int accountCount = getAccountNum(loginName);
        if (accountCount > 0) {
            return Constants.EVNumber.one;
        }
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(loginName);
        int agentCount = agentAccountMapper.selectCount(agentAccount);
        if (agentCount > 0) {
            return Constants.EVNumber.two;
        }
        return Constants.EVNumber.zero;
    }

    // 0 查无该代理或会员 1会员 2 代理 3 总代
    public Integer findAccountOrAgentByNameEx(String loginName, String flag) {
        if (String.valueOf(Constants.EVNumber.one).equals(flag)) {    // 会员
            int accountCount = getAccountNum(loginName);
            if (accountCount > 0) {
                return Constants.EVNumber.one;
            }
        } else {
            AgentAccount agentAccount = new AgentAccount();
            agentAccount.setAgyAccount(loginName);
            List<AgentAccount> list = agentAccountMapper.select(agentAccount);
            if (Objects.nonNull(list) && list.size() > 0) {
                agentAccount = list.get(0);
                if (Integer.valueOf(Constants.EVNumber.zero).equals(agentAccount.getParentId())) {
                    return Constants.EVNumber.three;
                }
                return Constants.EVNumber.two;
            }

        }
        return Constants.EVNumber.zero;
    }

    public SysFileExportRecord exportMbrAccountInfo(MbrAccount mbrAccount, SysUserEntity user, String module, String mbrAccountExcelTempPath) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(user.getUserId(), module);
        if (null != record) {
            List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(user.getRoleId(), ColumnAuthConstants.MEMBER_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
            List<ColumnAuthTreeDto> fullMenuList = columnAuthProviderService.getRoleColumnAuth(user.getRoleId(), ColumnAuthConstants.MEMBER_FULL_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
            List<String> columnStrs = menuList.stream().map(ColumnAuthTreeDto::getColumnKey).collect(Collectors.toList());
            List<String> fullColumnStrs = fullMenuList.stream().map(ColumnAuthTreeDto::getColumnKey).collect(Collectors.toList());
            if (nonNull(mbrAccount) && StringUtil.isNotEmpty(mbrAccount.getMobile())) {
                mbrAccount.setMobileEncrypt(getMobileEncrypt(mbrAccount.getMobile()));
            }
//            List<Map<String, Object>> list = mbrMapper.findExportList(mbrAccount);
            List<MbrAccount> mbrlist = mbrMapper.findExportList(mbrAccount);
            if (CollectionUtils.isNotEmpty(mbrlist)) {
                Set<String> loginNames = mbrlist.stream().map(MbrAccount::getLoginName).collect(Collectors.toSet());
                // 会员的最后投注日（投注大于100的最近一天）
                List<MbrAccountLastBetDate> lastBetDateAccounts = mbrMapper.lastBetDate(loginNames);
                for (MbrAccount targetAccount : mbrlist) {
                    Optional<MbrAccountLastBetDate> findFirst = lastBetDateAccounts.stream()
                            .filter(t -> t.getLoginName().equals(targetAccount.getLoginName())).findFirst();
                    if (findFirst.isPresent()) {
                        targetAccount.setBetDate(findFirst.get().getBetDate());
                    }
                }
            }
            List<Map<String, Object>> list = mbrlist.stream().map(e -> {
                dealAndTransData(e);
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(columnStrs) || !columnStrs.contains("mobileKey")) {//手机号查看权限
                list.stream().forEach(map -> {
                    map.put("mobile", "*");
                });
            } else if (CollectionUtils.isEmpty(fullColumnStrs) || !fullColumnStrs.contains("fullRelationMobile")) {//手机号完整信息查看
                list.stream().forEach(map -> {
                    if (nonNull(map.get("mobile")) && StringUtil.isNotEmpty(map.get("mobile").toString())) {
                        String mobile = map.get("mobile").toString();
                        String mobile1 = mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                        map.put("mobile", mobile1);
                    }
                });
            }
            list.stream().forEach(map -> {
                if (nonNull(map.get("loginType")) && StringUtil.isNotEmpty(map.get("loginType").toString())) {
                    if (map.get("loginType").equals("0")) {
                        map.put("loginType", "PC");
                    }
                    if (map.get("loginType").equals("1")) {
                        map.put("loginType", "WAP");
                    }
                    if (map.get("loginType").equals("2")) {
                        map.put("loginType", "IOS");
                    }
                    if (map.get("loginType").equals("3")) {
                        map.put("loginType", "Android");
                    }

                }

            });
//            if (list.size() > 10000) {
//                throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
//            }
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(mbrAccountExcelTempPath, list, user.getUserId(), module, siteCode);
        }
        return record;
    }

    /**
     * 外拨电话
     *
     * @param accountId
     * @return
     */
    @Transactional
    public void dial(Integer accountId) {
        // 根据会员id查询会员mobile
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
        String mobile = mbrAccount.getMobile();
        String telExtNo = getUser().getTelExtNo();

        if (StringUtil.isEmpty(mobile)) {
            throw new R200Exception("会员电话号码为空！");
        }
        if (StringUtil.isEmpty(getUser().getTelExtNo())) {
            throw new R200Exception("用户绑定分机号为空！");
        }

        // 拼接拨打的电话: 手机号前面加一个1，然后再乘以22，再加上246135，
        String formatMobile = String.valueOf(Long.parseLong("1" + mobile) * 22 + 246135);
        // 回调用公共参数refId
        String refId = CommonUtil.getSiteCode() + String.valueOf(new SnowFlake().nextId());

        // 调用接口拨打电话
        String res = callCenterUrl + "?op=dialv2&ext_no=" + telExtNo + "&dia_num=" + formatMobile;
        log.info("外拨电话请求地址：" + res);
        String result;
        Map<String, String> stringMap = new HashMap<>(2);
        try {
            result = okHttpService.get(okHttpService.getPayHttpsClient(), res, stringMap);
            log.info("外拨电话返回信息【" + result + "】");
        } catch (Exception e) {
            log.error("外拨电话报错【" + e + "】");
            throw new RRException("外拨电话异常！请联系管理员！");
        }
        // 处理外拨结果
        if (StringUtil.isEmpty(result)) {
            throw new R200Exception("外拨电话：http请求异常，请联系管理员");
        }
        if ("-".equals(StringUtil.substring(result, 0, 1))) {
            throw new R200Exception(result);
        }
        if ("+".equals(StringUtil.substring(result, 0, 1))) {
            // 外拨成功后，插入外拨记录
            MbrAccountCallrecord mbrAccountCallrecord = new MbrAccountCallrecord();
            mbrAccountCallrecord.setRefId(refId);
            mbrAccountCallrecord.setAccountId(accountId);
            mbrAccountCallrecord.setLoginName(mbrAccount.getLoginName());
            mbrAccountCallrecord.setUserId(getUser().getUserId());
            mbrAccountCallrecord.setUserName(getUser().getUsername());
            mbrAccountCallrecord.setBeginTime(getCurrentDate(FORMAT_18_DATE_TIME));
            mbrAccountCallrecordMapper.insert(mbrAccountCallrecord);
        }
    }

    /**
     * 查询客户外拨电话记录
     *
     * @param accountId
     * @return
     */
    public R queryTelRecordByAccountId(Integer accountId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);

        MbrAccountCallrecord mbrAccountCallrecord = new MbrAccountCallrecord();
        mbrAccountCallrecord.setAccountId(accountId);
        List<MbrAccountCallrecord> mbrAccountCallrecords = mbrAccountCallrecordMapper.select(mbrAccountCallrecord);

        PageUtils page = BeanUtil.toPagedResult(mbrAccountCallrecords);
        return R.ok().put("data", page);
    }

    public List<MbrAccount> queryList(MbrAccount info) {
        List<MbrAccount> list = mbrMapper.findAllAccountList(info);
        // 1.获得所有depth=0即parentId=id的数据
        List<MbrAccount> zeroDepthList = new ArrayList<>();
        List<MbrAccount> oneDepthList = new ArrayList<>();
        list.stream().forEach(ls -> {
            if (ls.getDepth().equals(0)) {
                zeroDepthList.add(ls);
            }
            if (ls.getDepth().equals(1)) {
                oneDepthList.add(ls);
            }
        });

        // 2. 当深度为1时，设置parentId
        zeroDepthList.stream().forEach(ls -> {
            for (MbrAccount account : oneDepthList) {
                if (ls.getId().equals(account.getId())) {
                    ls.setParentId(account.getParentId());
                }
            }
        });

        return zeroDepthList;
    }

    // 更新修改时间
    public void updateModifyTime(MbrAccount mbrAccount) {
        super.update(mbrAccount);
    }

    @Transactional
    public int batchUpdateActLevel(BatchUpdateActLevelDto dto) {
        String siteCode = CommonUtil.getSiteCode();
        String key = RedisConstants.ACCOUNT_ACT_LEVEL_UPDATE_BATCH + siteCode + dto.getNewLevelId();
        key += CollectionUtils.isNotEmpty(dto.getOldLevelIds()) ? dto.getOldLevelIds() : 0;
        key += CollectionUtils.isNotEmpty(dto.getAccountIds()) ? dto.getAccountIds() : 0;
        int result = Constants.EVNumber.zero;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode + dto.getNewLevelId(), 8, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            // 获得新等级的等级名称、等级级别
            MbrActivityLevel level = mbrActivityLevelMapper.selectByPrimaryKey(dto.getNewLevelId());
            List<MbrAccount> oldMbrList = new ArrayList<>();

            // 按等级调级
            if (CollectionUtils.isNotEmpty(dto.getOldLevelIds()) && CollectionUtils.isEmpty(dto.getAccountIds())) {
                // 获得原等级会员数据
                oldMbrList = mbrMapper.findAccountByLevelIds(dto.getOldLevelIds());

                result = mbrMapper.batchUpdateMbrActLevel(dto.getNewLevelId(), dto.getOldLevelIds(), null);
            }
            // 按会员调级
            if (CollectionUtils.isNotEmpty(dto.getAccountIds()) && CollectionUtils.isEmpty(dto.getOldLevelIds())) {
                if (Objects.nonNull(dto.getForceUpdate())
                        && Integer.valueOf(Constants.EVNumber.one).equals(dto.getForceUpdate())) {
                    // 强制更新
                    // 获得原等级会员数据
                    oldMbrList = mbrMapper.findAccountByAccIdsContainLock(dto.getAccountIds());
                    result = mbrMapper.batchUpdateMbrActLevelContainLock(dto.getNewLevelId(), null, dto.getAccountIds());
                } else {
                    // 不更新锁定
                    // 获得原等级会员数据
                    oldMbrList = mbrMapper.findAccountByAccIds(dto.getAccountIds());
                    result = mbrMapper.batchUpdateMbrActLevel(dto.getNewLevelId(), null, dto.getAccountIds());
                }
            }
            // 增加操作日志
            accountLogService.batchUpdateActLevel(level, oldMbrList, dto.getMemo());
            return result;
        } else {
            throw new R200Exception("请不要重复操作！");
        }
    }

    public List<Map<String, Object>> getLevelNotLockMbrList() {
        return mbrMapper.findAccountByActLevelLock(Constants.EVNumber.zero);
    }

    // 修改会员返利比例
    public void modifyRebateRatio(MbrAccount mbrAccount) {
        // 校验返利比例
        promotionService.verifyRebateRatio(mbrAccount);
        // 更新返利比例
        mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
    }

    // 群发短信
    public void accountMassTexting(MbrAccount account, String siteCode) {
        int count = 0;                          // 计数器,群发最多一次100条
        StringBuffer to = new StringBuffer();   // 手机号码，以|分隔

        boolean logFlag = false;                        // 是否需要插入日志
        List<String> loginNames = Lists.newArrayList(); // 日志记录
        String contents = account.getContent();
        // 群发根据查询条件的会员
        List<MbrAccount> accountList = mbrMapper.findMbrAccountListForMassTexting(account);
        try {
            String content = URLEncoder.encode(account.getContent(), "utf8");
            account.setContent(content);
        } catch (Exception e) {
            throw new R200Exception("发送的内容存在特殊字符，请重新编辑！");
        }
        String phoneArea = "0086";  // 仅发送0086
        for (MbrAccount as : accountList) {
            if (nonNull(as) && as.getIsVerifyMoblie() == Available.enable) {
                loginNames.add(as.getLoginName());
                to.append(phoneArea + as.getMobile()).append("|");
                count++;
                if (count >= 100) {
                    sendSmsSevice.sendSmsMass(to.toString(), account.getContent(), siteCode);
                    logFlag = true;
                    // 清理
                    count = 0;
                    to.delete(0, to.length());
                }
            }
        }
        if (count != 0) {
            sendSmsSevice.sendSmsMass(to.toString(), account.getContent(), siteCode);
            logFlag = true;
        }
        // 插入日志
        if (logFlag) {
            accountLogService.accountMassTextingLog(loginNames, contents);
        }
    }

    // 群发短信统计
    public MbrAccount accountMassTextingCount(MbrAccount account) {
        return mbrMapper.accountMassTextingCount(account);
    }

    public void updateAccountAgent(Integer agentId, Integer accountId, String username, String ip, String supLoginName) {
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        if (nonNull(account)) {
            int count = mbrMapper.findAccoutnSubCount(account.getId());
            if (count > 0) {
                throw new R200Exception("有下级会员不可变更");
            }
            if (StringUtil.isNotEmpty(supLoginName)) {
                updateSupLoginName(account, supLoginName, username, ip);
                return;
            }
            Integer parentid = mbrMapper.findsubAccountParentid(account.getId());
            if (nonNull(parentid)) {
                throw new R200Exception("已经存在推荐人时，不可变更代理");
            }
            AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(agentId);
            if (agentAccount.getParentId() == Constants.EVNumber.zero) {
                throw new R200Exception("不可选择总代");
            }
            MbrAccount account1 = new MbrAccount();
            // 获取总代-无限层级
            AgentAccount tAgent = agentMapper.getTagentByCagent(agentAccount);
            account1.setTagencyId(tAgent.getId());
            account1.setCagencyId(agentAccount.getId());
            account1.setId(account.getId());
            if (Constants.SITECODE_GOC.equals(CommonUtil.getSiteCode()) && agentAccount.getAttributes() == Constants.EVNumber.one) {
                account1.setSubCagencyId(account1.getCagencyId());
            }
            accountMapper.updateByPrimaryKeySelective(account1);
            accountLogService.updateAccountAgent(account, agentAccount.getAgyAccount(), username, ip);
            if (agentAccount.getAttributes() != Constants.EVNumber.one) {
                mbrMapper.updateSubCagency(account.getId());
            }
        }
    }




    private void updateSupLoginName(MbrAccount account, String supLoginName, String username, String ip) {
        int count = analysisMapper.findAccountRptCount(account.getLoginName());
        Integer parentid = mbrMapper.findsubAccountParentid(account.getId());
        if (count > 0 && nonNull(parentid)) {
            throw new R200Exception("会员存在有效投注，且已经存在推荐人时，不可变更推荐人");
        }
        int num = mbrMapper.findPromotionCountByAccountId(account.getId());
        if (num > 0) {
            throw new R200Exception("会员存在下级好友时，不可变更推荐人");
        }
        MbrAccount supMbrAccount = new MbrAccount();
        supMbrAccount.setLoginName(supLoginName);
        MbrAccount subaccount = accountMapper.selectOne(supMbrAccount);
        if (isNull(subaccount)) {
            throw new R200Exception("推荐人不存在");
        }
        mbrMapper.deleteMbrTreeAccountId(account.getId());
        MbrAccount account1 = new MbrAccount();
        account1.setTagencyId(subaccount.getTagencyId());
        account1.setCagencyId(subaccount.getCagencyId());
        account1.setId(account.getId());
        accountMapper.updateByPrimaryKeySelective(account1);
        setMbrNode(account, subaccount.getId(), Boolean.FALSE);
        accountLogService.updateSupLoginName(account, supLoginName, username, ip, parentid);
    }

    /**
     * 0表示为空，1表示匹配成功，2表示匹配失败，3表示解绑
     *
     * @return
     */
    public MbrAccount checkUserInfo(MbrAccount account, String ip) {
        MbrAccount mbrAccount = getAccountInfo(account.getId());
        String cardNo = account.getCardNo();
        MbrAccount data = new MbrAccount();
        data.setRealName(checkInfo(account.getRealName(), mbrAccount.getRealName()));
        data.setMobile(checkInfo(account.getMobile(), mbrAccount.getMobile()));
        data.setWeChat(checkInfo(account.getWeChat(), mbrAccount.getWeChat()));
        data.setQq(checkInfo(account.getQq(), mbrAccount.getQq()));
        if (StringUtil.isNotEmpty(cardNo)) {
            MbrBankcard bankcard = new MbrBankcard();
            bankcard.setAccountId(account.getId());

            List<MbrBankcard> bankcards = mbrBankcardService.queryListCond(bankcard);
            if (Collections3.isEmpty(bankcards)) {
                data.setCardNo("0");
            } else {
                bankcard.setCardNo(cardNo);
                List<MbrBankcard> bankcards1 = mbrBankcardService.queryListCond(bankcard);
                if (Collections3.isEmpty(bankcards1)) {
                    data.setCardNo("2");
                } else if (Boolean.TRUE.equals(bankcards.get(0).getIsDel())) {
                    data.setCardNo("3");
                } else {
                    data.setCardNo("1");
                }
            }
        }
        mbrAccountLogService.checkUserInfo(account, mbrAccount.getLoginName(), getUser().getUsername(), ip);
        return data;
    }

    /**
     * @param src 源字符串
     * @param dec 目标字符串
     * @return
     */
    private String checkInfo(String src, String dec) {
        if (StringUtil.isNotEmpty(src)) {
            if (StringUtil.isEmpty(dec)) {
                return "0";
            } else if (src.equals(dec)) {
                return "1";
            } else {
                return "2";
            }
        }
        return null;
    }

    public MbrAccount getPromotionUrl(MbrAccount account, String siteCode) {
        // 根据会员id获得domainCode
        MbrAccount act = mbrAccountMapper.selectByPrimaryKey(account.getId());
        if (Objects.isNull(act)) {
            return account;
        }

        // 获得推广链接
        String domainCode = act.getDomainCode();
        String promotionPC = promotionPCDomain(siteCode, domainCode);
        String promotionH5 = promotionH5Domain(siteCode, domainCode);

        account.setPromotionUrl(promotionPC);
        account.setPromotionH5Url(promotionH5);
        return account;
    }

    /**
     * 会员详情-账户资料-修改登录锁定状态
     *
     * @param account
     * @param userName
     * @param ip
     */
    public void loginLockUpdate(MbrAccount account, String userName, String ip) {
        // 会员登录锁定状态处理
        MbrAccount mbr = mbrAccountMapper.selectByPrimaryKey(account.getId());
        // 会员名登录错误计数器 key
        String userLogin = RedisConstants.REDIS_USER_LOGIN + mbr.getLoginName().toLowerCase() + "_" + CommonUtil.getSiteCode();
        // 手机号登录错误计数器 key
        String mobileLogin = RedisConstants.REDIS_MOBILE_LOGIN + mbr.getMobile() + "_" + CommonUtil.getSiteCode();
        // 进行解锁
        if (Integer.valueOf(Constants.EVNumber.zero).equals(account.getLoginLock())) {
            // 清楚redis的key
            redisService.del(userLogin);
            redisService.del(mobileLogin);
        }
        // 进行加锁
        if (Integer.valueOf(Constants.EVNumber.one).equals(account.getLoginLock())) {
            redisService.setRedisExpiredTime(userLogin, 0, 900, TimeUnit.SECONDS);      // 会员名登录错误次数 设置为0
            redisService.setRedisExpiredTime(mobileLogin, 0, 900, TimeUnit.SECONDS);    // 会员名登录错误次数 设置为0
        }
        accountLogService.loginLockUpdate(account);
    }

    /**
     * 解锁会员的存款锁定
     *
     * @param accountId
     */
    public void unlockDepositLock(Integer accountId) {
        MbrAccount mbr = new MbrAccount();
        mbr.setId(accountId);
        mbr.setDepositLock(Constants.EVNumber.zero);
        mbrAccountMapper.updateByPrimaryKeySelective(mbr);
    }

    /**
     * 会员存款锁定次数重置
     *
     * @param accountId
     */
    public void resetDepositLockNum(Integer accountId) {
        MbrDepositCount count = new MbrDepositCount();
        count.setAccountId(accountId);
        count.setStartDay(DateUtil.getCurrentDate(FORMAT_10_DATE));
        mbrDepositCountMapper.resetDepositLockNum(count);
    }

    /**
     * 查询存款锁定状态
     *
     * @param accountId
     */
    public MbrDepositCount depositLockStatus(Integer accountId) {
        // 先检查存款锁定是否需要给会员解锁
        mbrDepositLockLogService.finishLockLog(new MbrAccount(){{setId(accountId);}});
        // 获得会员
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(accountId);
        // 校验存款计数
        MbrDepositCount count = payInfoService.checkDepositCount(account, Constants.EVNumber.one);
        return count;
    }

    private void dealAndTransData(MbrAccount dto) {
        // 1开启，0禁用,2余额冻结
        switch (dto.getAvailable()) {
            case 0:
                dto.setAvailableStr("禁用");
                break;
            case 1:
                dto.setAvailableStr("开启");
                break;
            case 2:
                dto.setAvailableStr("余额冻结");
                break;
            default:
                dto.setAvailableStr("");
        }
    }

    public PageUtils accountMobileList(Integer id, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy(orderBy);
        List<MbrAccountMobile> list = mbrMapper.accountMobileList(id);
        return BeanUtil.toPagedResult(list);
    }

    public Integer getIpAccNum(String ip) {
        Integer count = mbrMapper.getIpAccNum(ip);
        return count;
    }

    public Integer getDeviceAccNum(String dev) {
        //获取该设备号登录过的账号
        List<String> accountIds = mbrMapper.getAccountIdsByDevice(dev);
        if (!nonNull(accountIds) || accountIds.size() == 0) {
            return 0;
        }
        int tag = 0;
        //获取这些账号的首次登录设备(注册设备)
        List<MbrAccountDevice> mbrAccountDevices = mbrMapper.getDeviceByAccountIds(accountIds);
        for (MbrAccountDevice mbrdev : mbrAccountDevices) {
            if (mbrdev.getDeviceUuid().equals(dev)) {
                tag = tag + 1;
            }
        }
        return tag;
    }


    public List<MbrAccount> selectAccountIdsForGroupJob(MbrGroup thisGroup, MbrGroup nextGroup, Boolean queryRecent) {
        return mbrMapper.selectAccountIdsForGroupJob(thisGroup, nextGroup, queryRecent);
    }

    @Transactional
    public int updateManyGroupidForJob(List<Integer> ids, Integer groupId) {
        return mbrMapper.updateManyGroupidForJob(ids, groupId);
    }


    public Set<Integer> checkUserNames(String userNames) {
        if (StringUtils.isEmpty(userNames)) {
            return null;
        }
        List<String> usernameList = Splitter.on(",").trimResults().splitToList(userNames);
        List<MbrAccount> mbrListByLoginNames = mbrMapper.getMbrListByLoginNames(usernameList);
        Set<String> hasUserNameSet = mbrListByLoginNames.stream().map(MbrAccount::getLoginName)
                .collect(Collectors.toSet());
        // 差集
        SetView<String> difference = Sets.difference(new HashSet<String>(usernameList), hasUserNameSet);
        if (CollectionUtils.isNotEmpty(difference)) {
            StringBuffer sb = new StringBuffer("用户:");
            for (String userName : difference) {
                sb.append(userName + ",");
            }
            sb.append("不存在，请检查后重新输入");
            throw new R200Exception(sb.toString());
        }
        return mbrListByLoginNames.stream().map(MbrAccount::getId).collect(Collectors.toSet());
    }

    public int queryMbrDeviceNum(String loginName) {
        return mbrMapper.queryMbrDeviceNum(loginName);
    }

    public MbrUseDevice getDeviceByUuid(String loginName, String exptime, String uuid) {

        return mbrMapper.getDeviceByUuid(loginName, exptime, uuid, "3");
    }

    public void saveOrUpdateDevice(MbrAccountDevice device) {
        //检查会员是否有常用设备记录,
        int num = mbrMapper.queryMbrDeviceNum(device.getLoginName());
        if (num < 1) { //假如没有那么则判定次设备为用户第一个常用设备,新增常用设备记录
            MbrUseDevice useDevice = new MbrUseDevice();
            useDevice.setLoginName(device.getLoginName());
            useDevice.setDeviceUuid(device.getDeviceUuid());
            useDevice.setExptime(DateUtil.getNextMonthDD()); //时效一个月
            useDevice.setValiTimes(1); //1次为常用设备需要短信验证的次数
            mbrUseDeviceMapper.insert(useDevice);
        } else { //假如已经有常用设备
            //获取当前设备号在有效期的记录,只要在有效期就行,并不需已经校验3次
            MbrUseDevice dev = mbrMapper.getDeviceByUuid(device.getLoginName(), DateUtil.getLastMonthDD(), device.getDeviceUuid(), null);
            if (nonNull(dev)) {//假如近1个月有在这设备上登录
                if (dev.getValiTimes() < 1) { ///目前还不是常用设备,则验证次数加1
                    dev.setValiTimes(dev.getValiTimes() + 1);
                }
                dev.setExptime(DateUtil.getNextMonthDD());
                mbrUseDeviceMapper.updateByPrimaryKey(dev);
            } else { //假如近1个月没有在这设备上登录
                MbrUseDevice useDevice = new MbrUseDevice();
                useDevice.setLoginName(device.getLoginName());
                useDevice.setDeviceUuid(device.getDeviceUuid());
                useDevice.setExptime(DateUtil.getNextMonthDD()); //时效一个月
                useDevice.setValiTimes(1); //1表示重新验证,或者玩家已有常用设备,新增一台新的 开始第一次验证
                mbrUseDeviceMapper.insert(useDevice);
            }
        }
    }


    public int updateFreeWalletSwitch(Integer accountId, Integer status){
        return mbrMapper.updateFreeWalletSwitch(accountId, status);
    }

    public int getRealNameNum(String realName){
        return mbrMapper.getRealNameNum(realName);
    }
    public int getCodeNum(Integer codeId){
        return mbrMapper.getCodeNum(codeId);
    }

    public void updateCagencyIdByAccountId(Integer accountId, Integer cagencyId){
        mbrMapper.updateCagencyIdByAccountId( accountId,  cagencyId);
    }
}