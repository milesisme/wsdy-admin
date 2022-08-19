package com.wsdy.saasops.common.utils;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 字符串工具类
 * 
 */
public class StringUtil {

	private static final int INDEX_NOT_FOUND = -1;

	private static final String NULL = "null";

	private static final String PLACEHOLDER = "\\?";

	//private static final String PLACEHSYMBOL = ",";

	private static final String PLACEQUOTE = "'";
	private static final String ONE_DAY_STR="86400";
	private static final String ONE_HOUR_STR="3600"; 
	private static final String ONE_MINUTE_STR="60";
	
	private static final String ONE_DAY_CHAR="天";
	private static final String ONE_HOUR_CHAR="小时";
	private static final String ONE_MINUTE_CHAR="分";

	private static Pattern EMOJ_PATTERN = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]");

	/**
	 * 判断字符串是否为空
	 * 
	 * @param source
	 *            字符串
	 * @return 若为null或长度为0, 则返回true, 否则返回false
	 */
	public static boolean isEmpty(String source) {
		return source == null || source.length() == 0;
	}
/**
 * 
 * @param source
 * @return
 */
	public static boolean isEmpty(Object source) {
		return source == null? true : source instanceof String ? isEmpty((String)source):false ;
	}

	/**
	 * 判断字符串是否不为空
	 * 
	 * @param source
	 *            字符串
	 * @return 若不为null而且长度不为0, 则返回true, 否则返回false
	 */
	public static boolean isNotEmpty(String source) {
		return !isEmpty(source);
	}

	/**
	 * 分割字符串得到子串
	 * 
	 * @param source
	 *            源字符串
	 * @param beginIndex
	 *            开始索引, 允许负数值, 表示从后往前
	 * @return
	 */
	public static String substring(String source, int beginIndex) {
		return substring(source, beginIndex, source.length());
	}

	/**
	 * 分割字符串得到子串
	 * 
	 * @param source
	 *            源字符串
	 * @param beginIndex
	 *            开始索引, 允许负数值, 表示从后往前
	 * @param endIndex
	 *            结束索引, 允许负数值, 表示从后往前
	 * @return
	 */
	public static String substring(String source, int beginIndex, int endIndex) {
		int length = source.length();
		if (beginIndex < 0) {
			beginIndex += length;
		}
		if (endIndex < 0) {
			endIndex += length;
		}
		if (beginIndex > endIndex) {
			throw new StringIndexOutOfBoundsException("String index out of range: " + (endIndex - beginIndex));
		}
		return source.substring(beginIndex, endIndex);
	}

	/**
	 * 获取子串在源串中第一次出现的位置结束, 索引从0开始的串
	 * 
	 * @param source
	 *            源串
	 * @param substring
	 *            子串
	 * @return 若源串中不含有子串, 则返回一个空串
	 */
	public static String beforeString(String source, String substring) {
		int index = source.indexOf(substring);
		if (index != INDEX_NOT_FOUND) {
			return source.substring(0, index);
		}
		return "";
	}

	/**
	 * 获取子串在源串中最后一次出现的位置结束, 索引从0开始的串
	 * 
	 * @param source
	 *            源串
	 * @param substring
	 *            子串
	 * @return 若源串中不含有子串, 则返回一个空串
	 */
	public static String beforeLastString(String source, String substring) {
		int index = source.lastIndexOf(substring);
		if (index != INDEX_NOT_FOUND) {
			return source.substring(0, index);
		}
		return "";
	}

	/**
	 * 获取子串在源串中第一次出现的位置开始, 至源串末尾的串
	 * 
	 * @param source
	 *            源串
	 * @param substring
	 *            子串
	 * @return 若源串中不含有子串, 则返回一个空串
	 */
	public static String afterString(String source, String substring) {
		int index = source.indexOf(substring);
		if (index != INDEX_NOT_FOUND) {
			return source.substring(index + substring.length());
		}
		return "";
	}

	/**
	 * 获取子串在源串中最后一次出现的位置开始, 至源串末尾的串
	 * 
	 * @param source
	 *            源串
	 * @param substring
	 *            子串
	 * @return 若源串中不含有子串, 则返回一个空串
	 */
	public static String afterLastString(String source, String substring) {
		int index = source.lastIndexOf(substring);
		if (index != INDEX_NOT_FOUND) {
			return source.substring(index + substring.length());
		}
		return "";
	}

	/**
	 * 获取源串中, 两个子串之间的串
	 * 
	 * @param source
	 *            源串
	 * @param begin
	 *            子串
	 * @param end
	 *            子串
	 * @return 若源串中不含有其中任一子串, 则返回一个空串
	 */
	public static String betweenString(String source, String begin, String end) {
		int endIndex = source.indexOf(end);
		int beginIndex = source.indexOf(begin);
		if (beginIndex != INDEX_NOT_FOUND && endIndex != INDEX_NOT_FOUND) {
			return source.substring(beginIndex + begin.length(), endIndex);
		}
		return "";
	}

	/**
	 * 首字母大写
	 * 
	 * @param str
	 *            字符串
	 * @return
	 */
	public static String toCapitalize(String str) {
		byte[] bytes = str.getBytes();
		byte e = bytes[0];
		if (e >= 'a' && e <= 'z') {
			bytes[0] -= 32;
		}
		return new String(bytes);
	}

	/**
	 * 首字母小写
	 * 
	 * @param str
	 *            字符串
	 * @return
	 */
	public static String toUncapitalize(String str) {
		byte[] bytes = str.getBytes();
		byte e = bytes[0];
		if (e >= 'A' && e <= 'Z') {
			bytes[0] += 32;
		}
		return new String(bytes);
	}

	/**
	 * 以参数替换占位符[?]的形式格式化字符串
	 * 
	 * @param origin
	 *            字符串
	 * @param args
	 *            参数
	 * @return
	 */
	public static String format(String origin, Object... args) {
		for (Object arg : args) {
			origin = origin.replaceFirst(PLACEHOLDER, arg == null ? NULL : arg.toString());
		}
		return origin;
	}
	
	public static String formatTrad(String str) {
		StringBuffer sb = new StringBuffer();
		if (!isEmpty(str)) {
			sb.append(PLACEQUOTE);
			sb.append(str.replaceAll(",", "','"));
			sb.append(PLACEQUOTE);
		}
		return sb.toString();
	}

	public static String formatOnlineTime(BigInteger time) {
		StringBuffer buf = new StringBuffer();
		if (time.compareTo(new BigInteger("59")) == 1) {
			int d = BigDecimalMath.intDev(time, new BigInteger(ONE_DAY_STR));
			time = BigDecimalMath.intSub(time, BigDecimalMath.intMul(ONE_DAY_STR, String.valueOf(d)));
			int h = BigDecimalMath.intDev(time, new BigInteger(ONE_HOUR_STR));
			time = BigDecimalMath.intSub(time, BigDecimalMath.intMul(ONE_HOUR_STR, String.valueOf(h)));
			int m = BigDecimalMath.intDev(time, new BigInteger(ONE_MINUTE_STR));
			if (d > 0) {
                buf.append(String.valueOf(d)).append(ONE_DAY_CHAR);
            }
			if (h > 0 || d > 0) {
                buf.append(String.valueOf(h)).append(ONE_HOUR_CHAR);
            }
			if (m > 0) {
                buf.append(String.valueOf(m)).append(ONE_MINUTE_CHAR);
            }
		} else if (time.compareTo(BigInteger.ZERO) == 1) {
			buf.append("少于1分");
		}
		return buf.toString();
	}
    
	/**
	 *  手机号脱敏
	 * 
	 * @param phone
	 * @return
	 */
	public static String phone(String phone) {
		if (!StringUtil.isEmpty(phone) && !"null".equals(phone)) {
            return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        } else {
            return "";
        }
	}
	
	public static String bankNo(String bankNo) {
		if (!StringUtil.isEmpty(bankNo))
		{
			if(bankNo.length()==16)
			{
				return bankNo.replaceAll("(\\d{4})\\d{8}(\\d{4})", "$1****$2");
			}else if(bankNo.length()==19)
			{
				return bankNo.replaceAll("(\\d{4})\\d{12}(\\d{3})", "$1****$2");
			}else {
				return "****"+bankNo.substring(4,bankNo.length()-4)+"****";
			}
		}
		else {
            return "";
        }
	}

	public static String bankNoEx(String bankNo) {
		if (!StringUtil.isEmpty(bankNo)){
            return bankNo.substring(0,4)+"******" + bankNo.substring(bankNo.length()-4,bankNo.length());
		}
		else {
			return "";
		}
	}


	public static String walletAddress(String walletAddress) {
		if (!StringUtil.isEmpty(walletAddress)){
			return walletAddress.substring(0,4)+"******" + walletAddress.substring(walletAddress.length()-4,walletAddress.length());
		}
		else {
			return "";
		}
	}

	public static String mail(String mail) {
		if (!StringUtil.isEmpty(mail) && !"null".equals(mail)) {
			int a = mail.indexOf("@");
			if(a<0){	// 邮箱格式不正确
				return "";
			}
			return mail.replaceAll(mail.substring(a < 4 ? 0 : 3, mail.lastIndexOf("@")), "***");
		} else {
            return "";
        }
	}

	public static String QQ(String qq) {
		if (!StringUtil.isEmpty(qq) && !"null".equals(qq)) {
			return qq.replaceAll(qq.substring(qq.length() < 4 ? 0 : qq.length() - 3, qq.length()), "***");
		}
		return StringUtils.EMPTY;
	}

	public static String realName(String realName) {
		if (!StringUtil.isEmpty(realName) && !"null".equals(realName)) {
            return realName.replaceAll(realName.substring( 0 , 1), "**");
        } else {
            return "";
        }
	}
	public static boolean isHasEmoji(String reviewerName) {
		Matcher matcher = EMOJ_PATTERN.matcher(reviewerName);
		return matcher.find();
	}
}