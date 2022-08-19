package com.wsdy.saasops.common.validator;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import com.wsdy.saasops.common.utils.StringUtil;
import org.apache.commons.lang.StringUtils;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;

/**
 * 数据校验
 */
public abstract class Assert {

    /**
     * 密码口令中相同字符不允许最小的连续个数
     */
    public static String LIMIT_NUM_SAME_CHAR = "3";
    /**
     * 密码口令中字符在逻辑位置上不允许最小的连续个数
     */
    public static String LIMIT_LOGIC_NUM_CHAR = "4";
    /**
     * 是否区分大小写
     */
    public static String CHECK_DISTINGGUISH_CASE = "disable";
    /**
     * 键盘横向方向规则
     */
    //public static String[] KEYBOARD_HORIZONTAL_ARR = { "01234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm", };
    public static String[] KEYBOARD_HORIZONTAL_ARR = { "01234567890" };
    /**
     * 键盘物理位置横向不允许最小的连续个数
     */
    public static String LIMIT_HORIZONTAL_NUM_KEY = "3";

    public static void isBlank(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new R200Exception(message);
        }
    }

    public static void isNull(Object object, String message) {
        if (object == null) {
            throw new R200Exception(message);
        }
    }

    public static void isMaxAmout(BigDecimal min, BigDecimal max, String message) {
        if (min.compareTo(max) == 1) {
            throw new R200Exception(message);
        }
    }


    public static void isNotEmpty(List object, String message) {
        if (!Collections3.isNotEmpty(object)) {
            throw new R200Exception(message);
        }
    }

    public static void isNullOrEmpty(List list, String message) {
        if (null == list || list.size() == 0) {
            throw new R200Exception(message);
        }
    }

    public static void isQq(Object object, String message) {
        if (null == object || !object.toString().matches("[1-9][0-9]{0,14}")) {
            throw new R200Exception(message);
        }
    }

    public static void isWeChat(Object object, String message) {
//        if (null == object || !object.toString().matches("^[a-zA-Z]{1}[-_a-zA-Z0-9]{5,19}+$")) {
//            throw new R200Exception(message);
//        }
    }

    public static void isPInt(Object object, String message) {
        if (null == object || !object.toString().matches("^[1-9]*[1-9][0-9]*$")) {
            throw new R200Exception(message);
        }
    }

    public static void isPhone(Object object, String message) {
       /* if (null == object || !object.toString().matches("^1(3\\d|47|5((?!4)\\d)|7(0|1|[6-8])|8\\d)\\d{8,8}$")) {
            throw new R200Exception(message);
        }*/
        if (null == object || object.toString().length() != 11 || !object.toString().matches("^[1-9]*[1-9][0-9]*$")) {
            throw new R200Exception(message);
        }
    }

    public static void isPhoneAll(Object object,String mobileAreaCode) {
        if("886".equals(mobileAreaCode)){   // 台湾号码校验
            if (null == object || object.toString().length() != 10 || !object.toString().matches("^09\\d{8}$")) {
                throw new R200Exception("会员电话号码只能为数字,并且长度为10位!");
            }
        } else if ("84".equals(mobileAreaCode)) { // 越南号码效验
            if (null == object || object.toString().length() != 9 || !object.toString().matches("^\\d{9}$")) {
                throw new R200Exception("会员电话号码只能为数字,并且长度为9位!");
            }
        }else
        {  // 默认校验大陆
            if (null == object || object.toString().length() != 11 || !object.toString().matches("^[1-9]*[1-9][0-9]*$")) {
                throw new R200Exception("会员电话号码只能为数字,并且长度为11位!");
            }
        }
    }


    public static void isNumeric(Object object, String message) {
        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$")) {
            throw new R200Exception(message);
        }
    }

    public static void isChina(Object object, String message) {
        if (null == object || !object.toString().matches("^[\\u4e00-\\u9fa5]{0,}$")) {
            throw new R200Exception(message);
        }
    }

    public static void isNumeric(Object object, String message, Integer max) {
        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$") || object.toString().length() > max) {
            throw new R200Exception(message);
        }
    }

    public static void isNumeric(BigDecimal object, String message, Integer max, BigDecimal minValue) {
        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$") || object.toString().length() > max || minValue.compareTo(object) == 1) {
            throw new R200Exception(message);
        }
    }

    public static void isNumericInterregional(Integer object, String message, Double minValue, Double maxValue) {

        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$") || minValue > object || object > maxValue) {
            throw new R200Exception(message);
        }
    }

    //不能大于某一个值
    public static void isMaxNum(BigDecimal object, String message, BigDecimal maxNum) {
        if (null == object || !object.toString().matches("^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|[1-9]\\d*|0$") || object.compareTo(maxNum) == 1) {
            throw new R200Exception(message);
        }
    }

    //第一个入参大于第二个入参
    public static void isMax(BigDecimal object, BigDecimal object1, String message) {
        if (object.compareTo(object1) == 1) {
            throw new R200Exception(message);
        }
    }

    public static void isCharacter(Object object, String message) {
        if (null == object || !object.toString().matches("^[A-Za-z0-9]{6,16}$")) {
            throw new R200Exception(message);
        }
        /*String a = object.toString().substring(0, 1);
        if (!a.matches("[a-zA-Z]+$")) {
            throw new R200Exception(message);
        }*/
    }

    public static void isCharacters(Object object, String message) {
        if (null == object || !object.toString().matches("^[^\\u4e00-\\u9fa5]+$")) {
            throw new R200Exception(message);
        }
    }

    public static void isPwdCharacter(Object object, String message) {
        if (null == object || !object.toString().matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z\\S]{6,20}$")) {
            throw new R200Exception(message);
        }
    }

    /**
     * 评估aaaa, 1111这样的相同连续字符
     *
     * @param password
     * @return 含有aaaa, 1111等连续字符串 返回true
     */
    public static void checkSequentialSameChars(String password, String message) {
        String t_password = new String(password);
        int n = t_password.length();
        char[] pwdCharArr = t_password.toCharArray();
        boolean flag = false;
        int limit_num = Integer.parseInt(LIMIT_NUM_SAME_CHAR);
        int count = 0;
        boolean res = false;
        for (int i = 0; i + limit_num <= n; i++) {
            count = 0;
            for (int j = 0; j < limit_num - 1; j++) {
                if (pwdCharArr[i + j] == pwdCharArr[i + j + 1]) {
                    count++;
                    if (count == limit_num - 1) {
                        res = true;
                    }
                }
            }
        }
        if (res) {
            throw new R200Exception(message);
        }
    }

    /**
     * 评估a-z,z-a这样的连续字符
     *
     * @param password
     * @return 含有a-z,z-a连续字符串 返回true
     */
    public static boolean checkSequentialChars(String password) {
        String t_password = new String(password);
        boolean flag = false;
        int limit_num = Integer.parseInt(LIMIT_LOGIC_NUM_CHAR);
        int normal_count = 0;
        int reversed_count = 0;

        // 检测包含字母(区分大小写)
        if ("enable".equals(CHECK_DISTINGGUISH_CASE)) {

        } else {
            t_password = t_password.toLowerCase();
        }
        int n = t_password.length();
        char[] pwdCharArr = t_password.toCharArray();

        for (int i = 0; i + limit_num <= n; i++) {
            normal_count = 0;
            reversed_count = 0;
            for (int j = 0; j < limit_num - 1; j++) {
                if (pwdCharArr[i + j + 1] - pwdCharArr[i + j] == 1) {
                    normal_count++;
                    if (normal_count == limit_num - 1) {
                        return true;
                    }
                }

                if (pwdCharArr[i + j] - pwdCharArr[i + j + 1] == 1) {
                    reversed_count++;
                    if (reversed_count == limit_num - 1) {
                        return true;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 键盘规则匹配器 横向连续检测
     *
     * @param password
     * @return 含有横向连续字符串 返回true
     */
    public static void checkLateralKeyboardSite(String password, String message) {

        String t_password = new String(password);
        // 将字符串内所有字符转为小写
        t_password = t_password.toLowerCase();
        int n = t_password.length();

        /**
         * 键盘横向规则检测
         */
        boolean flag = false;
        int arrLen = KEYBOARD_HORIZONTAL_ARR.length;
        int limit_num = Integer.parseInt(LIMIT_HORIZONTAL_NUM_KEY);

        for (int i = 0; i + limit_num <= n; i++) {
            String str = t_password.substring(i, i + limit_num);
            String distinguishStr = password.substring(i, i + limit_num);

            for (int j = 0; j < arrLen; j++) {
                String configStr = KEYBOARD_HORIZONTAL_ARR[j];
                String revOrderStr = new StringBuffer(KEYBOARD_HORIZONTAL_ARR[j]).reverse()
                        .toString();

                // 检查包含字母(区分大小写)
                if ("enable".equals(CHECK_DISTINGGUISH_CASE)) {
                    // 考虑 大写键盘匹配的情况
                    String upperStr = KEYBOARD_HORIZONTAL_ARR[j].toUpperCase();
                    if ((configStr.indexOf(distinguishStr) != -1) || (upperStr.indexOf(distinguishStr) != -1)) {
                        flag = true;
                    }
                    // 考虑逆序输入情况下 连续输入
                    String revUpperStr = new StringBuffer(upperStr).reverse().toString();
                    if ((revOrderStr.indexOf(distinguishStr) != -1) || (revUpperStr.indexOf(distinguishStr) != -1)) {
                        flag = true;
                    }
                } else {
                    if (configStr.indexOf(str) != -1) {
                        flag = true;
                    }
                    // 考虑逆序输入情况下 连续输入
                    if (revOrderStr.indexOf(str) != -1) {
                        flag = true;
                    }
                }
            }
        }
        if (flag) {
            throw new R200Exception(message);
        }
    }


    public static void accEqualPwd(String loginName, String pwd, String message) {
        if (StringUtils.isEmpty(pwd) || loginName.equals(pwd)) {
            throw new R200Exception(message);
        }
    }

    public static void isAccount(Object object, String message) {

        if (object.toString().length() > 10 || object.toString().length() < 6) {
            throw new R200Exception(message);
        }
    }

    public static void isAccountEx(Object object, String message) {

        if (object.toString().length() > 16 || object.toString().length() < 6) {
            throw new R200Exception(message);
        }
    }

    public static void isLenght(Object object, String message, int start, int end) {
        if (!org.springframework.util.StringUtils.isEmpty(object) && (object.toString().length() > end || object.toString().length() < start)) {
            throw new R200Exception(message);
        }
    }

    public static void isSiteCode(String siteCode, String message, int start, int end) {
        String format = "^[0-9a-zA-Z]{" + start + "," + end + "}$";
        if (!siteCode.matches(format)) {
            throw new R200Exception(message);
        }
    }

    public static void isBankCardNo(Object object, String message, int start, int end) {
        if (object.toString().length() < start || object.toString().length() > end) {
            throw new R200Exception(message);
        }
    }

    public static void message(String message) {
        throw new R200Exception(message);
    }

    public static void checkEmail(String email, String message) {
        String format = "\\w+@[\\w\\-]+\\.[a-z]+(\\.[a-z]+)?";
        if (!email.matches(format)) {
            throw new R200Exception(message);
        }
    }

    /**
     * 判断是否为汉字及.
     *
     * @param character
     * @param message
     */
    public static void chineseCharacter(String character, String message) {
        String format = "[\\u4e00-\\u9fa5-\\.]+$";
        if (!character.matches(format)) {
            throw new R200Exception(message);
        }
    }

    public static void userRealNameCharacter(String character, String message) {
        String format = "^[A-Za-z0-9\\u4e00-\\u9fa5]{2,20}$";
        if (!character.matches(format)) {
            throw new R200Exception(message);
        }
    }
    /**
     * 判断是否中文或者英文+ 空格符号
     *
     * @param character
     * @param message
     */
    public static void realNameCharacter(String character, String message) {
        String format = "^([\\u4e00-\\u9fa5\\·]+|([a-zA-Z]+\\s?)+)$";
        if (!character.matches(format)) {
            throw new R200Exception(message);
        }
    }

    public static void isBigDecimalNum(BigDecimal object, String message) {
        if (new BigDecimal(object.intValue()).compareTo(object) != 0) {
            throw new R200Exception(message);
        }
    }

    /**
     * 判断是否包含有中文字符
     */
    public static void isContainChinaChar(String str, String message) {
        if (StringUtil.isNotEmpty(str)) {
            String format = "[\u4e00-\u9fa5]";
            Pattern p = Pattern.compile(format);
            if (p.matcher(str).find()) {
                throw new R200Exception(message);
            }
        }
    }
    // 判断是否是正确的百分数(含2位小数)
    public static void isPercent(BigDecimal object, String message) {
        if (null == object || !object.toString().matches("^?(100|(([1-9]\\d|\\d)(\\.\\d{1,2})?))$")) {
            throw new R200Exception(message);
        }
    }

    // 校验是否为ERC20地址
    public static void isERC20Address(String character, String message) {
        String format = "^0x[0-9a-fA-F]{40}$";
        if (!character.matches(format)) {
            throw new R200Exception(message);
        }
    }

    // 校验是否为TRC20地址
    public static void isTRC20Address(String character, String message) {
        if (!character.startsWith("T")) {
            throw new R200Exception(message);
        }
        if (character.length() != 34 ) {
            throw new R200Exception(message);
        }
    }

    public static void ipAndDeviceRegAssert(String message) {
            throw new R200Exception(message);
    }
}
