package com.wsdy.saasops.modules.operate.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.modules.user.dto.ElecGameDto;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.operate.dao.SetGameCategoryMapper;
import com.wsdy.saasops.modules.operate.dao.SetGameCategoryRelationMapper;
import com.wsdy.saasops.modules.operate.dao.TGmGameMapper;
import com.wsdy.saasops.modules.operate.entity.OprGame;
import com.wsdy.saasops.modules.operate.entity.SetGameCategory;
import com.wsdy.saasops.modules.operate.entity.SetGameCategoryRelation;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import com.wsdy.saasops.modules.operate.entity.TGmGame;

import cn.hutool.core.collection.CollectionUtil;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2021-12-27
 */
@Service
public class SetGameCategoryService extends BaseService<SetGameCategoryMapper, SetGameCategory> {

	@Autowired
	private TGmCatService tGmCatService;
	
	@Autowired
	private TGmGameMapper tGmGameMapper;

	@Autowired
	private SetGameCategoryMapper setGameCategoryMapper;

	@Autowired
	private SetGameCategoryRelationMapper setGameCategoryRelationMapper;

	/**
	 * 【后台管理查询】查询分类
	 * 
	 * @param setGameCategory
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	public PageUtils list(SetGameCategory setGameCategory, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		setGameCategory.setSiteCode(CommonUtil.getSiteCode());
		List<SetGameCategory> list = setGameCategoryMapper.list(setGameCategory);
		// 循环查询set_game_category_relation表的游戏名字
		for (SetGameCategory target : list) {
			// 原有的数据中心分类，查询t_gm_game表
			if (target.getIsTGmCatId()) {
				List<SetGameCategoryRelation> setGameCategoryRelationList = new ArrayList<>();
				List<TGmGame> tGmGameList = tGmGameMapper.getBySubCatId(target.getId(), target.getGamelogoid());
				// 组装数据到setGameCategoryRelationList
				for (TGmGame tGmGame : tGmGameList) {
					SetGameCategoryRelation setGameCategoryRelation = new SetGameCategoryRelation();
					setGameCategoryRelation.setGamecategoryid(tGmGame.getDepotId());
					setGameCategoryRelation.setGamename(tGmGame.getGameName());
					setGameCategoryRelation.setGameid(tGmGame.getId());
					setGameCategoryRelation.setGamecategoryid(target.getId());
					setGameCategoryRelation.setSortId(tGmGame.getSortId());
					setGameCategoryRelationList.add(setGameCategoryRelation);
				}
				// set setGameCategoryRelationList
				target.setGameCategoryRelations(setGameCategoryRelationList);
				// 拼接gamename
				StringJoiner joiner = new StringJoiner(",");
				setGameCategoryRelationList.forEach(t -> joiner.add(t.getGamename()));
				target.setGameName(joiner.toString());
			}
			// 非原有数据，查询set_game_category_relation的游戏id
			else {
				List<SetGameCategoryRelation> setGameCategoryRelationList = setGameCategoryRelationMapper
						.selectByCategoryId(target.getId());
				target.setGameCategoryRelations(setGameCategoryRelationList);
				StringJoiner joiner = new StringJoiner(",");
				setGameCategoryRelationList.forEach(t -> joiner.add(t.getGamename()));
				target.setGameName(joiner.toString());
			}
		}
		return BeanUtil.toPagedResult(list);
	}

	/**
	 * [后台保存] 更新setGameCategory 以及插入游戏数据到set_game_category_relation表
	 * 
	 * @param setGameCategory
	 */
	@Transactional
	public void updateCategor(SetGameCategory setGameCategory) {
		// 为数据中心的分类，插入数据
		if (setGameCategory.getIsTGmCatId()) {
			setGameCategory.setTGmCatId(setGameCategory.getId());
			setGameCategoryMapper.insertSetGameCategory(setGameCategory);
		}
		// IsTGmCatId = false 的，即：更新v2分类
		else {
			this.update(setGameCategory);
			// 删除分类对应的游戏关联表数据
			SetGameCategoryRelation setGameCategoryRelation = new SetGameCategoryRelation();
			setGameCategoryRelation.setGamecategoryid(setGameCategory.getId());
			setGameCategoryRelationMapper.delete(setGameCategoryRelation);
		}
		
		// 新加分类关联的游戏数据
		List<SetGameCategoryRelation> gameCategoryRelations = setGameCategory.getGameCategoryRelations();
		if (CollectionUtil.isNotEmpty(gameCategoryRelations)) {
			gameCategoryRelations.forEach(t -> t.setGamecategoryid(setGameCategory.getId()));
			setGameCategoryRelationMapper.insertList(gameCategoryRelations);
		}
	}
	
