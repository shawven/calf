package com.test.support.util;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
     * @param b1
     * @param bn
     * @return
     */
    public static BigDecimal add(BigDecimal b1, BigDecimal... bn) {
        b1 = of(b1);
        if (null != bn) {
            for (BigDecimal b : bn) {
                b1 = b1.add(of(b));
            }
        }
        return b1;
    }



    /**
     * BigDecimal的减法运算封装
     *
     * @param b1
     * @param bn
     * @return
     */
    public static BigDecimal subtract(BigDecimal b1, BigDecimal... bn) {
        b1 = of(b1);
        if (null != bn) {
            for (BigDecimal b : bn) {
                b1 = b1.subtract(of(b));
            }
        }
        return b1;
    }

    /**
     * BigDecimal的除法运算封装
     *
     * @param b1
     * @param b2
     * @return
     */
    public static BigDecimal divide(BigDecimal b1, BigDecimal b2) {
        if (null == b1 || b1.equals(ZERO)) {
            return ZERO;
        }
        return b1.divide(of(b2));
    }


    /**
     * BigDecimal的除法运算封装
     *
     * @param b1
     * @param b2
     * @param scale
     * @param roundingMode
     * @return
     */
    public static BigDecimal divide(BigDecimal b1, BigDecimal b2, int scale, RoundingMode roundingMode) {
        if (null == b1 || b1.equals(ZERO)) {
            return ZERO;
        }
        return b1.divide(of(b2), scale, roundingMode);
    }


    /**
     * BigDecimal的乘法运算封装
     *
     * @param b1
     * @param b2
     * @return
     */
    public static BigDecimal multiply(BigDecimal b1, BigDecimal b2) {
        if (b1 == null || b1.equals(ZERO) || b2 == null || b2.equals(ZERO)) {
            return ZERO;
        }
        return b1.multiply(b2);
    }

    /**
     * 判断是否相等，只判断是否有数值意义 此处 0 == null
     *
     * @param b1
     * @param b2
     * @return
     */
    public static boolean equals(BigDecimal b1, BigDecimal b2) {
        if (b1 == null && b2 == null) {
            return true;
        }
        if (b1 == null || b2 == null) {
            return false;
        }
        int scala = Math.max(b1.scale(), b2.scale());
        return b1.setScale(scala, HALF_UP).equals(b2.setScale(scala, HALF_UP));
    }

    /**
     * 是否所有都相等，同上
     *
     * @param b1
     * @param bns
     * @return
     */
    public static boolean equalsAll(BigDecimal b1, BigDecimal... bns) {
        for (BigDecimal bn : bns) {
            if (!equals(b1, bn)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param val
     * @return
     */
    public static String toAmount(Float val) {
        return of(val).setScale(2, HALF_UP).toString();
    }

    /**
     * @param val
     * @return
     */
    public static String toAmount(Double val) {
        return of(val).setScale(2, HALF_UP).toString();
    }

    /**
     * @param val
     * @return
     */
    public static String toAmount(String val) {
        return of(val).setScale(2, HALF_UP).toString();
    }

    /**
     * @param val
     * @return
     */
    public static String toAmount(BigDecimal val) {
        return of(val).setScale(2, HALF_UP).toString();
    }

    /**
     * @param val
     * @return
     */
    public static double toDouble(BigDecimal val) {
        return of(val).doubleValue();
    }

    /**
     * @param val
     * @return
     */
    public static BigDecimal of(Float val) {
        return val == null ? ZERO : new BigDecimal(val.toString());
    }

    /**
     * @param val
     * @return
     */
    public static BigDecimal of(Double val) {
        return val == null ? ZERO : BigDecimal.valueOf(val);
    }

    /**
     * @param val
     * @return
     */
    public static BigDecimal of(String val) {
        return !NumberUtils.isDigits(val) ? ZERO : new BigDecimal(val);
    }

    /**
     * @param val
     * @return
     */
    public static BigDecimal of(BigDecimal val) {
        return val == null ? ZERO : val;
    }
}
