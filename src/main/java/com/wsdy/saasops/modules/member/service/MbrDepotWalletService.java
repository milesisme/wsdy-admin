package com.wsdy.saasops.modules.member.service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.wsdy.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.github.pagehelper.PageHelper;



@Service
@Slf4j
public class MbrDepotWalletService extends BaseService<MbrDepotWalletMapper, MbrDepotWallet>{
	@Autowired
	private MbrMapper mbrMapper;
    @Autowired
	private  MbrDepotAsyncWalletService mbrDepotAsyncWalletService;

	public PageUtils queryListPage(MbrDepotWallet mbrDepotWallet, Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
		List<MbrDepotWallet> list = mbrMapper.findAccountWallet(null, mbrDepotWallet.getAccountId());
		String siteCode = CommonUtil.getSiteCode();
		List<CompletableFuture<UserBalanceResponseDto>> listComplet = new ArrayList<CompletableFuture<UserBalanceResponseDto>>();
		list.forEach(e1 -> {
			try {
				listComplet.add(mbrDepotAsyncWalletService.getAsyncBalance(e1.getDepotId(), e1.getAccountId(), siteCode));
			} catch (Exception e) {
				log.debug("平台号:{}查账失败!", e1.getDepotId());
			}
		});
		CompletableFuture.allOf(listComplet.toArray(new CompletableFuture[listComplet.size()])).join();
		listComplet.forEach(e -> {
			list.forEach(wallet -> {
				try {
					if (e.get().getDepotId().equals(wallet.getDepotId())) {
						wallet.setBalance(e.get().getBalance());
						wallet.setIsTransfer(Available.enable);
					}
				} catch (Exception ex) {
					log.debug("平台号:{}查账失败!", wallet.getDepotId());
				}
			});

		});
		return BeanUtil.toPagedResult(list);
	}

    public List<MbrDepotWallet> queryCondDepot(MbrDepotWallet mbrDepotWallet) {
        return mbrMapper.findAccountWallet(mbrDepotWallet.getDepotIds(),mbrDepotWallet.getAccountId());
    }

	public List<MbrDepotWallet> findDepots(Integer accountId) {
		return mbrMapper.findDepots(accountId);
	}
}
