package com.wsdy.saasops.modules.operate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.operate.dao.SetGmGameMapper;
import com.wsdy.saasops.modules.operate.dto.GameDepotNameDto;
import com.wsdy.saasops.modules.operate.entity.SetGmGame;

@Service
public class SetGmGameService extends BaseService<SetGmGameMapper, SetGmGame> {

	@Autowired
	private SetGmGameMapper setGmGameMapper;
	
	
	public List<GameDepotNameDto> selectSportSetDepotname(){
		return setGmGameMapper.selectSportSetDepotname();
	}

}
