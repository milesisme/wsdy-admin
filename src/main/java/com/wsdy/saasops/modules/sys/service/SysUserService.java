package com.wsdy.saasops.modules.sys.service;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.WarningConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.common.utils.google.GoogleAuthenticatorUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.analysis.dto.ExportRetentionRateDailyActiveDto;
import com.wsdy.saasops.modules.analysis.vo.RetentionRateDailyActiveReportVo;
import com.wsdy.saasops.modules.member.dao.MbrAccountLogMapper;
import com.wsdy.saasops.modules.member.dto.WarningLogDto;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrGroupService;
import com.wsdy.saasops.modules.sys.dao.SysUserAgyaccountrelationMapper;
import com.wsdy.saasops.modules.sys.dao.SysUserDao;
import com.wsdy.saasops.modules.sys.dao.SysUserMbrgrouprelationMapper;
import com.wsdy.saasops.modules.sys.dto.AuthenticatorDto;
import com.wsdy.saasops.modules.sys.dto.SysUserExportDto;
import com.wsdy.saasops.modules.sys.dto.SysUserLastLoginTimeDto;
import com.wsdy.saasops.modules.sys.entity.*;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity.ErrorCode;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Splitter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * 系统用户
 */
@Slf4j
@Service("sysUserService")
public class SysUserService {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserAgyaccountrelationService sysUserAgyaccountrelationService;
    @Autowired
    private SysUserMbrgrouprelationService sysUserMbrgrouprelationService;
    @Autowired
    private MbrGroupService mbrGroupService;
    @Autowired
    private AgentAccountService agentAccountService;
    @Autowired
    private SysUserMbrgrouprelationMapper sysUserMbrgrouprelationMapper;
    @Autowired
    private SysUserAgyaccountrelationMapper sysUserAgyaccountrelationMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private SysUserTokenService sysUserTokenService;
    @Resource(name = "stringRedisTemplate_1")
    private RedisTemplate redisTemplate;
    @Value("${redis.cache.sysUser}")
    private String sys_user;

    @Resource
    private MbrAccountLogMapper mbrAccountLogMapper;

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;


    public R userLogin(LoggingParam loggingParam, String url, Boolean isSign) {
        String username = loggingParam.getUsername();
        String password = loggingParam.getPassword();
        SysUserEntity user = checkoutUser(username, password);

        String lastIp = user.getIp();
        //操作日志
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = CommonUtil.getIpAddress(request);
        if (isSign == true) {
            boolean success = GoogleAuthenticatorUtils.verify(user.getAuthenticatorKey(), loggingParam.getCaptcha());
            if (success == false) {
                throw new R200Exception("身份验证码不正确");
            }
            sysUserDao.updateAuthenticatorLogin(Constants.EVNumber.one, user.getUserId());
        }
        sysUserDao.updateLoginIp(user.getUserId(), ip);
        // 用户角色判断
        List<Long> roleIds = sysUserRoleService.queryRoleIdList(user.getUserId());
        if (CollectionUtils.isNotEmpty(roleIds)) {
            SysRoleEntity roleEntity = sysRoleService.queryObject(roleIds.get(0));
            if (roleEntity != null && roleEntity.getIsEnable() == 0) {
                throw new R200Exception("账号未分配角色,请联系管理员");
            }
        } else {
            throw new R200Exception("账号未分配角色,请联系管理员");
        }
        user.setDomainUrl(CommonUtil.requestUrl(url));
        R r = sysUserTokenService.createToken(user.getUserId());
        // 增加返回userId
        r.put("id", user.getUserId());

        mbrAccountLogService.authenticatorLoginLog(loggingParam.getUsername(), CommonUtil.getIpAddress(request));

        if (StringUtil.isNotEmpty(lastIp) && !lastIp.equals(ip)) {
            String content = String.format(WarningConstants.LOGIN_WARNING_TMP, ip);
            mbrAccountLogService.addWarningLog(new WarningLogDto(loggingParam.getUsername(), content, Constants.EVNumber.one));
        }
        return r;
    }

    public SysUserEntity checkoutUser(String username, String password) {
        SysUserEntity user = queryByUserName(username);
        if (user == null || !user.getPassword().equals(new Sha256Hash(password, user.getSalt()).toHex())) {
            log.info("用户不存在" + username + "," + CommonUtil.getSiteCode() + ",ps" + password);
            throw new R200Exception("账号或密码不正确");
        }
        if (user.getStatus() == 0) {
            throw new R200Exception("账号已被锁定,请联系管理员");
        }
        return user;
    }

