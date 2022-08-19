package com.wsdy.saasops.modules.sys.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.EncryptioUtil;
import com.wsdy.saasops.common.utils.google.GoogleAuthenticatorUtils;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.SysEncryptMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.SysEncrypt;
import com.wsdy.saasops.modules.sys.dao.SysUserDao;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
public class SysEncryptionService {

    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private SysSettingMapper sysSettingMapper;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysEncryptMapper sysEncryptMapper;
    @Autowired
    private MbrAccountMapper accountMapper;

    public static final String PREFIX_MOBILE = "ACCOUNTDES";
    public static final String PREFIX_MOBILE_JIA = "JIA";
    public static final String PREFIX_SPLICE = "__";

    public SysSetting encryptInfo() {
        return sysSettingService.getSysSetting(SystemConstants.SYS_ENCRYPT);
    }

    @Transactional
    public void updateEncryptInfo(Long userId, String securepwd, String value) {
        // 校验安全密码
        SysUserEntity dbUserEntity = sysUserDao.queryObject(userId);
        String newSecurePwd = new Sha256Hash(securepwd, dbUserEntity.getSalt()).toHex();
        if (!newSecurePwd.equals(dbUserEntity.getSecurepwd())) {
            throw new R200Exception("安全密码不正确");
        }

        SysSetting sysSetting = encryptInfo();
        SysSetting setting = new SysSetting();
        setting.setSyskey(SystemConstants.SYS_ENCRYPT);
        if (nonNull(sysSetting)) {
            sysSettingMapper.delete(setting);   // 后面insert
        }
        setting.setSysvalue(value);
        if ("1".equals(value)) {    // 开启
            SysEncrypt sysEncrypt = new SysEncrypt();
            sysEncrypt.setDessecretkey(GoogleAuthenticatorUtils.createSecretKey());
            sysEncrypt.setDel(Constants.EVNumber.zero);
            sysEncryptMapper.insert(sysEncrypt);
        }
        sysSettingMapper.insert(setting);
        castEncryption(CommonUtil.getSiteCode());
    }


    public void castEncryption(String siteCode) {
        log.info(siteCode + "开始数据加密");
        SysSetting sysSetting = encryptInfo();
        if (isNull(sysSetting)) {
            log.info(siteCode + "未设置过加密");
            return;
        }
        // 获取最新加密秘钥
        SysEncrypt sysEncrypt = sysEncryptMapper.findEncryptDesc(null);
        if (isNull(sysEncrypt)){
            log.info(siteCode + "获取SysEncrypt为null");
            return;
        }
        // 如果不存在最新加密秘钥加密的数据，则设置该秘钥为删除
        deleteSysEncrypt(sysEncrypt.getId());

        if ("1".equals(sysSetting.getSysvalue())) { // 加密开启
            if (isNull(sysEncrypt)) {
                return;
            }
            // 查出未加密的会员
            String key = PREFIX_MOBILE + PREFIX_SPLICE + sysEncrypt.getId();
            List<MbrAccount> accountList = sysEncryptMapper.desAccountList(key, Constants.EVNumber.zero, 3000); // isSign 0 未加密
            if (accountList.size() > 0) {
                accountList.stream().forEach(as -> {
                    updatAccountMobile(siteCode, as, sysEncrypt, Boolean.TRUE);
                });
            }
        }
        if ("0".equals(sysSetting.getSysvalue())) { // 加密关闭
            // 查出已加密的会员，mobile是明文
            String key = PREFIX_MOBILE;
            List<MbrAccount> accountList = sysEncryptMapper.desAccountList(key, Constants.EVNumber.one, 3000);
            if (accountList.size() > 0) {
                accountList.stream().forEach(as -> {
                    updatAccountMobile(siteCode, as, null, Boolean.FALSE);
                });
            }
        }
    }

    /**
     * 异步加解密
     * @param siteCode      站点code
     * @param as            会员对象
     * @param sysEncrypt    秘钥对象
     * @param isSign        true 加密  false 解密
     */
    public void updatAccountMobile(String siteCode, MbrAccount as, SysEncrypt sysEncrypt, Boolean isSign) {
        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            if (isSign) {
                log.info(siteCode + "会员id【" + as.getId() + "】，电话" + as.getMobile());
                String jiami = EncryptioUtil.encrypt(as.getMobile(), sysEncrypt.getDessecretkey());
                if (nonNull(jiami)) {
                    // JIA 用于当set值时与读库的set区分
                    String mobilekey = PREFIX_MOBILE_JIA + PREFIX_MOBILE + PREFIX_SPLICE + sysEncrypt.getId() + PREFIX_SPLICE + jiami;
                    log.info(siteCode + "会员id【" + as.getId() + "】，电话加密后" + mobilekey);
                    as.setMobile(mobilekey);
                }
            }
            accountMapper.updateByPrimaryKeySelective(as);
        });
    }

    public void deleteSysEncrypt(Integer id) {
        List<SysEncrypt> encryptList = sysEncryptMapper.findEncryptNoId(id);
        if (encryptList.size() > 0) {
            encryptList.stream().forEach(es -> {
                String key = PREFIX_MOBILE + PREFIX_SPLICE + es.getId();
                int count = sysEncryptMapper.desAccountCount(key);
                if (count == 0) {
                    es.setDel(Constants.EVNumber.one);
                    sysEncryptMapper.updateByPrimaryKeySelective(es);
                }
            });
        }
    }
}
