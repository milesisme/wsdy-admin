-- admin 用户 admin/123123
INSERT INTO `sys_user`(`user_id`, `username`, `password`, `securepwd`, `salt`, `email`, `mobile`, `status`, `dept_id`, `create_time`, `createUserId`, `expireTime`, `realName`, `userMbrGroupAuth`, `userAgyAccountAuth`, `isDelete`, `modifyUserId`, `modifyTime`) 
VALUES (1, 'admin', 'abdb65640d63f23277cf9825c18f68115e129bacce9bff31fae42e2d9f45e966',
'abdb65640d63f23277cf9825c18f68115e129bacce9bff31fae42e2d9f45e966', 'dgRi9Cr9ZVsbFcWQk7kP', 'admin@renren.io', '18688888888', 1, NULL, '2016-11-11 11:11:11', NULL, '2020-03-14 03:49:15', 'eveb管理员', 1, 1, 1, 1, '2018-07-02 09:30:15');

-- 创建管理员角色

INSERT INTO `sys_role`(`role_id`, `role_name`, `remark`, `dept_id`, `create_time`, `role_nickName`, `createUser`, `isEnable`) VALUES (1, '超级管理员', '超级管理员', NULL, '2018-02-02 14:31:35', '超级管理员', 'admin', 1);


-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('1', '0', '系统管理', null, null, '0', 'iconfont icon-systemManage', '14', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('2', '65', '用户管理', '/main/systemManage/roleManage', null, '1', 'fa fa-user', '2', null, null, null, '0', 'sys:user:list');
INSERT INTO `sys_menu` VALUES ('3', '65', '角色权限', '/main/systemManage/roleAuth', null, '1', 'fa fa-user-secret', '1', null, null, null, '0', 'sys:menu:list');
INSERT INTO `sys_menu` VALUES ('15', '2', '查看', null, 'sys:user:list,sys:user:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('16', '2', '新增', null, 'sys:user:save,sys:role:select,agent:account:save', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('17', '2', '编辑', null, 'sys:user:update,sys:role:select', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('18', '2', '删除', null, 'sys:user:delete', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('19', '3', '查看', null, 'sys:role:list,sys:role:info,sys:menu:list,sys:menu:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('20', '3', '新增', null, 'sys:role:save,sys:menu:perms', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('22', '3', '删除', null, 'sys:role:delete', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('51', '0', '会员管理', '', null, '0', 'iconfont icon-memberManage', '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('52', '0', '代理管理', null, null, '0', 'el-icon-share', '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('53', '0', '资金管理', '', null, '0', 'iconfont icon-wageManage', '5', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('55', '0', '记录报表', '', null, '0', 'iconfont icon-businessAnalyze', '7', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('59', '1', '基本设置', null, null, '0', '', '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('65', '1', '权限设置', null, null, '0', '', '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('70', '53', '会员入款', '/main/wageManage/vipPay', null, '1', null, '3', null, null, null, '0', 'fund:onLine:list');
INSERT INTO `sys_menu` VALUES ('71', '70', '查看', null, 'fund:onLine:list,fund:onLine:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('80', '59', '系统设置', '/main/systemManage/systemSetting', null, '1', 'fa fa-file-code-o', '1', null, null, null, '0', 'setting:syssetting:StationSetQry,setting:syssetting:MailSetQry,setting:syssetting:SmsSetQry,setting:syssetting:RegisterSetQry,setting:syssetting:PromotionSetQry,setting:syssetting:PaySetQry,setting:syssetting:FriendTransSetQry');
INSERT INTO `sys_menu` VALUES ('91', '51', '会员组', '/main/memberManage/memberGroup', null, '1', 'fa fa-file-code-o', '2', null, null, null, '0', 'member:mbrgroup:list');
INSERT INTO `sys_menu` VALUES ('103', '102', '查看', null, 'fund:company:list,fund:company:info,setting:sysdeposit:list', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('104', '102', '修改', null, 'fund:company:update', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('106', '51', '会员列表', '/main/memberManage/memberList', 'member:mbraccount:list,member:mbraccount:info', '1', 'fa fa-file-code-o', '1', null, null, null, '0', 'member:mbraccount:list');
INSERT INTO `sys_menu` VALUES ('112', '111', '查看', null, 'log:logmbrlogin:list,log:logmbrlogin:info', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('113', '111', '新增', null, 'log:logmbrlogin:save', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('114', '111', '修改', null, 'log:logmbrlogin:update', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('115', '111', '删除', null, 'log:logmbrlogin:delete', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('117', '116', '查看', null, 'log:logmbrregister:list,log:logmbrregister:info', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('118', '116', '新增', null, 'log:logmbrregister:save', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('119', '116', '修改', null, 'log:logmbrregister:update', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('120', '116', '删除', null, 'log:logmbrregister:delete', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('122', '121', '查看', null, 'log:logsystem:list,log:logsystem:info', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('123', '121', '新增', null, 'log:logsystem:save', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('124', '121', '修改', null, 'log:logsystem:update', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('125', '121', '删除', null, 'log:logsystem:delete', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('147', '53', '会员提款', '/main/wageManage/memberWithdraw', '', '1', 'fa fa-file-code-o', '5', null, null, null, '0', 'fund:accWithdraw:list');
INSERT INTO `sys_menu` VALUES ('148', '147', '查看', null, 'fund:accWithdraw:list,fund:accWithdraw:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('149', '147', '一审', null, 'fund:accWithdraw:update', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('150', '147', '导出', null, 'fund:accWithdraw:exportExcel', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('151', '52', '代理列表', '/main/agentManage/agentList', null, '1', null, '1', null, null, null, '0', 'agent:account:info');
INSERT INTO `sys_menu` VALUES ('152', '151', '代理查看', null, 'agent:account:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('154', '151', '代理删除', null, 'agent:account:delete', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('156', '151', '代理新增', null, 'agent:account:agentSave', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('164', '52', '代理提款', '/main/agentManage/agentWithdrawal', null, '1', null, '3', null, null, null, '0', 'agent:withdraw:list');
INSERT INTO `sys_menu` VALUES ('165', '164', '查看', null, 'agent:withdraw:list,agent:withdraw:info', '2', null, null, null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('166', '164', '初审', null, 'agent:withdraw:firstTrial', '2', null, null, null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('167', '164', '复审', null, 'agent:withdraw:recheck', '2', null, null, null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('170', '844', '信息模板', '/main/systemManage/messageTemplate', null, '1', 'fa fa-file-code-o', '1', null, null, null, '0', 'msgtemple:msgtemple:list');
INSERT INTO `sys_menu` VALUES ('171', '170', '查看', null, 'msgtemple:msgtemple:list,msgtemple:msgtemple:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('173', '170', '状态控制', null, 'msgtemple:msgtemple:available', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('191', '180', '导出', null, 'setting:sysdeposit:exportExcel', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('192', '881', '支付分配', '/main/operateManage/payAllocation', null, '1', 'fa fa-file-code-o', '2', null, null, null, '0', 'setting:allot:list');
INSERT INTO `sys_menu` VALUES ('193', '192', '查看', null, 'setting:allot:list', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('195', '192', '移动', null, 'setting:allot:update', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('196', '192', '编辑', null, 'setting:allot:updateQuota', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('198', '53', '户内转账', '/main/wageManage/transformSheet', null, '1', 'fa fa-file-code-o', '6', null, null, null, '0', 'fund:billReport:list');
INSERT INTO `sys_menu` VALUES ('199', '198', '查看', null, 'fund:billReport:list,fund:billReport:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('200', '198', '新增', null, 'fund:billReport:save', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('209', '53', '资金调整', '/main/wageManage/adjustSheet', null, '1', 'fa fa-file-code-o', '7', null, null, null, '0', 'fund:audit:list');
INSERT INTO `sys_menu` VALUES ('210', '209', '查看', null, 'fund:audit:list,fund:audit:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('211', '209', '新增', null, 'fund:audit:add,fund:audit:reduce', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('212', '209', '审核', null, 'fund:audit:update', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('215', '881', '支付列表', '/main/operateManage/payList', null, '1', 'fa fa-file-code-o', '1', null, null, null, '0', 'setting:company:list,setting:onlinepay:list,setting:fastPay:list');
INSERT INTO `sys_menu` VALUES ('216', '1073', '查看', null, 'setting:company:list', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('217', '1073', '新增', null, 'setting:company:save', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('218', '1073', '编辑', null, 'setting:company:update', '3', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('219', '1073', '删除', null, 'setting:company:delete', '3', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('227', '51', '在线会员', '/main/memberManage/onlineMember', '', '1', 'fa fa-file-code-o', '3', null, null, null, '0', 'member:mbraccount:listOnline');
INSERT INTO `sys_menu` VALUES ('228', '227', '查看', '', 'member:mbraccount:listOnline', '2', '', '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('232', '876', '查看', null, 'operate:tgmcat:gameList,operate:tgmcat:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('233', '844', '公告通知', '/main/operateManage/noticeMessage', null, '1', 'fa fa-file-code-o', '2', null, null, null, '0', 'operate:oprnotice:list');
INSERT INTO `sys_menu` VALUES ('234', '233', '查看', null, 'operate:oprnotice:list,operate:oprnotice:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('235', '233', '新增', null, 'operate:oprnotice:save', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('236', '233', '编辑', null, 'operate:oprnotice:update', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('237', '233', '删除', null, 'operate:oprnotice:delete', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('255', '254', '查看', null, 'operate:oprrecmbr:list,operate:oprrecmbr:info', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('256', '254', '新增', null, 'operate:oprrecmbr:save', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('257', '254', '修改', null, 'operate:oprrecmbr:update', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('258', '254', '删除', null, 'operate:oprrecmbr:delete', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('259', '254', '导出', null, 'operate:oprrecmbr:exportExcel', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('260', '876', '编辑', null, 'operate:tgmcat:updateOrExport', '2', '2', '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('261', '845', '广告管理', '/main/operateManage/adManage', null, '1', 'fa fa-file-code-o', '1', null, null, null, '0', 'operate:opradv:list');
INSERT INTO `sys_menu` VALUES ('262', '261', '查看', null, 'operate:opradv:list,operate:opradv:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('263', '261', '新增', null, 'operate:opradv:save', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('264', '261', '编辑', null, 'operate:opradv:update', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('265', '261', '删除', null, 'operate:opradv:delete', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('289', '55', '投注记录', '/main/operateAnalyze/betRecord', null, '1', null, '7', null, null, null, '0', 'analysis:betDetails:finalBetDetailsAll');
INSERT INTO `sys_menu` VALUES ('296', '55', '输赢报表', '/main/operateAnalyze/loseWinSheet', null, '1', null, '2', null, null, null, '0', 'analysis:betDetails:finalBetDetailsAll');
INSERT INTO `sys_menu` VALUES ('297', '55', '红利报表', '/main/operateAnalyze/dividendSheet', null, '1', null, '3', null, null, null, '0', 'analysis.bouns.view');
INSERT INTO `sys_menu` VALUES ('298', '55', '游戏数据报表', '/main/operateAnalyze/gameDataSheet', null, '1', null, '4', null, null, null, '0', 'analysis:betDetails:finalBetDetailsAll');
INSERT INTO `sys_menu` VALUES ('303', '302', '查看', null, 'merchant:fundMerchantPay:list,merchant:fundMerchantPay:info', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('304', '302', '新增', null, 'merchant:fundMerchantPay:save', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('305', '302', '修改', null, 'merchant:fundMerchantPay:update', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('306', '302', '删除', null, 'merchant:fundMerchantPay:delete', '2', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('309', '297', '查看', null, 'analysis.bouns.view', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('310', '298', '查看', null, 'analysis:betDetails:finalBetDetailsAll', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('311', '296', '查看', null, 'analysis:betDetails:finalBetDetailsAll', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('312', '289', '查看', null, 'analysis:betDetails:finalBetDetailsAll', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('691', '846', '出款管理', '/main/operateManage/withdrawManage', null, '1', 'fa fa-file-code-o', '3', null, null, null, '0', 'merchant:fundMerchantPay:list');
INSERT INTO `sys_menu` VALUES ('692', '691', '查看', null, 'merchant:fundMerchantPay:list,merchant:fundMerchantPay:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('693', '691', '新增', null, 'merchant:fundMerchantPay:save', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('694', '691', '编辑', null, 'merchant:fundMerchantPay:update', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('697', '147', '二审', null, 'fund:accWithdraw:FinialUpdate', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('698', '843', '红利列表', '/main/marketActivity/bonusList', null, '1', null, '2', null, null, null, '0', 'operate:bonus:list');
INSERT INTO `sys_menu` VALUES ('699', '1065', '查看', null, 'operate:activity:activityAuditList', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('700', '1065', '审核', null, 'operate:activity:activityAudit', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('706', '106', '会员详情', null, 'member:mbraccount:list,member:mbraccount:info', '3', null, '1', null, 'memberDetailKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('707', '106', '新增会员', null, 'member:mbraccount:save', '3', null, '2', null, 'addMemberKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('722', '706', '基本资料', null, null, '3', null, '0', null, 'memberDataKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('723', '706', '账户资料', null, null, '3', null, '0', '', 'memberOtherDataKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('724', '706', '风控审核', null, null, '3', null, '0', null, 'memberRiskValidateKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('725', '706', '最新备注', null, 'member:mbrmemo:list,member:mbrmemo:info', '3', null, '0', null, 'memberNewRemarkKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('726', '706', '游戏管理', null, null, '3', null, '0', null, 'memberGameManageKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('727', '706', '资产信息', null, null, '3', null, '0', null, 'memberAssetInfoKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('728', '706', '会员日志', null, null, '3', null, '0', null, 'memberLogKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('729', '706', '重置登陆密码', null, 'member:mbraccount:pwdUpdate', '3', null, '0', null, 'memberResetLoginPwdKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('731', '722', '用户名', null, null, '3', null, '1', 'loginName', 'loginNameKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('732', '722', '姓名', '', '', '3', '', '1', 'realName', 'realNameKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('733', '722', '电话', null, null, '3', null, '1', 'mobile', 'mobileKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('734', '722', '银行卡', null, 'member:mbrbankcard:list,member:mbrbankcard:info', '3', null, '1', null, 'bankCardKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('735', '722', '邮箱', null, null, '3', null, '1', 'email', 'emailKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('736', '722', '微信', null, null, '3', null, '1', 'weChat', 'weChatKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('737', '722', 'QQ', null, null, '3', null, '1', 'qq', 'qqKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('738', '722', '注册时间', null, null, '3', null, '1', 'registerTime', 'registerTimeKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('739', '722', '注册IP', null, null, '3', null, '1', 'registerIp', 'registerIpKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('740', '722', '注册网址', null, null, '3', null, '1', 'registerUrl', 'registerUrlKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('741', '722', '最后登录时间', null, null, '3', null, '1', 'loginTime', 'loginTimeKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('742', '722', '最后登录IP', '', '', '3', '', '1', 'loginIp', 'loginIpKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('743', '723', '主账号余额', null, 'member:mbrwallet:list,member:mbrwallet:info', '3', null, '1', 'totalBalance', 'totalBalanceKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('744', '723', '状态', null, null, '3', null, '1', 'available', 'availableKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('745', '723', '会员组', null, null, '3', null, '1', 'groupId', 'groupIdKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('746', '723', '总代', null, null, '3', null, '1', 'tagencyId', 'tagencyIdKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('747', '722', '是否在线', null, null, '3', null, '1', 'isOnline', 'isOnlineKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('748', '723', '代理', null, null, '3', null, '1', 'cagencyId', 'cagencyIdKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('749', '724', '同注册IP', null, null, '3', null, '5', 'registerIp', 'tRegisterIpKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('751', '724', '同登录IP', null, null, '3', null, '3', 'loginIp', 'tLoginIpKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('752', '724', '同QQ', null, null, '3', null, '2', 'qq', 'tQqKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('753', '724', '同一代理', null, null, '3', null, '1', 'agent', 'tAgentKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('762', '727', '线上入款次数', null, null, '3', null, '1', 'onlinedepositNum', 'onlinedepositNumKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('763', '727', '线上入款金额', null, null, '3', null, '1', 'onlinedepositAmounts', 'onlinedepositAmountsKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('764', '727', '公司入款次数', null, null, '3', null, '1', 'offlinedepositNum', 'offlinedepositNumKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('765', '727', '公司入款金额', null, null, '3', null, '1', 'offlinedepositAmounts', 'offlinedepositAmountsKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('766', '727', '提款次数', null, null, '3', null, '1', 'withDrawNum', 'withDrawNumKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('767', '727', '提款总额', null, null, '3', null, '1', 'withdrawdrawingAmounts', 'withdrawdrawingAmountsKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('768', '727', '红利次数', null, null, '3', null, '1', 'bonusNum', 'bonusNumKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('769', '727', '红利金额', null, null, '3', null, '1', 'bonusAmounts', 'bonusAmountsKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('770', '727', '人工调整次数', null, null, '3', null, '1', 'adjustNum', 'adjustNumKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('771', '727', '人工调整金额', null, null, '3', null, '1', 'amounts', 'amountsKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('772', '727', '总投注额', null, null, '3', null, '1', 'bet', 'betKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('773', '727', '有效投注额', null, null, '3', null, '1', 'validBet', 'validBetKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('774', '727', '总派彩', null, null, '3', null, '1', 'payout', 'payoutKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('788', '728', '资金流水', null, null, '3', null, '1', null, 'capitalBill', null, '0', null);
INSERT INTO `sys_menu` VALUES ('789', '728', '转账记录', '', '', '3', '', '1', null, 'transferRecord', null, '0', null);
INSERT INTO `sys_menu` VALUES ('790', '728', '存款记录', null, null, '3', null, '1', null, 'depositRecord', null, '0', null);
INSERT INTO `sys_menu` VALUES ('791', '728', '提款记录', null, null, '3', null, '1', null, 'withdrawRecord', null, '0', null);
INSERT INTO `sys_menu` VALUES ('792', '728', '红利记录', null, null, '3', null, '1', null, 'bonusRecord', null, '0', null);
INSERT INTO `sys_menu` VALUES ('793', '728', '资料变更', null, null, '3', null, '1', null, 'informationChange', null, '0', null);
INSERT INTO `sys_menu` VALUES ('794', '728', '登录日志', null, null, '3', null, '1', null, 'loginLog', null, '0', null);
INSERT INTO `sys_menu` VALUES ('795', '732', '姓名-修改', null, 'member:mbraccount:update', '3', null, '1', null, 'realNameModifyKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('796', '733', '电话-修改', null, 'member:mbraccount:update', '3', null, '1', null, 'mobileModifyKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('798', '734', '银行卡-删除', null, 'member:mbrbankcard:delete', '3', null, '1', null, 'bankCardDelete', '3', '0', null);
INSERT INTO `sys_menu` VALUES ('799', '735', '邮箱-修改', null, 'member:mbraccount:update', '3', null, '1', null, 'emailModifyKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('800', '736', '微信-修改', null, 'member:mbraccount:update', '3', null, '1', null, 'wechatModifyKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('801', '737', 'QQ-修改', null, 'member:mbraccount:update', '3', null, '1', null, 'qqModifyKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('804', '744', '状态-修改', null, 'member:mbraccount:update,member:mbraccount:avlUpdate', '3', null, '1', null, 'availableModifyKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('805', '745', '会员组-修改', null, 'member:mbraccount:update,member:mbraccount:groupIdUpdate', '3', null, '1', null, 'groupIdModifyKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('808', '706', '发送站内信', null, 'member:mbraccount:sendMsg', '3', null, '0', null, 'memberSendMessageKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('820', '106', '会员名', null, null, '5', null, '0', 'loginName', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('821', '106', '真实姓名', null, null, '5', null, '0', 'realName', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('822', '106', '手机号码', null, null, '5', null, '0', 'mobile', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('823', '106', '邮箱', null, null, '5', null, '0', 'email', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('824', '106', '注册网址', null, null, '5', null, '0', 'registerUrl', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('825', '106', '注册来源', null, null, '5', null, '0', 'registerSource', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('826', '106', '登录来源', null, null, '5', null, '0', 'loginSource', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('827', '106', '总代', null, null, '5', null, '0', 'tagencyId', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('828', '106', '代理', null, null, '5', null, '0', 'cagencyId', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('829', '106', '会员组', null, null, '5', null, '0', 'groupId', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('830', '106', '状态', '', '', '5', '', '0', 'available', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('831', '106', '在线会员', null, null, '5', null, '0', 'isOnline', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('832', '106', 'QQ', null, null, '5', null, '0', 'qq', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('834', '106', '最后登录时间', null, null, '5', null, '0', 'loginTime', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('835', '106', '人工增加调整', null, 'fund:audit:mbradd', '3', null, '6', null, 'memberManualAddKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('836', '106', '人工减少调整', null, 'fund:audit:mbrreduce', '3', null, '7', null, 'memberManualReduceKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('841', '55', '账变流水', '/main/operateAnalyze/balanceRecord', null, '1', null, '10', null, null, null, '0', 'fund:billReport:list');
INSERT INTO `sys_menu` VALUES ('843', '0', '市场活动', null, null, '0', 'iconfont icon-activityManage', '9', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('844', '0', '公告管理', null, null, '0', 'iconfont icon-tellManage', '11', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('845', '0', '广告管理', null, null, '0', 'iconfont icon-adManege', '12', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('846', '0', '支付配置', null, null, '0', 'iconfont icon-paySet', '13', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('849', '706', '会员完整信息', null, 'member:mbraccount:contact', '3', null, '0', null, 'memberFullRelationKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('852', '849', '电话', null, 'member:mbraccount:contact:mobile', '3', null, '0', null, 'fullRelationMobile', '1', '0', null);
INSERT INTO `sys_menu` VALUES ('853', '849', '邮箱', null, 'member:mbraccount:contact:email', '3', null, '0', null, 'fullRelationEmail', '1', '0', null);
INSERT INTO `sys_menu` VALUES ('854', '849', '微信', null, 'member:mbraccount:contact:wechat', '3', null, '0', null, 'fullRelationWechat', '1', '0', null);
INSERT INTO `sys_menu` VALUES ('855', '849', 'QQ', null, 'member:mbraccount:contact:qq', '3', null, '0', null, 'fullRelationQq', '1', '0', null);
INSERT INTO `sys_menu` VALUES ('857', '725', '最新备注-新增', null, 'member:mbrmemo:save', '3', null, '1', null, null, '1', '0', null);
INSERT INTO `sys_menu` VALUES ('867', '55', '稽核报表', '/main/operateAnalyze/checkSheet', '', '1', null, '1', null, null, null, '0', 'member:audit:list');
INSERT INTO `sys_menu` VALUES ('868', '867', '查看稽核', null, 'member:audit:list', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('869', '867', '修改稽核', null, 'member:audit:update', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('870', '867', '清除稽核', null, 'member:audit:clear', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('875', '0', '游戏管理', null, null, '0', 'iconfont icon-gameManage', '10', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('876', '875', '游戏列表', '/main/gameManage/gameList', null, '1', 'fa fa-file-code-o', '1', null, null, null, '0', 'operate:tgmcat:gameList');
INSERT INTO `sys_menu` VALUES ('877', '1075', '查看', null, 'setting:onlinepay:list', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('878', '1075', '新增', null, 'setting:onlinepay:save', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('879', '1075', '编辑', null, 'setting:onlinepay:update', '3', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('880', '1075', '删除', null, 'setting:onlinepay:delete', '3', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('881', '846', '入款管理', null, null, '0', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('882', '55', '资金报表', '/main/operateAnalyze/moneySheet', 'analysis:fundReport:list', '1', null, '2', null, null, null, '0', 'analysis:fundReport:list');
INSERT INTO `sys_menu` VALUES ('884', '883', '查看', null, 'setting:commission:info', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('886', '59', '代理设置', '/main/systemManage/ProxySetting', null, '1', null, '3', null, null, null, '0', 'setting:agent:info,setting:agent:info2');
INSERT INTO `sys_menu` VALUES ('887', '1081', '编辑', null, 'setting:agent:register', '3', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('888', '1082', '编辑', null, 'setting:agent:commission', '3', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('889', '1081', '查看', null, 'setting:agent:info', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('890', '151', '状态控制', null, 'agent:account:available', '2', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('894', '151', '代理编辑', null, 'agent:account:update', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('903', '151', '重置登录密码', null, 'agent:account:password', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('907', '1074', '查看', null, 'setting:fastPay:list', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('908', '1074', '新增', null, 'setting:fastPay:save', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('909', '1074', '编辑', null, 'setting:fastPay:update', '3', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('910', '1074', '删除', null, 'setting:fastPay:delete', '3', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('914', '722', '推广链接', null, null, '3', null, '1', '', 'promotionUrlKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('915', '722', '推荐人数', null, null, '3', null, '1', 'promotionNum', 'promotionNumKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('916', '722', '推荐人', null, null, '3', null, '1', 'referrer', 'referrerKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('919', '1', '日志监控', null, null, '0', '', '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('920', '919', '操作日志', '/main/systemManage/logMonitor', 'sys:log:list', '1', 'fa fa-user-secret', '1', null, null, null, '0', 'sys:log:list');
INSERT INTO `sys_menu` VALUES ('921', '106', '批量转移会员组', null, 'member:mbraccount:changeMbrGroup', '3', null, '8', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('923', '843', '返利列表', '/main/operateManage/rebateList', null, '1', null, '3', null, null, null, '0', 'member:rebate:list');
INSERT INTO `sys_menu` VALUES ('924', '923', '查看', null, 'member:rebate:list', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('926', '844', '会员留言', '/main/operateManage/messageBoard', null, '1', 'fa fa-file-code-o', '4', null, null, null, '0', 'member:message:list');
INSERT INTO `sys_menu` VALUES ('927', '926', '查看', null, 'member:message:list', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('928', '926', '回复', null, 'member:message:send', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('935', '53', '好友转账', '/main/wageManage/friendTransfer', null, '1', 'fa fa-file-code-0', '8', null, null, null, '0', 'member:mbrfreindtransdetail:list');
INSERT INTO `sys_menu` VALUES ('936', '935', '查看', null, 'member:mbrfreindtransdetail:list,member:mbrfreindtransdetail:info', '2', null, null, null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('941', '51', '活动等级', '/main/memberManage/memberLevel', null, '1', 'fa fa-file-code-o', '4', null, null, null, '0', 'member:mbractlevel:list');
INSERT INTO `sys_menu` VALUES ('942', '941', '自动晋升', null, 'member:mbractlevel:setAutomatic', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('947', '843', '活动类别', '/main/marketActivity/activityCategory', '', '1', null, '4', null, null, null, '0', 'operate:activitycat:list');
INSERT INTO `sys_menu` VALUES ('948', '947', '查看', '', 'operate:activitycat:list', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('949', '947', '新增', '', 'operate:activitycat:save', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('950', '947', '编辑', '', 'operate:activitycat:update', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('951', '947', '删除', '', 'operate:activitycat:delete', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('953', '952', '查看', null, 'agent:domain:info', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('954', '952', '审核', null, 'agent:domain:audit', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('955', '952', '新增', null, 'agent:domain:save', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('957', '734', '银行卡-状态', null, 'member:mbrbankcard:available', '3', null, '0', null, null, '2', '0', null);
INSERT INTO `sys_menu` VALUES ('958', '106', '导出', null, 'member:mbraccount:export', '3', null, '9', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('959', '106', '注册IP', null, null, '5', null, null, 'registerIp', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('960', '106', '登录IP', null, null, '5', null, null, 'loginIp', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('962', '106', '呼叫', null, 'member:mbraccount:call', '3', null, '4', null, 'memberCall', null, '0', null);
INSERT INTO `sys_menu` VALUES ('963', '106', '呼叫记录', null, 'member:mbraccount:callrecord', '3', null, '5', null, 'memberCallrecord', null, '0', null);
INSERT INTO `sys_menu` VALUES ('964', '843', '活动规则', '/main/marketActivity/activityRule', null, '1', null, '5', null, null, null, '0', 'operate:activity:rulelist');
INSERT INTO `sys_menu` VALUES ('965', '964', '查看', null, 'operate:activity:rulelist,operate:activity:ruleinfo', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('966', '964', '新增', null, 'operate:activity:saveRule', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('967', '964', '编辑', null, 'operate:activity:updateRule', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('968', '964', '状态控制', null, 'operate:activity:availableRule', '2', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('969', '70', '导出', null, 'fund:deposit:exportExcel', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('971', '289', '导出', null, 'analysis:betDetails:exportExcel', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('972', '843', '活动介绍', '/main/marketActivity/activityIntroduction', null, '1', null, '6', null, null, null, '0', 'operate:activity:list');
INSERT INTO `sys_menu` VALUES ('973', '972', '查看', null, 'operate:activity:list,operate:activity:info', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('974', '972', '编辑', null, 'operate:activity:update', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('975', '972', '新增', null, 'operate:activity:save', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('976', '947', '状态控制', null, 'operate:activitycat:updateAvailable', '2', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('977', '723', '会员星级', null, 'member:activitylevel:info', '3', null, '1', 'actLevelId', 'actLevelId', null, '0', null);
INSERT INTO `sys_menu` VALUES ('978', '723', '锁定会员等级', null, 'member:activitylevel:update', '3', null, '1', '', null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('979', '1065', '新增', null, 'operate:bonus:save', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('981', '977', '会员星级-修改', null, 'member:activitylevel:modify,member:mbraccount:update', '3', null, '1', null, 'actLevelModifyKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('982', '843', '返水列表', '/main/marketActivity/waterRebateList', null, '1', null, '2', null, null, null, '0', 'operate:activity:waterAuditList');
INSERT INTO `sys_menu` VALUES ('983', '982', '查看', null, 'operate:activity:waterAuditList', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('984', '1066', '审核', null, 'operate:activity:waterAudit', '3', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('985', '967', '删除', null, 'operate:activity:deleteDisableRule', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('987', '106', '批量调级', null, 'member:mbraccount:batchUpdateActLevel', '3', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('988', '698', '查看', null, 'operate:bonus:list', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('989', '106', '会员星级', null, null, '5', null, '0', 'actLevelId', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('992', '722', '注册来源', null, null, '3', null, '1', 'registerSource', 'registerSourceKey', null, '0', null);
INSERT INTO `sys_menu` VALUES ('994', '882', '存款渠道统计', null, 'fund:onLine:depositStatisticByPay', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('996', '296', '导出', null, 'analysis:betDetails:exportMbrWinLoseInfo', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('997', '151', '关联会员组', null, 'agent:account:updateAgyMerGroup', '2', null, '7', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('998', '80', '站点设置', null, '', '2', '', '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1000', '80', '短信设置', null, '', '2', '', '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1001', '80', '注册设置', null, '', '2', '', '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1002', '80', '推广设置', null, '', '2', '', '5', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1003', '80', '出入款设置', null, '', '2', '', '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1004', '80', '好友转账设置', null, '', '2', '', '7', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1005', '998', '查看', null, 'setting:syssetting:StationSetQry', '3', null, '0', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1006', '998', '编辑', null, 'setting:syssetting:StationSetEdit', '3', null, '1', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1009', '1000', '查看', null, 'setting:syssetting:SmsSetQry', '3', null, '0', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1010', '1000', '编辑', null, 'setting:syssetting:SmsSetEdit', '3', null, '1', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1011', '1001', '查看', null, 'setting:syssetting:RegisterSetQry', '3', null, '0', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1012', '1001', '编辑', null, 'setting:syssetting:RegisterSetEdit', '3', null, '1', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1013', '1002', '查看', null, 'setting:syssetting:PromotionSetQry', '3', null, '0', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1014', '1002', '编辑', null, 'setting:syssetting:PromotionSetEdit', '3', null, '1', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1015', '1003', '查看', null, 'setting:syssetting:PaySetQry', '3', null, '0', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1016', '1003', '编辑', null, 'setting:syssetting:PaySetEdit', '3', null, '1', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1017', '1004', '查看', null, 'setting:syssetting:FriendTransSetQry', '3', null, '0', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1018', '1004', '编辑', null, 'setting:syssetting:FriendTransSetEdit', '3', null, '1', null, '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1019', '70', '审核', null, 'fund:onLine:updateStatus', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1020', '106', '会员列表查看', null, null, '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1021', '1020', '会员名', null, null, '3', null, '0', 'loginName', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1022', '1020', '真实姓名', '', '', '3', '', '0', 'realName', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1023', '1020', '代理', null, null, '3', null, '0', 'cagencyId', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1024', '1020', '会员组', null, null, '3', null, '0', 'groupId', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1025', '1020', '总余额', null, null, '3', null, '0', 'totalBalance', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1026', '1020', '注册时间', null, null, '3', null, '0', 'registerTime', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1028', '1020', '最后登录时间', null, null, '3', null, '0', 'loginTime', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1029', '1020', '状态', null, null, '3', null, '0', 'available', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1030', '1020', '已推荐人数', null, null, '3', null, '0', 'promotionNum', null, null, '1', null);
INSERT INTO `sys_menu` VALUES ('1031', '1020', '会员星级', null, null, '3', null, '0', 'actLevelId', '', null, '1', null);
INSERT INTO `sys_menu` VALUES ('1033', '722', '区域查看', null, 'member:basicinfo:view', '3', null, '0', '', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1034', '723', '区域查看', null, 'member:basicinfo:view2', '3', null, '0', '', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1035', '724', '区域查看', null, 'member:basicinfo:view3', '3', null, '0', '', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1036', '727', '区域查看', null, 'member:basicinfo:view4', '3', null, '0', '', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1037', '728', '区域查看', null, 'member:basicinfo:view5', '3', null, '0', '', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1038', '91', '会员组列表', null, '', '2', null, '0', '', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1039', '91', '取款条件', null, '', '2', null, '0', '', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1040', '91', '存款条件', null, '', '2', null, '0', '', '', null, '0', null);
INSERT INTO `sys_menu` VALUES ('1042', '1038', '查看', null, 'member:mbrgroup:list,member:mbrgroup:info', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1043', '1038', '新增', null, 'member:mbrgroup:save', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1044', '1038', '修改', null, 'member:mbrgroup:update', '3', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1045', '1038', '删除', null, 'member:mbrgroup:delete', '3', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1046', '1039', '查看', null, 'member:mbrwithdrawalcond:info', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1047', '1039', '新增', null, 'member:mbrwithdrawalcond:save', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1048', '1039', '修改', null, 'member:mbrwithdrawalcond:update', '3', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1049', '1040', '查看', null, 'member:mbrdepositcond:info', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1050', '1040', '新增', null, 'member:mbrdepositcond:save', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1051', '1040', '修改', null, 'member:mbrdepositcond:update', '3', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1054', '941', '活动等级列表', null, '', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1055', '1054', '查看', null, 'member:mbractlevel:list,member:mbractlevel:info', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1056', '1054', '编辑', null, 'member:mbractlevel:update', '3', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1057', '941', '周期统计规则', null, '', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1058', '1057', '查看', null, 'member:mbractlevel:qryStaticsRule', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1059', '1057', '编辑', null, 'member:mbractlevel:setStaticsRule', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1060', '151', '会员新增', null, 'agent:account:addmbr', '2', null, '5', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1061', '151', '推广域名删除', null, 'agent:domain:delete', '2', null, '8', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1062', '198', '刷新', null, 'fund:billReport:add', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1063', '882', '报表查看', null, 'analysis:fundReport:list', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1064', '841', '报表查看', null, 'fund:billReport:list', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1065', '698', '发放记录', null, '', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1066', '982', '发放记录', null, '', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1067', '1066', '查看', null, 'operate:activity:waterAuditListIssue', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1068', '964', '删除', null, 'operate:activity:deleteDisableRule', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1069', '876', '状态控制', null, 'operate:tgmcat:available', '2', null, '2', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1070', '233', '状态控制', null, 'operate:oprnotice:available', '2', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1071', '926', '新建', null, 'member:message:newSend', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1072', '261', '状态控制', null, 'operate:opradv:available', '2', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1073', '215', '银行卡', null, 'setting:company:all', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1074', '215', '自动入款平台', null, 'setting:fastPay:all', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1075', '215', '线上支付', null, 'setting:onlinepay:all', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1076', '1073', '状态控制', null, 'setting:company:available', '3', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1077', '1074', '状态控制', null, 'setting:fastPay:available', '3', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1079', '1075', '状态控制', null, 'setting:onlinepay:available', '3', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1080', '691', '状态控制', null, 'merchant:fundMerchantPay:available', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1081', '886', '代理注册', null, '', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1082', '886', '代理佣金', null, '', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1083', '1082', '查看', null, 'setting:agent:info2', '3', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1084', '3', '编辑', null, 'sys:role:modify', '2', null, '3', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1085', '3', '状态控制', null, 'sys:role:available', '2', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1086', '2', '状态控制', null, 'sys:user:available', '2', null, '4', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1087', '2', '登录密码修改', null, 'sys:user:modifypwd', '2', null, '5', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1088', '2', '安全密码修改', null, 'sys:user:modifysecurepwd', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1089', '920', '查看', null, 'sys:log:list', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1090', '964', '发放黑名单', null, 'operate:activity:getActBlacklist', '2', null, '5', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1091', '964', '自助返水控制', null, 'operate:activity:setSelfHelp', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1092', '1090', '黑名单修改', null, 'operate:activity:updateActBlacklist', '2', null, '0', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1093', '227', '踢线', null, 'member:mbraccount:kickLine', '2', null, '6', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1094', '732', '姓名-查看', null, '', '3', null, '0', 'realName', 'realNameKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('1095', '733', '电话-查看', null, '', '3', null, '0', 'mobile', 'mobileKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('1096', '734', '银行卡-查看', null, 'member:mbrbankcard:list,member:mbrbankcard:info', '3', null, '0', null, 'bankCardKey', '3', '0', null);
INSERT INTO `sys_menu` VALUES ('1097', '735', '邮箱-查看', null, '', '3', null, '0', 'email', 'emailKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('1098', '736', '微信-查看', null, '', '3', null, '0', 'weChat', 'weChatKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('1099', '737', 'QQ-查看', null, '', '3', null, '0', 'qq', 'qqKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('1100', '744', '状态-查看', null, '', '3', null, '0', 'available', 'availableKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('1101', '745', '会员组-查看', null, '', '3', null, '0', 'groupId', 'groupIdKey', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('1102', '977', '会员星级-查看', null, 'member:activitylevel:info', '3', null, '0', 'actLevelId', 'actLevelId', '2', '0', null);
INSERT INTO `sys_menu` VALUES ('1103', '725', '最新备注-查看', null, 'member:mbrmemo:list,member:mbrmemo:info', '3', null, '0', null, 'memberNewRemarkKey', '1', '0', null);
INSERT INTO `sys_menu` VALUES ('1104', '170', '编辑', null, 'msgtemple:msgtemple:update', '2', null, '1', null, null, null, '0', null);
INSERT INTO `sys_menu` VALUES ('1105', '106', '注册时间', NULL, NULL, '5', NULL, '0', 'registerTime', NULL, NULL, '0', NULL);
INSERT INTO `sys_menu` VALUES ('1106', '881', '线路统计', '/main/operateManage/lineStatistics', NULL, '1', 'fa fa-file-code-o', '3', NULL, NULL, NULL, '0', 'setting:deposit:statisticSucRate');
INSERT INTO `sys_menu` VALUES ('1107', '1106', '查看', NULL, 'setting:deposit:statisticSucRate', '3', NULL, '0', NULL, NULL, NULL, '0', NULL);
INSERT INTO `sys_menu` VALUES ('1108', '297', '导出', NULL, 'analysis:bouns:export', '2', NULL, '2', NULL, NULL, NULL, '0', NULL);
INSERT INTO `sys_menu` VALUES ('1109', '209', '导出', NULL, 'fund:audit:export', '2', NULL, '1', NULL, NULL, NULL, '0', NULL);
INSERT INTO `sys_menu` VALUES ('1110', '106', '群发短信', NULL, 'member:mbraccount:massTexting', '2', NULL, '3', NULL, NULL, NULL, '0', NULL);
INSERT INTO `sys_menu` VALUES ('1111', '706', '修改代理', NULL, 'member:mbraccount:updateAccountAgent', '2', NULL, '0', NULL, NULL, NULL, '0', NULL);
INSERT INTO `sys_menu` VALUES ('1112', '849', '真实姓名', NULL, 'member:mbraccount:contact:realname', '3', NULL, '0', NULL, 'fullRelationRealname', '1', '0', NULL);
INSERT INTO `sys_menu` VALUES ('1113', '106', '推荐好友', NULL, NULL, '5', NULL, '0', 'supLoginName', NULL, NULL, '0', NULL);
INSERT INTO `sys_menu` VALUES (1114, 843, '任务中心', '/main/marketActivity/taskCenter', NULL, 1, NULL, 9, NULL, NULL, NULL, 0, 'task:account:configList');
INSERT INTO `sys_menu` VALUES (1115, 1114, '启用状态', NULL, 'task:account:updateAvailable', 2, NULL, 3, NULL, NULL, NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1116, 1114, '编辑规则', NULL, 'task:account:updateTask', 2, NULL, 2, NULL, NULL, NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1117, 1114, '黑名单查询', NULL, 'task:account:blacklist', 2, NULL, 4, NULL, NULL, NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1118, 1114, '黑名单删除', NULL, 'task:account:deleteBlacklist', 2, NULL, 4, NULL, NULL, NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1119, 1114, '黑名单新增', NULL, 'task:account:addBlacklist', 2, NULL, 4, NULL, NULL, NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1120, 1114, '查看', NULL, 'task:account:configList', 2, NULL, 1, NULL, NULL, NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1121, 843, '任务中心统计', '/main/marketActivity/taskStatistics', '', 1, NULL, 11, NULL, '', NULL, 0, 'task:account:bounsList');
INSERT INTO `sys_menu` VALUES (1122, 1121, '查看', NULL, 'task:account:bounsList', 2, NULL, 7, NULL, NULL, NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1123, 1121, '领取记录', NULL, 'task:account:bounsdetail', 2, NULL, 1, NULL, NULL, NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1124, 215, '普通扫码支付', NULL, 'setting:qrcodepay:all', 2, NULL, 0, NULL, NULL, NULL, 0, 'setting:company:list,setting:onlinepay:list,setting:fastPay:list,setting:qrcodepay:list');
INSERT INTO `sys_menu` VALUES (1125, 1124, '查看', NULL, 'setting:qrcodepay:list', 3, NULL, 0, NULL, NULL, NULL, 0, 'setting:company:list,setting:onlinepay:list,setting:fastPay:list,setting:qrcodepay:list');
INSERT INTO `sys_menu` VALUES (1126, 1124, '新增', NULL, 'setting:qrcodepay:save', 3, NULL, 1, NULL, NULL, NULL, 0, 'setting:company:list,setting:onlinepay:list,setting:fastPay:list,setting:qrcodepay:list');
INSERT INTO `sys_menu` VALUES (1127, 1124, '编辑', NULL, 'setting:qrcodepay:update', 3, NULL, 3, NULL, NULL, NULL, 0, 'setting:company:list,setting:onlinepay:list,setting:fastPay:list,setting:qrcodepay:list');
INSERT INTO `sys_menu` VALUES (1128, 1124, '删除', NULL, 'setting:qrcodepay:delete', 3, NULL, 2, NULL, NULL, NULL, 0, 'setting:company:list,setting:onlinepay:list,setting:fastPay:list,setting:qrcodepay:list');
INSERT INTO `sys_menu` VALUES (1129, 1124, '状态控制', NULL, 'setting:qrcodepay:available', 3, NULL, 4, NULL, NULL, NULL, 0, 'setting:company:list,setting:onlinepay:list,setting:fastPay:list,setting:qrcodepay:list');
INSERT INTO `sys_menu` VALUES (1130, 706, '验证信息', NULL, 'member:mbraccount:chkUserInfo', 2, NULL, 0, NULL, 'chkUserInfo', NULL, 0, NULL);
INSERT INTO `sys_menu` VALUES (1131, 977, '会员星级-日志查看', NULL, 'member:mbraccount:accountAutoLog', 3, NULL, 1, NULL, '', 2, 0, NULL);
INSERT INTO `sys_menu` VALUES (1131, 725, '最新备注-删除', NULL, 'member:mbrmemo:delete', '3', NULL, '2', NULL, '', '1', '0', NULL);
INSERT INTO `sys_menu` VALUES (1132, 725, '最新备注-删除', NULL, 'member:mbrmemo:delete', 3, NULL, 2, NULL, '', 1, 0, NULL);
INSERT INTO `sys_menu` VALUES (1133, 106, '推荐好友数范围', NULL, NULL, 5, NULL, 0, 'promotionNum', NULL, NULL, 0, '');
INSERT INTO `sys_menu` VALUES (1134, 106, '余额范围', NULL, NULL, 5, NULL, 0, 'accountBalance', NULL, NULL, 0, '');

-- 生产默认会员组
INSERT INTO `mbr_group`(`id`, `groupName`, `totalDeposit`, `totalBet`, `expandWay`, `memo`, `available`, `isDef`) VALUES (1, '默认组', 5000.00, 20000.00, 1, '默认组', 1, 1);
-- 角色赋值权限

 INSERT INTO sys_role_menu(role_id,menu_id,isTotalChecked) SELECT 1,menu_id,1 FROM sys_menu;
 INSERT INTO `sys_user_role`( `user_id`, `role_id`) VALUES ( 1, 1);

-- 角色赋值数据权限
INSERT INTO `sys_user_mbrGroupRelation`( `mbrGroupId`, `userId`) VALUES ( 1, 1);
INSERT INTO `sys_user_agyAccountRelation`( `userId`, `agyAccountId`, `agyAccountType`, `disabled`) VALUES ( 1, 1, 0, 1);
INSERT INTO `sys_user_agyAccountRelation`( `userId`, `agyAccountId`, `agyAccountType`, `disabled`) VALUES ( 1, 4, 0, 1);

 -- 测试代理数据
INSERT INTO `agy_account`(`id`, `agyAccount`, `agyPwd`, `securePwd`, `salt`, `realName`, `mobile`, `email`, `memo`, `available`, `parentId`, `qq`, `weChat`, `registerUrl`, `registerSign`, `createUser`, `createTime`, `modifyUser`, `modifyTime`, `ip`, `spreadCode`, `commissionId`, `status`) VALUES (1, 'GeneralAgent', '', NULL, NULL, 'Defaultagent', '88888888', 'Defaultagent@qq.com', '系统默认总代', 1, 0, NULL, NULL, NULL, NULL, 'admin', '2017-10-18 21:10:53', 'admin', '2017-10-21 12:59:18', NULL, NULL, NULL, 1);
INSERT INTO `agy_account`(`id`, `agyAccount`, `agyPwd`, `securePwd`, `salt`, `realName`, `mobile`, `email`, `memo`, `available`, `parentId`, `qq`, `weChat`, `registerUrl`, `registerSign`, `createUser`, `createTime`, `modifyUser`, `modifyTime`, `ip`, `spreadCode`, `commissionId`, `status`,`groupid`) VALUES (2, 'DefaultAgent', '', NULL, NULL, 'Generalagent', '99999999', 'Generalagent@qq.com', '系统默认代理', 1, 1, NULL, NULL, NULL, NULL, 'admin', '2017-10-18 21:33:48', 'admin', '2017-10-21 12:58:52', NULL, NULL, NULL, 1,1);
INSERT INTO `agy_account`(`id`, `agyAccount`, `agyPwd`, `securePwd`, `salt`, `realName`, `mobile`, `email`, `memo`, `available`, `parentId`, `qq`, `weChat`, `registerUrl`, `registerSign`, `createUser`, `createTime`, `modifyUser`, `modifyTime`, `ip`, `spreadCode`, `commissionId`, `status`,`groupid`) VALUES (3, 'DefaultTest', '', NULL, NULL, 'DefaultTest', '123123', 'DefaultTest@qq.com', '系统默认测试代理', 1, 4, NULL, NULL, NULL, NULL, 'admin', '2017-10-19 09:36:07', 'admin', '2017-10-22 00:58:53', NULL, NULL, NULL, 1,1);
INSERT INTO `agy_account`(`id`, `agyAccount`, `agyPwd`, `securePwd`, `salt`, `realName`, `mobile`, `email`, `memo`, `available`, `parentId`, `qq`, `weChat`, `registerUrl`, `registerSign`, `createUser`, `createTime`, `modifyUser`, `modifyTime`, `ip`, `spreadCode`, `commissionId`, `status`) VALUES (4, 'TestAgent', '', NULL, NULL, 'TestAgent', '2344234', '234@qq.com', '系统默认测试总代', 1, 0, NULL, NULL, NULL, NULL, 'admin', '2017-10-19 09:40:16', 'admin', '2017-10-22 00:58:55', NULL, NULL, NULL, 1);

-- ----------------------------
-- Records of set_basic_set_sys_setting
-- ----------------------------
-- 推广域名需要配置成前端站点id
INSERT INTO `set_basic_set_sys_setting` VALUES ('accountPromotion', '', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentPromotion', '', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('accWebRegister', '1', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('actLevelStaticsRule', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('address', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentAddAccount', '1', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentAddress', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentAddSub', '1', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentCaptchareg', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentDisplayTermsOfWebsite', '1', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentDomainAnalysisSite', 'gf.nbl.aligatao.com', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentEmail', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentLoginName', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentLoginPwd', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentMobile', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentMobileCaptchareg', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentQQ', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentRealName', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentReLoginPwd', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentRgister', '1', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentServiceTermsOfWebsite', null, 'agentTerms');
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentSysRgister', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('agentWechat', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('autoDeleteDays', '7', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('automaticPromotion', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('captchareg', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('characterSet', 'true', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('configCodeMb', '请自定义', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('configCodePc', '请自定义', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('defaultQueryDays', '7', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('email', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('freeWalletSwitch', '1', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('friendTransAutomatic', '1', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('friendTransMaxAmount', '100000', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('loginName', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('loginPwd', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('logoPath', '', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('mailPassword', '123123', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('mailSendAccount', 'admin@qq.com', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('mailSendPort', '456', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('mailSendServer', 'smtp.gmail.com', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('memberDisplayTermsOfWebsite', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('memberQueryDays', '7', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('memberServiceTermsOfWebsite', '', '请自行配置');
INSERT INTO `set_basic_set_sys_setting` VALUES ('mobile', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('mobileCaptchareg', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('passwordExpireDays', '30', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('payAutomatic', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('payDraw', '1', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('payMoney', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('qq', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('realName', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('reLoginPwd', '2', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('smsGetwayAddress', '请自行配置', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('smsInterfaceName', '请自行配置', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('smsInterfacePassword', '请自行配置', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('smsMobileCompelBind', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('smsSendName', '请自行配置', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('smsTemplate', '您的验证码是：{0}。请不要把验证码泄露给其他人。', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('titlePath', '', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('websiteCodeMb', '请自行配置', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('websiteCodePc', '请自行配置', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('websiteDescription', '123', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('websiteKeywords', '1212', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('websiteTitle', '请自行配置', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('weChat', '0', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('wetherSsl', 'false', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('smsPlatform', '2', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('actLevelStaticsRuleDescription', '待站点管理员完善', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('registerMethod', '0', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('rebateCastDepth', '3', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('egSanGongFlg', '0', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('outCallPlatform', '1', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('depositCondition', '[]', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('withdrawCondition', '[]', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('massTextFlag', '0', NULL);
INSERT INTO `set_basic_set_sys_setting` VALUES ('configCodePc1', '', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('configCodeMb1', '', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('configTelegram', '', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('configSkype', '', null);
INSERT INTO `set_basic_set_sys_setting` VALUES ('configFlygram', '', null);
-- ----------------------------
-- Records of set_basic_msgmodel
-- ----------------------------
INSERT INTO `set_basic_msgmodel` VALUES ('15', '玩家注册成功', '1', '<p>亲爱的会员：#{loginName}，恭喜您注册成功！我们网站有上百种好玩的游戏，您可以一试身手！</p>', '<p>亲爱的会员：#{loginName}，恭喜您注册成功！我们网站有上百种好玩的游戏，您可以一试身手！</p>', '<p>亲爱的会员：#{loginName}，恭喜您注册成功！我们网站有上百种好玩的游戏，您可以一试身手！</p>', '1', 'admin', '2018-02-28 16:52:47', '2019-05-23 15:24:04', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('16', '修改会员资料', '3', '<p>您的资料修改已完成，请牢记修改后的资料。若有疑问，请联系在线客服咨询。</p>', '<p>您的资料修改已完成，请牢记修改后的资料。若有疑问，请联系在线客服咨询。</p>', '<p>您的资料修改已完成，请牢记修改后的资料。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-02-28 16:54:06', '2019-05-06 16:58:20', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('17', '强制踢出会员', '4', '<p>您的登录信息已被强制注销！若有疑问，请联系在线客服咨询。</p>', '<p>您的登录信息已被强制注销！若有疑问，请联系在线客服咨询。</p>', '<p>您的登录信息已被强制注销！若有疑问，请联系在线客服咨询。</p>', '2', 'admin', '2018-02-28 16:54:38', '2019-05-10 15:03:20', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('19', '在线支付成功', '6', '<p>恭喜您，您的在线支付操作已成功，存款金额:#{despoitMoney}元，订单号:#{orderNum}。</p>', '<p>恭喜您，您的在线支付操作已成功，存款金额:#{despoitMoney}元，订单号:#{orderNum}。</p>', '<p>恭喜您，您的在线支付操作已成功，存款金额:#{despoitMoney}元，订单号:#{orderNum}。</p>', '1', 'admin', '2018-02-28 16:55:49', '2018-02-28 16:55:49', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('20', '会员升级成功', '2', '<p>恭喜您,您的会员等级已提升！若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您,您的会员等级已提升！若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您,您的会员等级已提升！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 11:09:26', '2018-03-13 11:09:26', '0', '0', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('22', '会员账号冻结', '5', '<p>您的账号#{loginName}于#{date}起被禁止登录！若有疑问，请联系在线客服咨询。</p>', '<p>您的账号#{loginName}于#{date}起被禁止登录！若有疑问，请联系在线客服咨询。</p>', '<p>您的账号#{loginName}于#{date}起被禁止登录！若有疑问，请联系在线客服咨询。</p>', '2', 'admin', '2018-03-13 13:19:36', '2019-05-17 10:43:58', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('24', '存款审核成功', '7', '<p>您的存款#{despoitMoney}元的操作已完成，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>您的存款#{despoitMoney}元的操作已完成，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>您的存款#{despoitMoney}元的操作已完成，请查看。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:05:13', '2018-03-13 15:05:13', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('25', '存款审核失败', '8', '<p>您的存款#{despoitMoney}元的操作失败，请核对后再试。若有疑问，请联系在线客服咨询。</p>', '<p>您的存款#{despoitMoney}元的操作失败，请核对后再试。若有疑问，请联系在线客服咨询。</p>', '<p>您的存款#{despoitMoney}元的操作失败，请核对后再试。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:06:11', '2018-03-13 15:06:11', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('26', '优惠审核成功', '9', '<p>恭喜您，您的优惠申请(金额RMB:#{acvitityMoney}元,优惠名称:#{acvitityName})已派发至您的账户余额，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的优惠申请(金额RMB:#{acvitityMoney}元,优惠名称:#{acvitityName})已派发至您的账户余额，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的优惠申请(金额RMB:#{acvitityMoney}元,优惠名称:#{acvitityName})已派发至您的账户余额，请查看。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:07:45', '2018-03-13 15:07:45', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('27', '优惠审核失败', '10', '<p>您的优惠申请(金额RMB:#{acvitityMoney}元,优惠名称:#{acvitityName})被拒绝！若有疑问，请联系在线客服咨询。</p>', '<p>您的优惠申请(金额RMB:#{acvitityMoney}元,优惠名称:#{acvitityName})被拒绝！若有疑问，请联系在线客服咨询。</p>', '<p>您的优惠申请(金额RMB:#{acvitityMoney}元,优惠名称:#{acvitityName})被拒绝！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:10:26', '2018-03-13 15:10:26', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('28', '会员返水成功', '11', '<p>恭喜您，您的返水金额#{acvitityMoney}元已派发至您的中心钱包，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的返水金额#{acvitityMoney}元已派发至您的中心钱包，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的返水金额#{acvitityMoney}元已派发至您的中心钱包，请查看。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:12:31', '2018-03-13 15:12:31', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('29', '拒绝会员返水', '12', '<p>您的返水权限被冻结！若有疑问，请联系在线客服咨询。</p>', '<p>您的返水权限被冻结！若有疑问，请联系在线客服咨询。</p>', '<p>您的返水权限被冻结！若有疑问，请联系在线客服咨询。</p>', '2', 'admin', '2018-03-13 15:18:21', '2019-08-28 17:05:30', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('30', '会员提款初审拒绝', '13', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:19:25', '2018-03-13 15:19:25', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('31', '会员提款复审拒绝', '14', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:19:53', '2018-03-13 15:19:53', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('32', '会员提款复审成功', '15', '<p>恭喜您，您的提款#{withdrawMoney}元已完成，请注意查收。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的提款#{withdrawMoney}元已完成，请注意查收。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的提款#{withdrawMoney}元已完成，请注意查收。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:21:13', '2018-03-13 15:21:13', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('33', '拒绝会员提款', '16', '<p>您的提款权限被冻结！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款权限被冻结！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款权限被冻结！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:21:45', '2018-03-13 15:21:45', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('34', '代理注册成功', '17', '<p>恭喜您，您的代理申请已审核通过。</p>', '<p>恭喜您，您的代理申请已审核通过。</p>', '<p>恭喜您，您的代理申请已审核通过。</p>', '1', 'admin', '2018-03-13 15:24:11', '2018-03-13 15:24:11', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('35', '代理取款审核成功', '18', '<p>恭喜您，您的提款#{withdrawMoney}元已完成，请注意查收。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的提款#{withdrawMoney}元已完成，请注意查收。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的提款#{withdrawMoney}元已完成，请注意查收。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:25:06', '2018-03-13 15:25:06', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('36', '代理取款审核失败', '19', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款#{withdrawMoney}元的操作被拒绝！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:26:14', '2018-03-13 15:26:14', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('37', '代理返佣成功', '20', '<p>恭喜您，您的#{term}期佣金#{commssion}元已结算，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的#{term}期佣金#{commssion}元已结算，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的#{term}期佣金#{commssion}元已结算，请查看。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:28:08', '2018-03-13 15:28:08', '1', '0', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('38', '代理账户冻结', '21', '<p>您的账号#{loginName}于#{date}起被禁止登录！若有疑问，请联系在线客服咨询。</p>', '<p>您的账号#{loginName}于#{date}起被禁止登录！若有疑问，请联系在线客服咨询。</p>', '<p>您的账号#{loginName}于#{date}起被禁止登录！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:29:37', '2018-03-13 15:29:37', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('39', '拒绝代理取款', '22', '<p>您的提款权限被冻结！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款权限被冻结！若有疑问，请联系在线客服咨询。</p>', '<p>您的提款权限被冻结！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:30:17', '2018-03-13 15:30:17', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('40', '拒绝代理返佣', '23', '<p>您未达到返佣标准！若有疑问，请联系在线客服咨询。</p>', '<p>您未达到返佣标准！若有疑问，请联系在线客服咨询。</p>', '<p>您未达到返佣标准！若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-03-13 15:30:43', '2018-03-13 15:30:43', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('41', '会员返点', '24', '<p>恭喜您，您的好友返利#{acvitityMoney}元已派发至您的中心钱包，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的好友返利#{acvitityMoney}元已派发至您的中心钱包，请查看。若有疑问，请联系在线客服咨询。</p>', '<p>恭喜您，您的好友返利#{acvitityMoney}元已派发至您的中心钱包，请查看。若有疑问，请联系在线客服咨询。</p>', '1', 'admin', '2018-11-15 14:20:55', '2018-11-15 14:20:55', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('42', '会员增加余额', '25', '<p>恭喜您，经审核给您的钱包余额增加#{despoitMoney}元 ，已派发至您的钱包，若有疑问，请联系在线客服！</p>', '<p>恭喜您，经审核给您的钱包余额增加#{despoitMoney}元 ，已派发至您的钱包，若有疑问，请联系在线客服！</p>', '<p>恭喜您，经审核给您的钱包余额增加#{despoitMoney}元 ，已派发至您的钱包，若有疑问，请联系在线客服！</p>', '1', 'lebron', '2019-01-10 15:34:11', '2019-01-10 15:34:11', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('43', '会员减少余额', '26', '<p>尊敬的会员，您好，经审核您的钱包余额扣除#{despoitMoney}元 ，若有疑问，请联系在线客服！</p>', '<p>尊敬的会员，您好，经审核您的钱包余额扣除#{despoitMoney}元 ，若有疑问，请联系在线客服！</p>', '<p>尊敬的会员，您好，经审核您的钱包余额扣除#{despoitMoney}元 ，若有疑问，请联系在线客服！</p>', '1', 'lebron', '2019-01-10 15:34:11', '2019-01-10 15:34:11', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('44', '好友转账', '27', '<p>您的好友#{loginName}向您转账#{transAmount}元，请留意，祝您玩的开心</p>', '<p>您的好友#{loginName}向您转账#{transAmount}元，请留意，祝您玩的开心</p>', '<p>您的好友#{loginName}向您转账#{transAmount}元，请留意，祝您玩的开心</p>', '1', 'admin', '2019-03-14 16:04:39', '2019-03-14 16:04:39', '1', '0', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('45', '绑定银行卡成功', '28', '<p>您的会员账号已成功绑定银行卡#{cardNo}，若有疑问，请联系在线客服</p>', '<p>您的会员账号已成功绑定银行卡#{cardNo}，若有疑问，请联系在线客服</p>', '<p>您的会员账号已成功绑定银行卡#{cardNo}，若有疑问，请联系在线客服</p>', '1', 'lebron', '2019-05-23 16:04:39', '2019-05-23 16:04:39', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('46', '绑定银行卡成功', '29', '<p>您的会员账号已成功解绑银行卡#{cardNo}，若有疑问，请联系在线客服</p>', '<p>您的会员账号已成功解绑银行卡#{cardNo}，若有疑问，请联系在线客服</p>', '<p>您的会员账号已成功解绑银行卡#{cardNo}，若有疑问，请联系在线客服</p>', '1', 'lebron', '2019-05-23 16:04:39', '2019-05-23 16:04:39', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('47', '会员账号启用', '30', '<p>您的会员账号 #{loginName} 于 #{date} 起被启用登录，若有疑问，请联系在线客服</p>', '<p>您的会员账号 #{loginName} 于 #{date} 起被启用登录，若有疑问，请联系在线客服</p>', '<p>您的会员账号 #{loginName} 于 #{date} 起被启用登录，若有疑问，请联系在线客服</p>', '1', 'lebron', '2019-05-23 16:04:39', '2019-05-23 16:04:39', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('48', '真实姓名修改成功', '31', '<p>您的会员真实姓名已被修改为#{realName}，若有疑问，请联系在线客服</p>', '<p>您的会员真实姓名已被修改为#{realName}，若有疑问，请联系在线客服</p>', '<p>您的会员真实姓名已被修改为#{realName}，若有疑问，请联系在线客服</p>', '1', 'lebron', '2019-05-23 16:04:39', '2019-05-23 16:04:39', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('50', '会员手机号修改成功', '33', '<p>您的手机号码已被修改为#{mobile}，若有疑问，请联系在线客服</p>', '<p>您的手机号码已被修改为#{mobile}，若有疑问，请联系在线客服</p>', '<p>您的手机号码已被修改为#{mobile}，若有疑问，请联系在线客服</p>', '1', 'lebron', '2019-05-23 16:04:39', '2019-05-23 16:04:39', '1', '1', '0');
INSERT INTO `set_basic_msgmodel` VALUES ('51', '重置登录密码成功', '34', '<p>您的会员账号登录密码已被重置成功，若有疑问，请联系在线客服</p>', '<p>您的会员账号登录密码已被重置成功，若有疑问，请联系在线客服</p>', '<p>您的会员账号登录密码已被重置成功，若有疑问，请联系在线客服</p>', '1', 'lebron', '2019-05-23 16:04:39', '2019-05-23 16:04:39', '1', '1', '0');


-- ----------------------------
-- Records of set_basic_msgmodeltype
-- ----------------------------
INSERT INTO `set_basic_msgmodeltype` VALUES ('1', '玩家注册成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('2', '会员升级成功\r\n');
INSERT INTO `set_basic_msgmodeltype` VALUES ('3', '修改会员资料');
INSERT INTO `set_basic_msgmodeltype` VALUES ('4', '强制踢出会员');
INSERT INTO `set_basic_msgmodeltype` VALUES ('5', '会员账号冻结');
INSERT INTO `set_basic_msgmodeltype` VALUES ('6', '在线支付成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('7', '存款审核成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('8', '存款审核失败');
INSERT INTO `set_basic_msgmodeltype` VALUES ('9', '优惠审核成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('10', '优惠审核失败');
INSERT INTO `set_basic_msgmodeltype` VALUES ('11', '会员返水成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('12', '拒绝会员返水');
INSERT INTO `set_basic_msgmodeltype` VALUES ('13', '会员提款初审拒绝');
INSERT INTO `set_basic_msgmodeltype` VALUES ('14', '会员提款复审拒绝');
INSERT INTO `set_basic_msgmodeltype` VALUES ('15', '会员提款复审成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('16', '拒绝会员提款');
INSERT INTO `set_basic_msgmodeltype` VALUES ('17', '代理注册成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('18', '代理取款审核成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('19', '代理取款审核失败');
INSERT INTO `set_basic_msgmodeltype` VALUES ('20', '代理返佣成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('21', '代理账户冻结');
INSERT INTO `set_basic_msgmodeltype` VALUES ('22', '拒绝代理取款');
INSERT INTO `set_basic_msgmodeltype` VALUES ('23', '拒绝代理返佣');
INSERT INTO `set_basic_msgmodeltype` VALUES ('24', '会员返点');
INSERT INTO `set_basic_msgmodeltype` VALUES ('25', '会员增加余额');
INSERT INTO `set_basic_msgmodeltype` VALUES ('26', '会员减少余额');
INSERT INTO `set_basic_msgmodeltype` VALUES ('27', '好友转账');
INSERT INTO `set_basic_msgmodeltype` VALUES ('28', '绑定银行卡成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('29', '解绑银行卡成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('30', '会员账号启用');
INSERT INTO `set_basic_msgmodeltype` VALUES ('31', '真实姓名修改成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('33', '会员手机号修改成功');
INSERT INTO `set_basic_msgmodeltype` VALUES ('34', '重置登录密码成功');

-- ----------------------------
-- Records of s_sys_config
-- ----------------------------
INSERT INTO `s_sys_config`(`id`, `groups`, `keys`, `values`, `description`) VALUES (9, 'groxyApi', '1', '{\"proxyType\":\"SOCKS\",\"ip\":\"10.111.135.52\",\"port\":38080,\"user\":\"kibana\",\"password\":\"wzt20180\"}', '平台代理');
INSERT INTO `s_sys_config`(`id`, `groups`, `keys`, `values`, `description`) VALUES (10, 'groxyPay', 'HUITOONGPAY', '{\"proxyType\":\"HTTP\",\"ip\":\"119.81.251.41\",\"port\":38090,\"user\":\"kibana\",\"password\":\"wzt20180522\"}', '支付代理');


CALL addAgentNode(0,1);
CALL addAgentNode(1,2);
CALL addAgentNode(0,4);
CALL addAgentNode(4,3);

-- ----------------------------
-- Records of mbr_activity_level
-- ----------------------------
INSERT INTO `mbr_activity_level` VALUES ('1', '零星会员', '1', '0.00', '99999.00', '0.00', '49.00', '0', '0', '0', '0', '0', '1', '2019-04-22 16:06:49', 'admin', 'jet123', '2019-08-28 16:45:36', '0', '待站点管理员完善');
INSERT INTO `mbr_activity_level` VALUES ('3', '二星会员', '2', '100.10', '250.10', '100.01', '310.02', '1', '0', '0', '0', '0', '1', '2019-06-06 18:49:34', 'admin', 'sofiazhao', '2019-07-12 10:24:46', '2', '待站点管理员完善');
INSERT INTO `mbr_activity_level` VALUES ('4', '三星会员', '2', '250.00', '500.00', '200.00', '350.00', '1', '0', '0', '0', '0', '1', '2019-06-06 19:04:04', 'admin', 'nathan', '2019-08-05 17:27:13', '3', '待站点管理员完善');
INSERT INTO `mbr_activity_level` VALUES ('5', '一星会员', '2', '100.00', '200.00', '100.00', '200.00', '0', '0', '0', '0', '0', '1', '2019-06-06 19:06:24', 'admin', 'jet123', '2019-08-28 16:17:28', '1', '待站点管理员完善');
INSERT INTO `mbr_activity_level` VALUES ('6', '四星会员', '2', '3001.00', '4000.00', '300.00', '400.00', '1', '0', '0', '0', '0', '1', '2019-06-13 14:32:58', 'admin', 'nathan', '2019-08-05 17:27:13', '4', '待站点管理员完善');
INSERT INTO `mbr_activity_level` VALUES ('7', '五星会员', '2', '5000.00', '9999.00', '4000.00', '5000.00', '1', '0', '0', '0', '0', '1', '2019-06-13 14:34:27', 'admin', 'nathan', '2019-08-05 17:27:13', '5', '待站点管理员完善');
INSERT INTO `mbr_activity_level` VALUES ('8', '六星会员', '2', '16000.00', '999999.00', '5000.00', '6000.00', '0', '0', '0', '0', '0', '1', '2019-06-13 14:36:34', 'admin', 'nathan', '2019-08-27 13:51:31', '6', '待站点管理员完善');


INSERT INTO `opr_act_rule`(`rule`, `time`, `rulename`, `acttmplid`, `available`, `isaudit`, `createuser`, `modifyuser`, `modifytime`, `isdelete`,
`isselfhelp`, `islimit`, `minamount`, `isselfhelpshow`)
VALUES (NULL, '2020-11-12 14:11:42.000', '包赔红包优惠【勿动】', 34, 1, 1, 'admn', 'admin', '2020-11-12 14:08:45', 1, 0, 0, NULL, 0);


INSERT INTO `opr_act_activity`(`activityname`, `usestart`, `useend`, `isshow`, `enablepc`, `enablemb`, `pclogourl`, `mblogourl`,
`content`, `mbcontent`, `createtime`, `available`, `createuser`, `modifyuser`, `modifytime`, `usestate`, `pcremotefilename`, `mbremotefilename`,
`ruleid`, `sort`, `isdel`, `labelid`, `isonline`)
VALUES ('包赔红包优惠【勿动】', '2020-11-12 14:12:31', '2030-11-12 00:00:00', 0, 1, '0', '', NULL,
'包赔红包', NULL, '2020-11-12 14:13:08', 1, '', '', '2021-04-08 12:44:00', 1, '', NULL, (SELECT id from opr_act_rule WHERE rulename ='包赔红包优惠【勿动】'), 22, 0, 0, 1);


INSERT INTO `opr_act_rule`(`rule`, `time`, `rulename`, `acttmplid`, `available`, `isaudit`, `createuser`, `modifyuser`, `modifytime`,
`isdelete`, `isselfhelp`, `islimit`, `minamount`, `isselfhelpshow`) VALUES (NULL, '2020-11-12 14:11:42.000',
'代理充值优惠【勿动】', 21, 1, 1, 'admn', 'admin', '2020-11-12 14:08:45', 1, 0, 0, NULL, 0);


INSERT INTO `opr_act_activity`( `activityname`, `usestart`, `useend`, `isshow`, `enablepc`, `enablemb`, `pclogourl`,
`mblogourl`, `content`, `mbcontent`, `createtime`, `available`, `createuser`, `modifyuser`, `modifytime`, `usestate`,
`pcremotefilename`, `mbremotefilename`, `ruleid`, `sort`, `isdel`, `labelid`, `isonline`) VALUES ( '系统代理充值优惠【勿动】',
'2020-11-12 14:12:31', '2030-11-12 00:00:00', 0, 1, '0', '', NULL, '<p>test</p>',
NULL, '2020-11-12 14:13:08', 1, '', 'dimas0', '2021-04-08 12:44:00', 1, '', NULL, (SELECT id from opr_act_rule WHERE rulename ='代理充值优惠【勿动】'), 22, 0, 0, 1);

delete from `sys_menu_extend`;
INSERT INTO `sys_menu_extend`(`menuid`, `parentid`, `refid`, `name`, `url`, `type`, `isinner`) VALUES (10000131, 152, 0, '直线代理', NULL, 6, 0);
INSERT INTO `sys_menu_extend`(`menuid`, `parentid`, `refid`, `name`, `url`, `type`, `isinner`) VALUES (10000132, 152, 1, '分线代理', NULL, 6, 0);
INSERT INTO `sys_menu_extend`(`menuid`, `parentid`, `refid`, `name`, `url`, `type`, `isinner`) VALUES (10000133, 152, 2, '推广员工', NULL, 6, 0);
INSERT INTO `sys_menu_extend`(`menuid`, `parentid`, `refid`, `name`, `url`, `type`, `isinner`) VALUES (10000134, 152, 3, '招商员工', NULL, 6, 0);

delete from `sys_role_menu_extend`;
INSERT INTO `sys_role_menu_extend`(`id`, `roleid`, `menuid`) VALUES (1, 1, 10000131);
INSERT INTO `sys_role_menu_extend`(`id`, `roleid`, `menuid`) VALUES (2, 1, 10000132);
INSERT INTO `sys_role_menu_extend`(`id`, `roleid`, `menuid`) VALUES (3, 1, 10000133);
INSERT INTO `sys_role_menu_extend`(`id`, `roleid`, `menuid`) VALUES (4, 1, 10000134);

INSERT INTO `sys_role_menu_exten`(`id`, `roleid`, `menuid`, `type`) VALUES (1, 1, -1, 6);

INSERT INTO mbr_label(`id`, `name`, `isAvailable`, `isSetRule`, `aliPayWithdrawal`, `bankWithdrawal`, `isExemptAliPay`, `isExemptBank`, `memo`) VALUES (1, '新会员', 1, 0, 0.00, 0.00, 0, 0, '系统默认新会员');
