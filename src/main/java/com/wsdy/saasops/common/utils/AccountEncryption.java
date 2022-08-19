package com.wsdy.saasops.common.utils;

import com.wsdy.saasops.modules.member.entity.SysEncrypt;
import com.wsdy.saasops.modules.member.dao.SysEncryptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

import static com.wsdy.saasops.modules.sys.service.SysEncryptionService.*;


@Component
public class AccountEncryption {

    @Autowired
    private SysEncryptMapper sysEncryptMapper;

    public static AccountEncryption accountEncryption;

    // 饿汉式单例
    @PostConstruct
    public void init() {
        accountEncryption = this;
        accountEncryption.sysEncryptMapper = this.sysEncryptMapper;
    }

    public String accountMobieEncrypt(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return mobile;
        }
        String[] a = mobile.split(PREFIX_SPLICE);
        String str = PREFIX_MOBILE_JIA + PREFIX_MOBILE;  // JIAACCOUNTDES
        if (str.equals(a[0])) { // 1.进行加密时的set ，JIAACCOUNTDES 处理 ACCOUNTDES
            return mobile.substring(3);
        }
        if (!PREFIX_MOBILE.equals(a[0])) {  // 2.查库的非加密的mobile的set 及 正常明文mobile的set,由调度去处理后续增加的
            return mobile;
        }
        // 3.查库的加密的mobile的set
        SysEncrypt encrypt = accountEncryption.sysEncryptMapper.findEncryptDesc(Integer.parseInt(a[1]));    // 获取该加密手机号的加密秘钥
        String jiemi = EncryptioUtil.decrypt(a[2], encrypt.getDessecretkey());  // 解密
        return jiemi;
    }
}