	/**
	 * [后台新增保存] 新加数据到setGameCategory 以及插入游戏数据到set_game_category_relation表
	 * 
	 * @param setGameCategory
	 */
	@Transactional
	public void saveCategor(SetGameCategory setGameCategory) {
		setGameCategory.setTGmCatId(0);
		setGameCategoryMapper.insertSetGameCategory(setGameCategory);
		// 新加分类关联的游戏数据
		List<SetGameCategoryRelation> gameCategoryRelations = setGameCategory.getGameCategoryRelations();
		if (CollectionUtil.isNotEmpty(gameCategoryRelations)) {
			gameCategoryRelations.forEach(t -> t.setGamecategoryid(setGameCategory.getId()));
			setGameCategoryRelationMapper.insertList(gameCategoryRelations);
		}
	}

	/**
	 * 后台删除分类，同时删除set_game_category_relation表的数据
	 * 
	 * @param id
	 */
	@Transactional
	public void deleteCategor(SetGameCategory setGameCategory) {
		// 默认分类删除记录，逻辑删除
		if (setGameCategory.getIsTGmCatId()) {
			TGmCat tGmCat = tGmCatService.queryObject(setGameCategory.getId());
			// 新加到v2分类表，标识为删除
			SetGameCategory setGameCategoryForDel = new SetGameCategory();
			setGameCategoryForDel.setName(tGmCat.getCatName());
			setGameCategoryForDel.setSortId(0);
			setGameCategoryForDel.setGamelogoid(0);
			setGameCategoryForDel.setTGmCatId(setGameCategory.getId());
			setGameCategoryForDel.setIsDelete(true);
			setGameCategoryForDel.setUpdateBy(getUser().getUsername());
			setGameCategoryForDel.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
			this.save(setGameCategoryForDel);
			return ;
		}
		// 删除游戏关联表
		SetGameCategoryRelation setGameCategoryRelation = new SetGameCategoryRelation();
		setGameCategoryRelation.setGamecategoryid(setGameCategory.getId());
		setGameCategoryRelationMapper.delete(setGameCategoryRelation);
		
		// 存在v2的分类，更新IsDelete = true
		SetGameCategory queryObject = this.queryObject(setGameCategory.getId());
		if (queryObject.getTGmCatId() > 0) {
			queryObject.setIsDelete(true);
			this.update(queryObject);
		}
		// V2 自己的新加的分类，直接删除
		else {
			SetGameCategory setGameCategoryForDel = new SetGameCategory();
			setGameCategoryForDel.setId(setGameCategory.getId());
			setGameCategoryMapper.delete(setGameCategoryForDel);
		}
	}
	
	/**
	 * 【用户端查询】当前平台下的所有彩票游戏分类
	 * 
	 * @param gamelogoid
	 * @return
	 */
	public List<SetGameCategory> getLotteryCategory(Integer gamelogoid) {
		return setGameCategoryMapper.getLotteryCategory(gamelogoid, CommonUtil.getSiteCode());
	}

	/**
	 * 【用户端查询】 分类下的游戏
	 * 
	 * @param elecGameDto
	 * @param pageSize 
	 * @param pageNo 
	 * @return
	 */
	public PageUtils getGameByCategory(ElecGameDto elecGameDto, @NotNull Integer pageNo, @NotNull Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<OprGame> oprGameList = setGameCategoryMapper.getGameByCategory(elecGameDto);
		return BeanUtil.toPagedResult(oprGameList);
	}


}
