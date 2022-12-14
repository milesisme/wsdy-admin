package com.wsdy.saasops.modules.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;

import java.util.List;
import java.util.Map;

/**
 * 基础Dao(还需在XML文件里，有对应的SQL语句)
 *
 */
@Mapper
@Component
public interface BaseDao<T>  {
	
	void save(T t);
	
	void save(Map<String, Object> map);
	
	void saveBatch(List<T> list);
	
	int update(T t);
	
	int update(Map<String, Object> map);
	
	int delete(Object id);
	
	int delete(Map<String, Object> map);
	
	int deleteBatch(Object[] id);

	T queryObject(Object id);
	
	List<T> queryList(SysRoleEntity roleEntity);
	
	List<T> queryList(Object id);
	
	int queryTotal(SysRoleEntity roleEntity);

	int queryTotal();

}
