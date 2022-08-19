package com.wsdy.saasops.modules.member.service;

import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.QiNiuYunUtil;
import com.wsdy.saasops.modules.member.dao.MbrOpinionMapper;
import com.wsdy.saasops.modules.member.entity.MbrOpinion;
import com.wsdy.saasops.modules.member.mapper.OpinionMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class MbrOpinionService {

    @Autowired
    private MbrOpinionMapper mbrOpinionMapper;
    @Autowired
    private OpinionMapper opinionMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private TGmApiService gmApiService;

    public PageUtils finOpinionList(MbrOpinion opinion, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrOpinion> opinions = opinionMapper.finOpinionList(opinion);
        opinions.stream().forEach(ms -> {
            if (StringUtils.isNotEmpty(ms.getImageUrl())) {
                ms.setImageUrl(gmApiService.queryGiniuyunUrl() + ms.getImageUrl());
            }
        });
        return BeanUtil.toPagedResult(opinions);
    }

    public void update(MbrOpinion opinion, String username) {
        MbrOpinion mbrOpinion = mbrOpinionMapper.selectByPrimaryKey(opinion.getId());
        if (nonNull(opinion)) {
            mbrOpinion.setStatus(opinion.getStatus());
            mbrOpinion.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            mbrOpinion.setModifyUser(username);
            mbrOpinionMapper.updateByPrimaryKeySelective(mbrOpinion);
        }
    }

    public String accountMessageSend(Integer accountId, String loginName,
                                   MultipartFile uploadMessageFile, String textContent,Integer type) {
        if (Objects.isNull(uploadMessageFile) && StringUtils.isEmpty(textContent)) {
            return null;
        }
        String resut = StringUtils.EMPTY;
        String key = RedisConstants.ACCOUNT_OPINION_KEY + CommonUtil.getSiteCode() + accountId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            MbrOpinion opinion = new MbrOpinion();
            opinion.setLoginName(loginName);
            opinion.setAccountId(accountId);
            opinion.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            opinion.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            opinion.setTextContent(textContent);
            opinion.setStatus(Constants.EVNumber.one);
            opinion.setType(type);
            if (nonNull(uploadMessageFile)) {
                try {
                    byte[] fileBuff = IOUtils.toByteArray(uploadMessageFile.getInputStream());
                    String fileName = qiNiuYunUtil.uploadFileKey(fileBuff);
                    opinion.setImageUrl(fileName);
                } catch (Exception e) {
                    log.error("messageSend", e);
                    throw new RRException("上传图片出错");
                }
            }
            mbrOpinionMapper.insert(opinion);
            redisService.del(key);
            resut = nonNull(opinion.getImageUrl())
                    ? gmApiService.queryGiniuyunUrl() + opinion.getImageUrl() : opinion.getTextContent();
        }
        return resut;
    }
}
