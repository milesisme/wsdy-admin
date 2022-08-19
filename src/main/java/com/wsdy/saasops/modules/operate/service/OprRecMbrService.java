package com.wsdy.saasops.modules.operate.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;

import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

import java.util.*;

import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wsdy.saasops.common.constants.OprConstants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.log.dao.OperationLogMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.dao.OprMesMapper;
import com.wsdy.saasops.modules.operate.dao.OprRecMapper;
import com.wsdy.saasops.modules.operate.dao.OprRecMbrMapper;
import com.wsdy.saasops.modules.operate.dto.AgyAccDto;
import com.wsdy.saasops.modules.operate.entity.OprMes;
import com.wsdy.saasops.modules.operate.entity.OprRec;
import com.wsdy.saasops.modules.operate.entity.OprRecMbr;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.github.pagehelper.PageHelper;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class OprRecMbrService extends BaseService<OprRecMbrMapper, OprRecMbr> {

    @Autowired
    private OprRecMbrMapper oprRecMbrMapper;
    @Autowired
    private OprRecMapper oprRecMapper;
    @Autowired
    private OprMesMapper oprMesMapper;
    @Value("${opr.plot.excel.path}")
    private String oprExcelPath;
    @Autowired
    OperationLogMapper operationLogMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrAccountMapper MbrAccountMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgentAccountService agentAccountService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;

    // 获取用户未读站内信数量
    public Integer getUnreadMsgCount(String loginName) {
        OprRecMbr record = new OprRecMbr();
        record.setMbrName(loginName);
        record.setIsRead(OprConstants.UN_READ);
        Integer count = oprRecMbrMapper.queryMbrMesList(record).size();
        return count;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void modifyOrm(List<OprRecMbr> ormList, String userName, String ip, Boolean isSign) {
        if (ormList != null && ormList.size() != 0) {
            for (int s = 0; s < ormList.size(); s++) {
                OprRecMbr orm = ormList.get(s);
                OprRecMbr opr = oprRecMbrMapper.selectOne(orm);
                deleteRecord(orm, userName, ip, isSign);
            }
        }
    }


    // 批量设置已读
    @Transactional(propagation = Propagation.REQUIRED)
    public void readBatch(List<OprRecMbr> ormList) {
        if (ormList != null) {
            for (int s = 0; s < ormList.size(); s++) {
                OprRecMbr orm = ormList.get(s);
                OprRecMbr record = new OprRecMbr();
                record.setMsgId(orm.getMsgId());
                record.setMbrId(orm.getMbrId());
                OprRecMbr opr = oprRecMbrMapper.selectOne(record);
                record.setIsRead(OprConstants.READED);
                record.setReadDate(getCurrentDate(FORMAT_18_DATE_TIME));
                if (null == opr) {
                    oprRecMbrMapper.insertSelective(record);
                } else {
                    record.setId(opr.getId());
                    oprRecMbrMapper.updateByPrimaryKeySelective(record);
                }

            }
        }
    }

    // 单条阅读
    @Transactional(propagation = Propagation.REQUIRED)
    public void readMsg(OprRecMbr oprRecMbr) {
        if (oprRecMbr != null) {
            OprRecMbr record = new OprRecMbr();
            record.setMsgId(oprRecMbr.getMsgId());

            record.setMbrId(oprRecMbr.getMbrId());
//TODO			
            OprRecMbr opr = oprRecMbrMapper.selectOne(record);
            record.setIsRead(OprConstants.READED);
            record.setReadDate(getCurrentDate(FORMAT_18_DATE_TIME));
            if (null == opr) {
                oprRecMbrMapper.insertSelective(record);
            }
        }
    }

    public Object queryMbrList(MbrAccount mbrAccount, String string) {

        if (mbrAccount.getGroupIds() == null || mbrAccount.getGroupIds().size() == 0) {
            mbrAccount.setGroupIds(null);
        }
        if (mbrAccount.getCagencyIds() == null || mbrAccount.getCagencyIds().size() == 0) {
            mbrAccount.setCagencyIds(null);
        }
        if (mbrAccount.getTagencyIds() == null || mbrAccount.getTagencyIds().size() == 0) {
            mbrAccount.setTagencyIds(null);
        }
        List<Map<String,Object>> mbrList = mbrMapper.queryMbrList(mbrAccount);
        return BeanUtil.toPagedResult(mbrList);
    }

    public Object queryAgentList(AgyAccDto agyAccDto, String auth) {
        List<AgentAccount> list = new ArrayList<>();

        if (agyAccDto.getIsAllGen() != null && agyAccDto.getIsAllGen() == true) {
            List<AgentAccount> genAgyList = agentAccountService.findTopAccountAll(null);
            return BeanUtil.toPagedResult(genAgyList);
        }

        if (agyAccDto.getIsAllAgt() != null && agyAccDto.getIsAllAgt() == true) {
            if (null != agyAccDto.getGenIds()) {
                List<AgentAccount> genAgyList = new ArrayList<>();
                for (Integer genId : agyAccDto.getGenIds()) {
                    AgentAccount agy = agentAccountService.queryObject(genId);
                    genAgyList.add(agy);
                }
                return BeanUtil.toPagedResult(genAgyList);
            }
        } else if (agyAccDto.getIsAllAgt() != null && agyAccDto.getIsAllAgt() == false) {
            if (null != agyAccDto.getAgtIds()) {
                List<AgentAccount> agyList = new ArrayList<>();
                for (Integer agyId : agyAccDto.getAgtIds()) {
                    AgentAccount agy = agentAccountService.queryObject(agyId);
                    agyList.add(agy);
                }
                return BeanUtil.toPagedResult(agyList);
            }
        }
        if (null != agyAccDto.getLoginName()) {
            list = agentMapper.queryAgentList(agyAccDto);
            BeanUtil.toPagedResult(list);
        } else {
            list = agentMapper.queryAgentList(agyAccDto);
            BeanUtil.toPagedResult(list);
        }
        return BeanUtil.toPagedResult(list);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOprRecMbr(OprRecMbr oprRecMbr, String loginName, int operateType, String ip) {
        // 获取当前用户
        SysUserEntity user = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        // 存储站内信内容
        OprMes oprMes = new OprMes();
        oprMes.setSender(user.getUsername());
        oprMes.setTitle(oprRecMbr.getTitle());
        oprMes.setContext(oprRecMbr.getContext());
        oprMes.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        oprMesMapper.insert(oprMes);
        OprMes om = oprMesMapper.selectOne(oprMes);

        //添加操作日志
        mbrAccountLogService.sendRecMbrMail(oprRecMbr, loginName, operateType, ip);

        // 站内信发送会员和代理
        send(oprRecMbr, om);
    }

    /**
     * 站内信发送
     *
     * @param oprRecMbr
     * @param sender
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void sendInMail(OprRecMbr oprRecMbr, String sender) {
        // 存储站内信内容
        OprMes oprMes = new OprMes();
        oprMes.setSender(sender);
        oprMes.setTitle(oprRecMbr.getTitle());
        oprMes.setContext(oprRecMbr.getContext());
        oprMes.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        oprMesMapper.insert(oprMes);
        OprMes om = oprMesMapper.selectOne(oprMes);
        send(oprRecMbr, om);
    }

    private void send(OprRecMbr oprRecMbr, OprMes om) {
        // 站内信发送会员
        if (null != oprRecMbr.getMbrList()) {
            List<OprRec> orList = new ArrayList<OprRec>();
            for (MbrAccount acc : oprRecMbr.getMbrList()) {
                if (null == acc.getId()) {
                    MbrAccount record = new MbrAccount();
                    record.setLoginName(acc.getLoginName());
                    List<MbrAccount> mList = MbrAccountMapper.select(record);
                    if (null != mList) {
                        MbrAccount mbrAccount = mList.get(0);
                        insertMesToMbrs(om, mbrAccount, orList);
                    }
                } else {
                    insertMesToMbrs(om, acc, orList);
                }
            }
            oprRecMapper.insertList(orList);
        }
        // 站内信发送代理
        if (null != oprRecMbr.getAgyList()) {
            List<OprRec> orList = new ArrayList<OprRec>();
            for (AgentAccount agent : oprRecMbr.getAgyList()) {
                if (null == agent.getId()) {
                    AgyAccDto agyAccDto = new AgyAccDto();
                    agyAccDto.setLoginName(agent.getAgyAccount());
                    List<AgentAccount> aList = agentMapper.queryAgentList(agyAccDto);
                    if (null != aList) {
                        AgentAccount aa = aList.get(0);
                        if (aa.getAgyAccount().equals(agent.getAgyAccount())) {
                            insertMesToAgents(om, aa, orList);
                        }
                    }
                } else {
                    insertMesToAgents(om, agent, orList);
                }
            }
            oprRecMapper.insertList(orList);
        }
    }


    private void insertMesToAgents(OprMes om, AgentAccount agent, List<OprRec> orList) {
        OprRec oprRec = new OprRec();
        oprRec.setMsgId(om.getId());
        if (agent.getParentId() == 0) {
            oprRec.setGenAgtId(agent.getId());
        } else {
            oprRec.setAgtId(agent.getId());
        }
        orList.add(oprRec);
    }

    private void insertMesToMbrs(OprMes om, MbrAccount mbrAccount, List<OprRec> orList) {
        OprRec oprRec = new OprRec();
        oprRec.setMsgId(om.getId());
        oprRec.setMbrId(mbrAccount.getId());
        orList.add(oprRec);
    }


    public PageUtils queryListPage(OprRecMbr oprRecMbr, Integer pageNo, Integer pageSize, String orderBy, String auth) {
        List<OprRecMbr> ormList = new LinkedList<>();
        PageHelper.startPage(pageNo, pageSize);
        if (oprRecMbr.getMbrTypes() != null && oprRecMbr.getMbrTypes().size() != 0) {
            String types = oprRecMbr.getMbrTypes().toString().replace("[", "").replace("]", "");
            switch (types) {
                case "1":
                    ormList = oprRecMbrMapper.queryMbrMesList(oprRecMbr);
                    break;
                case "2":
                    ormList.addAll(oprRecMbrMapper.queryAgyMesList(oprRecMbr));
                    break;
                default:
                    ormList.addAll(oprRecMbrMapper.queryAllList(oprRecMbr));
            }

        } else {
            ormList = oprRecMbrMapper.queryAllList(oprRecMbr);
        }
        return BeanUtil.toPagedResult(ormList);
    }


    public void deleteRecord(OprRecMbr orm, String userName, String ip, Boolean isSign) {
        OprRecMbr record = new OprRecMbr();
        record.setMsgId(orm.getMsgId());
        record.setMbrId(orm.getMbrId());
        OprRecMbr opr = oprRecMbrMapper.selectOne(record);
        OprMes oprMes = new OprMes();
        oprMes.setId(orm.getMsgId());
        OprMes msgRecord = oprMesMapper.selectOne(oprMes);
        record.setIsRead(OprConstants.DELETED);
        record.setReadDate(getCurrentDate(FORMAT_18_DATE_TIME));
        if (null == opr) {
            oprRecMbrMapper.insertSelective(record);
        } else {
            record.setId(opr.getId());
            oprRecMbrMapper.updateByPrimaryKeySelective(record);
        }
        //添加操作日志
        if (Boolean.TRUE.equals(isSign)) {
            mbrAccountLogService.deleteOprRecLog(opr, msgRecord.getTitle(), userName, ip);
        }
    }

    /**
     * 删除站内过期信息
     */
    public void deleteOprRecMbr() {
        oprRecMbrMapper.deleteOprRecMbr();
    }
}
