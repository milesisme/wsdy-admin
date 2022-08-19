package com.wsdy.saasops.config;

import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.AESUtil;
import com.wsdy.saasops.common.utils.SpringContextUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;


@Slf4j
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisInterceptor implements Interceptor {

    private static final String PRE_SCHEMA = "saasops_";

    private static final String MYBATIS_SQL_ID = "delegate.boundSql.sql";

    private static final String MYBATIS_SQL_STATEMENT = "delegate.mappedStatement";

    private static final String MANAGE = "manage";
    private static final String VERIFY = "verify";

    private static final String MYCAT_SQL = "/*!mycat:schema = ";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        //获取statementHandler包装类
        MetaObject MetaObjectHandler = SystemMetaObject.forObject(statementHandler);
        //获取查询接口映射的相关信息
        MappedStatement mappedStatement = (MappedStatement) MetaObjectHandler.getValue(MYBATIS_SQL_STATEMENT);
        //Mapper 对应的id ,暂时用不到
        String mapId = mappedStatement.getId();
        //获取sql
        String sql = (String) MetaObjectHandler.getValue(MYBATIS_SQL_ID);
        String schema = MANAGE;
        if (!notFilterSql().contains(mapId)) {
            HttpServletRequest request = getSchemaName();
            if (request != null) {
                //同步的处理方式
                schema = request.getAttribute(SystemConstants.SCHEMA_NAME).toString();
            } else if (ThreadLocalCache.siteCodeThreadLocal.get() != null && ThreadLocalCache.siteCodeThreadLocal.get().getSiteCode() != null) {
                //异步处理方式
                schema = TCpSiteService.siteCode.get(ThreadLocalCache.siteCodeThreadLocal.get().getSiteCode());
            }
        }
        //log.error("B-SQL = " + sql + " schema = " + schema +" F-SQL = " + getMysql(sql, PRE_SCHEMA + schema + "."));
        //获取站点前缀
        MetaObjectHandler.setValue(MYBATIS_SQL_ID, getMysql(sql, PRE_SCHEMA + schema + "."));
        return invocation.proceed();
    }

    private HttpServletRequest getSchemaName() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if (null == request.getAttribute(SystemConstants.SCHEMA_NAME) || "".equals(request.getAttribute(SystemConstants.SCHEMA_NAME))) {
                String siteCode = AESUtil.decrypt(request.getHeader(SystemConstants.STOKEN));
                String schemaName = SpringContextUtils.getBean(SystemConstants.T_CP_SITE_SERVICE, TCpSiteService.class).getSchemaName(siteCode);
                if (schemaName == null) {
                    log.error("SToken wrong,SToken =" + request.getHeader(SystemConstants.STOKEN), "schemaName = " + schemaName);
                    throw new RRException("SToken wrong ,schemaName  中不存在次siteCode对应");
                }
                request.setAttribute(SystemConstants.SCHEMA_NAME, schemaName);
                return request;
            } else {
                return request;
            }
        }
        return null;
    }

    private List<String> notFilterSql() {
        return Lists.newArrayList("com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.findCpSiteOne",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.findCpSiteOneEquals",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.findCpSiteLike",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.findPreciseSiteOne",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.getCpSiteBySiteCode",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.findCpSite",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.insertCpSiteUrlInfo",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.deleteCpSiteUrlInfo",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.updateCpSiteUrlClientType",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.getSiteurl",
                "com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper.queryGiniuyunUrl");
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        log.info(properties.toString());
    }

    private String getMysql(String sql, String preSchema) {
        for (String table : getTables()) {
            if (sql.contains(table)) {
                String newPreSchema = preSchema;
                String newTable = table;
                if ("t_gm_api".equalsIgnoreCase(table)) {
                    newTable = "s_gm_api";
                }
                if ("t_gm_apiprefix".equalsIgnoreCase(table)) {
                    newTable = "s_gm_apiprefix";
                }
                if (table.contains("t_") || "s_gm_api".equalsIgnoreCase(table) || "s_gm_apiprefix".equalsIgnoreCase(table)) {
                    for (String ntable : getManageTables()) {
                        if (table.equalsIgnoreCase(ntable)) {
                            newPreSchema = PRE_SCHEMA + MANAGE + ".";
                        }
                    }
                }
                if ("verify_deposit".equalsIgnoreCase(table) || "verify_warn".equalsIgnoreCase(table)) {
                    newPreSchema = PRE_SCHEMA + VERIFY + ".";
                }
                sql = sql.replaceAll("\n" + table, "\n" + newPreSchema + newTable);
                sql = sql.replaceAll("\t" + table, "\t" + newPreSchema + newTable);
                sql = sql.replaceAll(" " + table, " " + newPreSchema + newTable);
                sql = sql.replaceAll("," + table, "," + newPreSchema + newTable);
            }
        }
        return sql;
    }

    private List<String> getManageTables() {
        return Lists.newArrayList("t_bs_area", "t_bs_bank",
                "t_bs_financialcode", "t_channel_pay", "t_cp_company",
                "t_cp_site", "t_cp_siteurl", "t_game_logo", "s_gm_api", "s_gm_apiprefix",
                "t_gm_announcement", "t_gm_api", "t_gm_apiprefix",
                "t_gm_cat", "t_gm_code", "t_gm_depot", "t_gm_depotcat", "t_gm_game",
                "t_gm_label", "t_i18n", "t_label_game_depot",
                "t_op_acttmpl", "t_opr_adv", "t_opr_notice",
                "t_opt_adv_banner", "t_pay", "t_pay_bank",
                "t_pay_basic", "t_pay_cryptocurrencies_logo",
                "t_pay_logo", "t_pay_site", "t_restriction",
                "t_schema", "t_sys_config", "t_win_top");
    }

    private List<String> getTables() {
        return Lists.newArrayList("aff_vaildbet", "addMbrNode", "addAgentNode",
                "agy_account", "agy_account_log", "agy_account_memo",
                "agy_account_other", "agy_audit", "agy_bankcard",
                "agy_bill_detail", "agy_commission", "agy_commission_depot",
                "agy_commission_profit", "agy_contract",
                "agy_cryptocurrencies", "agy_department", "agy_deposit",
                "agy_domain", "agy_material", "agy_material_detail",
                "agy_menu", "agy_merchant_detail", "agy_role",
                "agy_role_menu", "agy_sub_account", "agy_sub_menu",
                "agy_time", "agy_token", "agy_tree",
                "agy_channel", "agy_channel_group", "agy_channel_log",
                "agy_user_role", "agy_wallet", "agy_withdraw",
                "fund_acc_log", "fund_acc_withdraw", "fund_audit",
                "fund_deposit", "fund_merchant_detail", "fund_merchant_pay",
                "fund_merchant_scope", "fund_white_list", "help_category", "help_title",
                "help_content", "log_agylogin", "help_guess_ask",
                "log_mbrlogin", "log_mbrregister", "log_system",
                "mbr_account", "mbr_account_callrecord", "mbr_account_device",
                "mbr_account_log", "mbr_account_mobile", "mbr_account_other",
                "mbr_account_time", "mbr_activity_level", "mbr_audit_account",
                "mbr_audit_bonus", "mbr_audit_fraud", "mbr_audit_history",
                "mbr_bankcard", "mbr_bill_detail", "mbr_bill_manage",
                "mbr_collect", "mbr_cryptocurrencies", "mbr_deposit_cond",
                "mbr_deposit_count", "mbr_depot_wallet", "mbr_friend_trans_detail",
                "mbr_group", "mbr_memo", "mbr_message","mbr_label",
                "mbr_message_info", "mbr_opinion", "mbr_promotion",
                "mbr_rebate", "mbr_rebate_agent_bonus", "mbr_rebate_agent_day",
                "mbr_rebate_agent_level", "mbr_rebate_agent_month",
                "mbr_rebate_report_new", "mbr_recently_game",
                "mbr_retrvpw", "mbr_token", "mbr_tree",
                "mbr_validbet", "mbr_wallet", "mbr_withdrawal_cond",
                "mbr_funds_report", "mbr_deposit_lock_log",
                "operation_log", "opr_act_activity", "opr_act_blacklist",
                "opr_act_bonus", "opr_act_cat", "opr_act_catactivity",
                "opr_act_label", "opr_act_lottery", "opr_act_rule",
                "opr_act_water", "opr_act_water_betdate", "opr_adv",
                "opr_adv_image", "opr_gmlabel", "opr_msg",
                "opr_msgrec", "opr_msgrecmbr", "opr_notice",
                "rpt_bet_rcd_day", "s_sys_config", "set_bacic_onlinepay", "set_bacic_onlinePay",
                "set_basic_agencyurl", "set_basic_cryptocurrencies_bank",
                "set_basic_cryptocurrencies_group", "set_basic_domain",
                "set_basic_fastpay", "set_basic_fastpay_group", "set_basic_msgmodel",
                "set_basic_msgmodeltype", "set_basic_paymbrgrouprelation",
                "set_basic_qrcode_bank", "set_basic_qrcode_group",
                "set_basic_set_sys_setting", "set_basic_sys_cryptocurrencies",
                "set_basic_sys_dep_mbr", "set_basic_sys_deposit", "set_basic_sys_qrcode", "set_game",
                "set_gm_game", "sms_config", "sms_log",
                "sys_encrypt", "sys_fileexport_record", "sys_menu",
                "sys_role", "sys_role_menu", "sys_user", "set_small_amount_line",
                "sys_user_agyaccountrelation", "sys_user_mbrgrouprelation", "sys_user_role",
                "sys_user_token", "t_bs_area", "t_bs_bank",
                "t_bs_financialcode", "t_channel_pay", "t_cp_company",
                "t_cp_site", "t_cp_siteurl", "t_game_logo",
                "t_gm_announcement", "t_gm_api", "t_gm_apiprefix",
                "t_gm_cat", "t_gm_code", "t_gm_depot", "t_gm_depotcat", "t_gm_game",
                "t_gm_label", "t_i18n", "t_label_game_depot",
                "t_op_acttmpl", "t_opr_adv", "t_opr_notice",
                "t_opt_adv_banner", "t_pay", "t_pay_bank",
                "t_pay_basic", "t_pay_cryptocurrencies_logo",
                "t_pay_logo", "t_pay_site", "t_restriction",
                "t_schema", "t_sys_config", "t_win_top",
                "task_bonus", "task_config", "task_level",
                "task_signin", "task_statistical", "verify_deposit", "verify_warn","mbr_use_device", "mbr_rebate_friends", "mbr_rebate_friends_reward",
                "mbr_depot_trade","mbr_depot_trade_log","sys_push","sys_warning", "mbr_rebate_hupeng", "mbr_rebate_hupeng_reward", "mbr_warning", "mbr_warning_condition",
                "sys_role_menu_extend", "sys_menu_extend", "mbr_rebate_first_charge_reward","mbr_sys_warning", "mbr_experience"
                );
    }

}
