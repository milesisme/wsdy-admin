package com.wsdy.saasops.modules.base.service;

import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.base.entity.BaseAuth;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.service.MbrGroupService;
import com.wsdy.saasops.modules.sys.entity.Authority;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.SysUserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public abstract class BaseService<D extends MyMapper<T>, T> {

	@Autowired
	protected D mapper;
	@Autowired
	private AgentAccountService agentAccountService;
    @Autowired
    private SysUserService sysUserService;

    @Autowired
	private MbrGroupService mbrGroupService;

	public int save(T entity) {
		return mapper.insert(entity);
	}

	public int delete(T entity) {
		return mapper.deleteByPrimaryKey(entity);
	}

	public int deleteById(Integer id) {
		return mapper.deleteByPrimaryKey(id);
	}

	public int update(T entity) {
		return mapper.updateByPrimaryKeySelective(entity);
	}
    public int update(T entity,Object example)
	{
		return mapper.updateByExampleSelective(entity,example);
	}
	public T queryObject(Integer key) {
		return mapper.selectByPrimaryKey(key);
	}

	public T queryObjectCond(T entity) {
		return mapper.selectOne(entity);
	}

	public List<T> queryList() {
		return mapper.selectAll();
	}

	public List<T> queryListCond(T entity) {
		return mapper.select(entity);
	}
	
	public int selectCount(T entity)
	{
		return mapper.selectCount(entity);
	}

    /**
     * 根据权限查询
     * @param entity
     * @return
     */
	public List<T> queryListCondInAuth(T entity) {
		return mapper.selectInAuth(entity);
	}

	protected SysUserEntity getUser() {
		return (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
	}

	protected Long getUserId() {
		return getUser().getUserId();
	}
	/**
	 * 获取当前用户的行权限，包括总代、代理、会员组
	 * @return
	 */
	public BaseAuth getRowAuth(){
        BaseAuth bAuth =new BaseAuth();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Authority auths =sysUserService.getUserAuth(getUserId(), CommonUtil.getSiteCode());
		Map rowAuth = auths.getRowAuthority();
		String authAgy_totalIds = "";
		String authAgy_local = "";
        if(rowAuth.get("agyAuthType").equals(1)){
			List<Integer> authAgyLocalList =agentAccountService.getAllLocalAgentAccount();
			authAgy_local =authAgyLocalList.toString().replace("[","").replace("]","");
		}else {
			authAgy_totalIds = auths.getRowAuthority().get("agyAthIds_total").toString();
			authAgy_local =auths.getRowAuthority().get("agyAthIds_local").toString();
		}
		if(!StringUtils.isEmpty(authAgy_totalIds)) {
			List<AgentAccount> agentAccounts =agentAccountService.getAllParentAccount(authAgy_totalIds,null);
			for ( AgentAccount agy: agentAccounts) {
				authAgy_local += agy.getId() +",";
			}
			if(agentAccounts.size() !=0) {
				bAuth.setAgyAccountIds(authAgy_local.substring(0, authAgy_local.length() - 1));
			}
		}
		authAgy_local=authAgy_local.endsWith(",")?authAgy_local.substring(0, authAgy_local.length() - 1):authAgy_local;
		bAuth.setAgyAccountIds(authAgy_local);
		if(rowAuth.get("mbrAuthType").equals(1)){
			String mbrAuths =mbrGroupService.getAllMbrGroupIds().toString().replace("[","").replace("]","");
			bAuth.setGroupIds(mbrAuths);
		}else {
			bAuth.setGroupIds(auths.getRowAuthority().get("mbrAuth").toString());
		}
		return bAuth;
	}

}