package com.wsdy.saasops.modules.base.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.modules.base.entity.BaseArea;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;

@Service
public class BaseAreaService{
	@Autowired
	BaseMapper baseMapper;

	public List<BaseArea> findArea(BaseArea record) {
		return baseMapper.findBaseArea(record);
	}
}
