package com.wsdy.saasops.modules.task.mapper;


import com.wsdy.saasops.modules.operate.entity.OprActBlacklist;
import com.wsdy.saasops.modules.task.dto.TaskActivityDto;
import com.wsdy.saasops.modules.task.entity.TaskBonus;
import com.wsdy.saasops.modules.task.entity.TaskConfig;
import com.wsdy.saasops.modules.task.entity.TaskSignin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;


@Component
@Mapper
public interface TaskMapper {

    List<TaskConfig> configList(@Param("id") Integer id);

    List<OprActBlacklist> taskBlackList(@Param("id") Integer id);

    List<TaskBonus> findBonusStatistical(TaskBonus taskBonus);

    List<TaskBonus> bounsDetail(TaskBonus taskBonus);

    BigDecimal findBonusAmount(
            @Param("accountId") Integer accountId,
            @Param("time") String time);

    int findBlackListByAccountId(
            @Param("tmplcode") String tmplcode,
            @Param("accountId") Integer accountId);

    TaskSignin findAccountSignin(@Param("accountId") Integer accountId, @Param("time") String time);

    List<TaskActivityDto> findTaskActivity(@Param("activityIds") List<Integer> activityIds);

    void updateClickRate(@Param("configid") Integer configid);

    List<TaskConfig> findTaskConfigList(@Param("financialCode") String financialCode);

    List<Integer> findDrawAccountLevel(@Param("accountId") Integer accountId);

    Integer findTaskAccountLevel(@Param("accountId") Integer accountId);

    TaskBonus findTaskReceiveTime(
            @Param("accountId") Integer accountId,
            @Param("configId") Integer configId,
            @Param("time") String time);

    String financialCodeName(@Param("code") String code);

    TaskBonus findTaskBonusLimtOne(
            @Param("accountId") Integer accountId,
            @Param("configId") Integer configId);

    BigDecimal findDepositamountTsk(
            @Param("accountId") Integer accountId,
            @Param("auditTime") String auditTime);

    BigDecimal findValidbetTsk(
            @Param("username") String username,
            @Param("startday") String startday);

    List<TaskBonus> findHRTaskBonusList(
            @Param("accountId") Integer accountId,
            @Param("configId") Integer configId,
            @Param("num") Integer num);
}