    public AuthenticatorDto getAuthenticator(SysUserEntity userEntity, String host, String authenticatorKey) {
        AuthenticatorDto authenticatorDto = new AuthenticatorDto();
        String secret = GoogleAuthenticatorUtils.createSecretKey();
        String key = StringUtils.isEmpty(authenticatorKey) ? secret : authenticatorKey;
        String googleAuthQRCodeData = GoogleAuthenticatorUtils.createGoogleAuthQRCodeData(key, userEntity.getUsername(), host);
        authenticatorDto.setSecret(key);
        authenticatorDto.setUrl(googleAuthQRCodeData);

        sysUserDao.updateAuthenticatorKey(key, userEntity.getUserId());
        return authenticatorDto;
    }

    protected SysUserEntity getUser() {
        return (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
    }

    public List<Long> queryAllMenuId(Long userId) {
        return sysUserDao.queryAllMenuId(userId);
    }

    public SysUserEntity queryByUserName(String username) {
        return sysUserDao.queryByUserName(username);
    }

    public SysUserEntity queryObject(Long userId) {
        return sysUserDao.queryObject(userId);
    }


    public SysUserEntity queryUserEntityOne(Long userId) {
        SysUserEntity userEntity = new SysUserEntity();
        SysUserEntity tempUser = sysUserDao.queryObject(userId);
        userEntity.setUserId(userId);
        List<SysUserEntity> sysUserEntities = sysUserDao.queryConditions(userEntity);
        SysUserEntity sysUserEntity = sysUserEntities != null ? sysUserEntities.get(0) : null;
        SysUserMbrgrouprelation mbrgrouprelation = new SysUserMbrgrouprelation();
        mbrgrouprelation.setUserId(sysUserEntity.getUserId());
        sysUserEntity.setMbrGroups(sysUserMbrgrouprelationMapper.select(mbrgrouprelation));
        SysUserAgyaccountrelation userAgyaccountrelation = new SysUserAgyaccountrelation();
        userAgyaccountrelation.setUserId(sysUserEntity.getUserId());
        sysUserEntity.setAgyAccounts(sysUserAgyaccountrelationMapper.select(userAgyaccountrelation));
        sysUserEntity.setSalt(tempUser.getSalt());
        return sysUserEntity;
    }

    public void setMbrAuthTotal(SysUserEntity user) {
        List<MbrGroup> groups = mbrGroupService.queryList();
        List<SysUserMbrgrouprelation> mbrGroups = user.getMbrGroups();
        mbrGroups.clear();
        groups.stream().forEach(group -> {
            SysUserMbrgrouprelation sysUserMbrgrouprelation = new SysUserMbrgrouprelation();
            sysUserMbrgrouprelation.setUserId(user.getUserId());
            sysUserMbrgrouprelation.setMbrGroupId(group.getId());
            mbrGroups.add(sysUserMbrgrouprelation);
        });
    }

    public void setAgyAuthTotal(SysUserEntity user) {
        List<AgentAccount> agentAccounts = agentAccountService.queryList();
        List<SysUserAgyaccountrelation> sysUserAgyaccountrelations = user.getAgyAccounts();
        sysUserAgyaccountrelations.clear();
        agentAccounts.stream().forEach(agentAccount -> {
            SysUserAgyaccountrelation sysUserAgyaccountrelation = new SysUserAgyaccountrelation();
            sysUserAgyaccountrelation.setAgyAccountId(agentAccount.getId());
            sysUserAgyaccountrelation.setDisabled(true);
            sysUserAgyaccountrelation.setAgyAccountType(Integer.valueOf(Constants.EVNumber.zero).equals(agentAccount.getParentId()) ? 0 : 1);
            sysUserAgyaccountrelations.add(sysUserAgyaccountrelation);
        });
    }

    public PageUtils queryList(SysUserEntity userEntity) {
        PageHelper.startPage(userEntity.getPageNo(), userEntity.getPageSize());
        if (!StringUtils.isEmpty(userEntity.getOrder())) {
            PageHelper.orderBy(userEntity.getOrder());
        }
        List<SysUserEntity> list = sysUserDao.queryList(userEntity);
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }

    @Transactional
    public void save(SysUserEntity user, String loginUserName, String ip) {
        user.setCreateTime(new Date());
        //sha256加密
        String salt = RandomStringUtils.randomAlphanumeric(20);
        user.setPassword(new Sha256Hash(user.getPassword(), salt).toHex());
        user.setSecurepwd(new Sha256Hash(user.getSecurepwd(), salt).toHex());
        user.setSalt(salt);

        //d 检查角色是否越权
        checkRole(user, loginUserName);

        // d 多个用户
        List<String> usernameList = Splitter.on(",").trimResults().splitToList(user.getUsername());
        for (String username : usernameList) {
            user.setUserId(null);
            user.setUsername(username.toLowerCase());
            sysUserDao.save(user);
            //d 添加操作日志
            mbrAccountLogService.addSysUserLog(user, loginUserName, ip);

            //d 保存用户与角色关系
            //SysUserEntity sue = sysUserDao.queryByUserName(user.getUsername());
            if (user.getRoleIdList() != null) {
                sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
            }
            //d 更新用户数据权限
            SysUserService sysUserService = context.getBean(SysUserService.class);
            sysUserService.saveDataAuth(user, CommonUtil.getSiteCode());
        }
    }

    @Transactional
    public int update(SysUserEntity user, String userName, String ip) {
        user.setPassword(null);
        user.setSecurepwd(null);
        sysUserDao.update(user);

        //添加操作日志
        mbrAccountLogService.updateSysUserInfoLog(user, userName, ip);

        // 检查角色是否越权
        checkRole(user, userName);
        // 保存用户与角色关系
        sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
        //更新用户数据权限
        SysUserService sysUserService = context.getBean(SysUserService.class);
        sysUserService.updateDataAuth(user, CommonUtil.getSiteCode());
        return 0;
    }

    /**
     * 删除缓存
     *
     * @param userId
     */
    @Transactional
    public void deleteCatchBatch(Long userId) {
        sysUserDao.deleteSysUser(userId);
        redisTemplate.delete(sys_user + CommonUtil.getSiteCode() + ":" + userId);
    }

    /**
     * 修改密码
     */
    public int updatePassword(Long userId, String password, String newPassword) {
        SysUserEntity dbUserEntity = sysUserDao.queryObject(userId);
        if (newPassword.equals(dbUserEntity.getPassword())) {
            return ErrorCode.code_01;
        }
        if (!StringUtils.isEmpty(dbUserEntity.getSecurepwd()) && newPassword.equals(dbUserEntity.getSecurepwd())) {
            return ErrorCode.code_02;
        }
        Map<String, Object> map = new HashMap<>(4);
        map.put("userId", userId);
        map.put("password", password);
        map.put("newPassword", newPassword);
        return sysUserDao.updatePassword(map);
    }

    /**
     * 修改安全密码
     */
    public int updateSecPassword(Long userId, String newPassword) {
        SysUserEntity dbUserEntity = sysUserDao.queryObject(userId);
        // 新安全码与原安全密码不能相同
        if (newPassword.equals(dbUserEntity.getSecurepwd())) {
            return ErrorCode.code_03;
        }
        // 新安全码不能和登录密码不能相同
        if (!StringUtils.isEmpty(dbUserEntity.getPassword()) && newPassword.equals(dbUserEntity.getPassword())) {
            return ErrorCode.code_04;
        }
        Map<String, Object> map = new HashMap<>(4);
        map.put("userId", userId);
        map.put("newPassword", newPassword);
        return sysUserDao.updateSecPassword(map);
    }

    public PageUtils queryConditions(SysUserEntity userEntity) {
        PageHelper.startPage(userEntity.getPageNo(), userEntity.getPageSize());
        if (!StringUtils.isEmpty(userEntity.getOrder())) {
            PageHelper.orderBy(userEntity.getOrder());
        }
        List<SysUserEntity> list = sysUserDao.queryConditions(userEntity);

        List<String> userNameList = list.stream().map(SysUserEntity::getUsername).collect(Collectors.toList());
        Map<String, Date> lastLoginMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userNameList)) {
            lastLoginMap = getUserLastLoginTime(userNameList);
        }

