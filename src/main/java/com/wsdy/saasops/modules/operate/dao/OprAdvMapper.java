package com.wsdy.saasops.modules.operate.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.AdvBanner;
import com.wsdy.saasops.modules.operate.entity.OprAdv;


@Mapper
public interface OprAdvMapper extends MyMapper<OprAdv> {

	void deleteByIds(Map<String, Object> map);
	void deleteImageByIds(Map<String, Object> map);

	List<OprAdv> selectByIds(Map<String, Object> map);

	List<OprAdv> queryOprAdvList(OprAdv oprAdv);

	List<OprAdv> queryOprAdvByAvailable(@Param("advType") Integer advType);

	List<OprAdv> queryWebOprAdvList(OprAdv oprAdv);

	List<AdvBanner> queryAdvBannerDtoList(AdvBanner advBannerDto);

	void deleteImageById(@Param("id") Integer id);

	List<OprAdv> coupletList();

	OprAdv queryOprAdvInfo(@Param("id") Integer id);

	int updateOprAdvAvailable(OprAdv oprAdv);



}
