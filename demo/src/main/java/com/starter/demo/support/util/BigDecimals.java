package com.starter.demo.support.util;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.awt.Event.DOWN;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;


/**
 * BigDecimals工具
 *
 * @author Shoven
 * @date 2019-03-27 9:48
 */
public class BigDecimals {

    /**
     * BigDecimal的加法运算封装
     *
     * @param o1 加数
     * @param o2 加数
     * @return 和
     */
    public static BigDecimal add(Object o1, Object... o2) {
        BigDecimal b1 = of(o1);
        if (null != o2) {
            for (Object n : ensureArray(o2)) {
                b1 = b1.add(of(n));
            }
        }
        return b1;
    }



    /**
     * BigDecimal的减法运算封装
     *
     * @param o1 被减数
     * @param o2 减数
     * @return 差
     */
    public static BigDecimal subtract(Object o1, Object... o2) {
        BigDecimal b1 = of(o1);
        if (null != o2) {
            for (Object n : ensureArray(o2)) {
                b1 = b1.subtract(of(n));
            }
        }
        return b1;
    }

    /**
     * BigDecimal的除法运算封装
     *
     * @param o1 被除数
     * @param o2 除数
     * @return 商
     */
    public static BigDecimal divide(Object o1, Object o2) {
        if (null == o1 || o1.equals(ZERO)) {
            return ZERO;
        }
        return of(o1).divide(of(o2));
    }


    /**
     * BigDecimal的除法运算封装
     *
     * @param o1 被除数
     * @param o2 除数
     * @param scale 精度
     * @param roundingMode 舍入模式
     * @return 商
     */
    public static BigDecimal divide(Object o1, Object o2, int scale, RoundingMode roundingMode) {
        if (null == o1 || o1.equals(ZERO)) {
            return ZERO;
        }
        return of(o1).divide(of(o2), scale, roundingMode);
    }


    /**
     * BigDecimal的乘法运算封装
     *
     * @param o1 因数
     * @param o2 因数
     * @return 积
     */
    public static BigDecimal multiply(Object o1, Object... o2) {
        if (o1 == null || o1.equals(ZERO) || o2 == null) {
            return ZERO;
        }
        BigDecimal b1 = of(o1);
        for (Object n : ensureArray(o2)) {
            b1 = b1.multiply(of(n));
        }
        return b1;
    }

    /**
     * 是否所有都相等，此处 0 == null视为相等
     *
     * @param o1 比较对象1
     * @param o2 比较对象2
     * @return 比较结果
     */
    public static boolean equals(Object o1, Object... o2) {
        if (o1 == o2) {
            return true;
        }
        BigDecimal b1 = of(o1);
        for (Object n : ensureArray(o2)) {
            BigDecimal b2 = of(n);
            int scala = Math.max(b1.scale(), b2.scale());
            if (!b1.setScale(scala, HALF_UP).equals(b2.setScale(scala, HALF_UP))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 比较结果，此处 0 == null视为相等
     *
     * @param o1 比较对象1
     * @param o2 比较对象2
     * @return 比较结果
     */
    public static int compare(Object o1, Object o2) {
        if (o1 == o2) {
            return 0;
        }
        BigDecimal d1 = of(o1);
        BigDecimal d2 = of(o2);
        int scala = Math.max(d1.scale(), d2.scale());
        // noinspection BigDecimalMethodWithoutRoundingCalled
        return d1.setScale(scala).compareTo(d2.setScale(scala));
    }

    /**
     * 与0比较结果，此处 0 == null视为相等
     *
     * @param o 比较对象1
     * @return 比较结果
     */
    public static int compareZero(Object o) {
        BigDecimal decimal = of(o);
        // noinspection BigDecimalMethodWithoutRoundingCalled
        return decimal.compareTo(ZERO.setScale(decimal.scale()));
    }

    /**
     * 金额字符串
     *
     * @param val 数字或者字符串类型
     * @return 两位小数的字符串数字
     */
    public static String toAmount(Object val) {
        return toScalaString(val, 2);
    }

    /**
     * 四舍五入小数点后两位
     *
     * @param val 数字或者字符串类型
     * @return 两位小数的字符串数字
     */
    public static BigDecimal rounding2decimal(Object val) {
        return of(val).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 转指定小数位数字符串
     *
     * @param val 数字或者字符串类型
     * @return 两位小数的字符串数字
     */
    public static String toScalaString(Object val, int scale) {
        // 去除超过位数的数字
        String str = of(val).setScale(scale, RoundingMode.DOWN).toString();
        // 0.0000 => 0
        // noinspection BigDecimalMethodWithoutRoundingCalled
        return str.equals(ZERO.setScale(scale).toString()) ? "0" : str;
    }

    /**
     * 转指定小数位数四舍五入字符串
     *
     * @param val 数字或者字符串类型
     * @return 两位小数的字符串数字
     */
    public static String toScalaRoundingString(Object val, int scale) {
        // 去除超过位数的数字
        String str = of(val).setScale(scale, RoundingMode.HALF_UP).toString();
        // 0.0000 => 0
        // noinspection BigDecimalMethodWithoutRoundingCalled
        return str.equals(ZERO.setScale(scale).toString()) ? "0" : str;
    }

    /**
     * 转double
     *
     * @param val 数字或者字符串类型
     * @return double
     */
    public static double toDouble(Object val) {
        return of(val).doubleValue();
    }

    /**
     * 转int
     *
     * @param val 数字或者字符串类型
     * @return int
     */
    public static int toInt(Object val) {
        return of(val).intValue();
    }

    /**
     * 转long
     *
     * @param val 数字或者字符串类型
     * @return long
     */
    public static long toLong(Object val) {
        return of(val).longValue();
    }

    /**
     * 从值得到一个BigDecimal实例
     *
     * @param val 数字或者字符串类型
     * @return BigDecimal实例
     */
    public static BigDecimal of(Object val) {
        if (val == null) {
            return ZERO;
        } else if (val instanceof BigDecimal) {
            return (BigDecimal)val;
        } else if (val instanceof Number) {
            return new BigDecimal(val.toString());
        } else {
            String str = val.toString();
            return !NumberUtils.isParsable(str) ? ZERO : new BigDecimal(str);
        }
    }

    /**
     * 确保是数组
     *
     * @param input 入参
     * @return 数组
     */
    private static Object[] ensureArray(Object input) {
        if (input != null && input.getClass().isArray()) {
            return (Object[])input;
        }
        return new Object[]{input};
    }
}