        Map<String, Date> finalLastLoginMap = lastLoginMap;
        list.stream().forEach(sysUserEntity -> {
            if (sysUserEntity.getUserAgyAccountAuth() == 1) {
                setAgyAuthTotal(sysUserEntity);
            }
            if (sysUserEntity.getUserMbrGroupAuth() == 1) {
                setMbrAuthTotal(sysUserEntity);
            }
            sysUserEntity.setLastLoginTime(finalLastLoginMap.get(sysUserEntity.getUsername()));
        });
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }

    /**
     * 获取用户最后的登录时间
     *
     * @param userNameList
     * @return
     */
    private Map<String, Date> getUserLastLoginTime(List<String> userNameList) {
        List<SysUserLastLoginTimeDto> lastLoginTimeDtoList = mbrAccountLogMapper.findUserLastLogTime(userNameList);

        Map<String, Date> lastLoginMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(lastLoginTimeDtoList)) {
            lastLoginMap = lastLoginTimeDtoList.stream().collect(Collectors.toMap(SysUserLastLoginTimeDto::getUserName,
                    SysUserLastLoginTimeDto::getLastLoginTime));

        }
        return lastLoginMap;
    }

    @Cacheable(value = "authority", key = "#siteCode+':'+#userId.toString()")
    public Authority getUserAuth(Long userId, String siteCode) {
        List<SysUserAgyaccountrelation> agyaccountrelations = sysUserDao.getAuthAgy(userId);
        List<SysUserMbrgrouprelation> mbrgrouprelations = sysUserDao.getAuthMbr(userId);
        String agyAthIds_local = "";
        String agyAthIds_total = "";
        for (SysUserAgyaccountrelation agy : agyaccountrelations) {
            agy.setUserId(userId);
            if (agy.getAgyAccountType() != null) {
                if (agy.getAgyAccountType() == 0) {
                    agyAthIds_total += agy.getAgyAccountId() + ",";
                } else {
                    agyAthIds_local += agy.getAgyAccountId() + ",";
                }
            }
        }
        //2 会员组
        String mbrAuth = "";
        for (SysUserMbrgrouprelation mbrgrouprelation : mbrgrouprelations) {
            mbrgrouprelation.setUserId(userId);
            mbrAuth += mbrgrouprelation.getMbrGroupId() + ",";
        }
        //把当前用户的权限信息放入redis缓存
        Map<String, Object> rowAuthority = new HashMap<>(8);
        rowAuthority.put("agyAthIds_total", !"".equals(agyAthIds_total) ? agyAthIds_total.substring(0, agyAthIds_total.length() - 1) : "");
        rowAuthority.put("agyAthIds_local", !"".equals(agyAthIds_local) ? agyAthIds_local.substring(0, agyAthIds_local.length() - 1) : "");
        rowAuthority.put("mbrAuth", !"".equals(mbrAuth) ? mbrAuth.substring(0, mbrAuth.length() - 1) : "");
        SysUserEntity user = sysUserDao.queryObject(userId);
        rowAuthority.put("mbrAuthType", user.getUserMbrGroupAuth());
        rowAuthority.put("agyAuthType", user.getUserAgyAccountAuth());
        return new Authority(String.valueOf(userId), rowAuthority);
    }

    /**
     * 检查角色是否越权
     */
    private void checkRole(SysUserEntity user, String userName) {
        //如果不是超级管理员，则需要判断用户的角色是否自己创建
        if (getUser().getRoleId() == 1) {
            return;
        }
        //查询用户创建的角色列表
        List<Long> roleIdList = sysRoleService.queryRoleIdList(userName);
        //判断是否越权
        if (!roleIdList.containsAll(user.getRoleIdList())) {
            throw new RRException("新增用户所选角色，不是本人创建");
        }
    }


    /**
     * 保存用户和数据权限的关联关系
     *
     * @param user
     */
    @Transactional
    @CachePut(value = "authority", key = "#siteCode+':'+#user.getUserId().toString()")
    public Authority saveDataAuth(SysUserEntity user, String siteCode) {
        //1 代理关系
        Long userId = user.getUserId();

        String agyAthIds_local = "";
        String agyAthIds_total = "";
        List<SysUserAgyaccountrelation> agyaccountrelations = user.getAgyAccounts();
        if (Collections3.isNotEmpty(agyaccountrelations)) {
            for (int i = 0; i < agyaccountrelations.size(); i++) {
                SysUserAgyaccountrelation agy = agyaccountrelations.get(i);
                agy.setUserId(userId);
                agyaccountrelations.get(i).setUserId(userId);
                if (agy.getAgyAccountType() == 0) {
                    agyAthIds_total += agy.getAgyAccountId() + ",";
                } else {
                    agyAthIds_local += agy.getAgyAccountId() + ",";
                }
            }
            sysUserAgyaccountrelationService.saveList(agyaccountrelations);
        }
        //2 会员组
        String mbrAuth = "";
        List<SysUserMbrgrouprelation> mbrgrouprelations = user.getMbrGroups();
        if (Collections3.isNotEmpty(mbrgrouprelations)) {
            for (SysUserMbrgrouprelation mbrgrouprelation : mbrgrouprelations) {
                mbrgrouprelation.setUserId(userId);
                mbrAuth += mbrgrouprelation.getMbrGroupId() + ",";
            }
            sysUserMbrgrouprelationService.saveList(mbrgrouprelations);
        }
        //把当前用户的权限信息放入redis缓存
        Map<String, Object> rowAuthority = new HashMap<>(8);
        rowAuthority.put("agyAthIds_total", !"".equals(agyAthIds_total) ? agyAthIds_total.substring(0, agyAthIds_total.length() - 1) : "");
        rowAuthority.put("agyAthIds_local", !"".equals(agyAthIds_local) ? agyAthIds_local.substring(0, agyAthIds_local.length() - 1) : "");
        rowAuthority.put("mbrAuth", !"".equals(mbrAuth) ? mbrAuth.substring(0, mbrAuth.length() - 1) : "");
        rowAuthority.put("mbrAuthType", user.getUserMbrGroupAuth());
        rowAuthority.put("agyAuthType", user.getUserAgyAccountAuth());
        return new Authority(String.valueOf(userId), rowAuthority);
    }

    @CacheEvict(value = "authority", key = "#siteCode+':'+#userId", allEntries = true)
    public void deleteAuthorityCache(Long userId, String siteCode) {
    }

    public void updateEnable(SysUserEntity user, String userName, String ip) {
        sysUserDao.updateEnable(user);

        //添加操作日志
        SysUserEntity userEntity = queryUserEntityOne(user.getUserId());
        userEntity.setStatus(user.getStatus());
        mbrAccountLogService.updateSysUserStatusLog(userEntity, userName, ip);
    }

    /**
     * 更新用户数据权限
     *
     * @param user
     * @return
     */
    @Transactional
    @CachePut(value = "authority", key = "#siteCode+':'+#user.getUserId().toString()")
    public Authority updateDataAuth(SysUserEntity user, String siteCode) {
        sysUserDao.deleteAuthority(user.getUserId());

        //更新用户数据权限
        SysUserService sysUserService = context.getBean(SysUserService.class);
        return sysUserService.saveDataAuth(user, CommonUtil.getSiteCode());
    }

    public int authsecPwd(Long userId, String securepwd) {
        SysUserEntity dbUserEntity = sysUserDao.queryObject(userId);
        String newSecurePwd = new Sha256Hash(securepwd, dbUserEntity.getSalt()).toHex();
        if (!newSecurePwd.equals(dbUserEntity.getSecurepwd())) {
            return ErrorCode.code_06;
        } else {
            return ErrorCode.code_07;
        }
    }

    public List<SysUserEntity> queryByUserNameList(List<String> usernameList) {
        return sysUserDao.queryByUserNameList(usernameList);
    }

    /**
     * 导出系统用户列表
     * @param userEntity
     * @param userId
     * @param module
     * @return
     */
    public SysFileExportRecord exportUserList(SysUserEntity userEntity, Long userId, String module) {
        //查询列表数据
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            try {

                PageUtils pageUtils = this.queryConditions(userEntity);

                List<SysUserEntity> resultList = (List<SysUserEntity>) pageUtils.getList();

                ExportParams exportParams = new ExportParams();
                Map<String, Object> map = new HashMap<>();
                List<SysUserExportDto> data = resultList.stream().map(sysUser -> {
                    SysUserExportDto dto = new SysUserExportDto();
                    dto.setUserName(sysUser.getUsername());
                    dto.setUserRole(sysUser.getRoleName());
                    dto.setRealName(sysUser.getRealName());
                    dto.setAvailable(sysUser.getStatus() == 1 ? "是" : "否");
                    dto.setLastLoginTime(DateUtil.formatDate(sysUser.getLastLoginTime()));
                    return dto;
                }).collect(Collectors.toList());

                map.put("data", data);
                map.put("entity", SysUserExportDto.class);
                map.put("title", exportParams);

                List<Map<String, Object>> sheetsList = new ArrayList<>();
                sheetsList.add(map);
                sysFileExportRecordService.exportMilSheet(sheetsList, userId, module, siteCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return record;
    }
}
