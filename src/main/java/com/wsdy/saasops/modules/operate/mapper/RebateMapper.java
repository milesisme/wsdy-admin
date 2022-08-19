package com.wsdy.saasops.modules.operate.mapper;

import com.wsdy.saasops.modules.operate.entity.RebateCat;
import com.wsdy.saasops.modules.operate.entity.RebateInfo;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RebateMapper {

    List<RebateInfo> queryListAll(RebateInfo rebateInfo);

    List<RebateInfo> refferList(RebateInfo rebateInfo);

    List<RebateInfo> refferListEgSanGong(RebateInfo rebateInfo);

    BigDecimal findTotalAmount(RebateInfo rebateInfo);

    List<RebateCat> findCatValidbetList(RebateInfo rebateInfo);
}
