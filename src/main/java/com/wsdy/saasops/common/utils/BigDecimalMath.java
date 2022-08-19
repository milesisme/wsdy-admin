package com.wsdy.saasops.common.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import com.wsdy.saasops.common.exception.RRException;

public class BigDecimalMath {
	public static BigDecimal add(double d1, double d2) { // 进行加法运算
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		return b1.add(b2);
	}

	public static BigDecimal sub(double d1, double d2) { // 进行减法运算
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		return b1.subtract(b2);
	}

	public static BigDecimal mul(double d1, double d2) { // 进行乘法运算
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		return b1.multiply(b2);
	}

	public static BigDecimal div(double d1, double d2, int len) {// 进行除法运算
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP);
	}

	public static BigDecimal round(double d, int len) { // 进行四舍五入操作
		BigDecimal b1 = new BigDecimal(d);
		BigDecimal b2 = new BigDecimal(1);
		
		// 任何一个数字除以1都是原数字
		// ROUND_HALF_UP是BigDecimal的一个常量，表示进行四舍五入的操作
		return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP);
	}
	
	public static BigDecimal add(BigDecimal d1, BigDecimal d2) { // 进行加法运算
		return d1.add(d2);
	}

	public static BigDecimal sub(BigDecimal d1, BigDecimal d2) { // 进行减法运算
		return d1.subtract(d2);
	}

	public static BigDecimal mul(BigDecimal d1, BigDecimal d2) { // 进行乘法运算
		return d1.multiply(d2);
	}

	public static BigDecimal div(BigDecimal d1, BigDecimal d2, int len) {// 进行除法运算
		return d1.divide(d2, len, BigDecimal.ROUND_HALF_UP);
	}

	public static BigDecimal round(BigDecimal d, int len) { // 进行四舍五入操作
		BigDecimal b2 = new BigDecimal(1);
		
		// 任何一个数字除以1都是原数字
		// ROUND_HALF_UP是BigDecimal的一个常量，表示进行四舍五入的操作
		return d.divide(b2, len, BigDecimal.ROUND_HALF_UP);
	}
	public static int intDev(BigInteger BigInt1,BigInteger BigInt2){
       BigInt1=BigInt1.divide(BigInt2);//除
        return BigInt1.intValue();
    }

	public static BigInteger intSub(BigInteger BigInt1, BigInteger BigInt2) {
		return BigInt1.subtract(BigInt2);// 减
	}

	public static BigInteger intMul(String Str1, String Str2) {
		BigInteger BigInt1 = new BigInteger(Str1);
		BigInteger BigInt2 = new BigInteger(Str2);
		BigInt1 = BigInt1.multiply(BigInt2);// 乘
		return BigInt1;
	}

	public static Integer ceil(int d1, int d2) {// 进行除法运算,向上取整
		return d1%d2!=0?d1/d2+1:d1/d2;
	}
	
	//向下取整
	public static BigDecimal formatDownRounding(BigDecimal b) {
		return b.setScale(0, BigDecimal.ROUND_DOWN);
	}

	public static String numberFormat(String number) {
		DecimalFormat df = new DecimalFormat("#########0.00");
		NumberFormat nf = NumberFormat.getInstance();
		try {
			return df.format(nf.parse(number));
		} catch (ParseException e) {
			throw new RRException("格式化数据异常!");
		}
	}
	public static void main(String[] args) {
		/*System.out.println("加法运算：" + BigDecimalMath.round(BigDecimalMath.add(10.345, 3.333), 2));
		System.out.println("乘法运算：" + BigDecimalMath.round(BigDecimalMath.mul(10.345, 3.333), 2));
		System.out.println("除法运算：" + BigDecimalMath.div(10.345, 3.333, 2));
		System.out.println("减法运算：" + BigDecimalMath.round(BigDecimalMath.sub(10.345, 3.333), 2));
		BigInteger a=new BigInteger("22");
		BigInteger b=new BigInteger("33");
		System.out.println(getDivide(a,b));*/
		//System.out.println(numberFormat("1231231231.0000"));
	}
}