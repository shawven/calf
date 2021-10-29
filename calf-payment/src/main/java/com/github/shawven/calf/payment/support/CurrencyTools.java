package com.github.shawven.calf.payment.support;

/**
 * @author Shoven
 * @date 2019-09-20
 */

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 货币转换工具
 */
public class CurrencyTools {

    /**
     * 系统支付金额单位对应多少分
     */
    private static Long unitOfCents;

    /**
     * 转成为分
     * 比如 unitOfCents 100， innerAmount = 0.01时 算出来外部金额等于1分
     * innerAmount 分
     *
     * @return
     */
    public static String toCent(String innerAmount) {
        if (innerAmount == null) {
            innerAmount = "0";
        }
        return new BigDecimal(innerAmount).multiply(BigDecimal.valueOf(unitOfCents)).toBigInteger().toString();
    }

    /**
     * 转成为元
     * 比如 unitOfCents：100， innerAmount = 1时 算出来外部金额等于0.01元
     * <p>
     * innerAmount 元
     *
     * @return
     */
    public static String toYuan(String innerAmount) {
        if (innerAmount == null) {
            innerAmount = "0";
        }
        return new BigDecimal(innerAmount)
                // 系统金额转成分
                .multiply(BigDecimal.valueOf(unitOfCents))
                // 转成元
                .divide(new BigDecimal(100), 2, RoundingMode.DOWN)
                .toString();
    }

    /**
     * 转自分
     * 比如 unitOfCents：100， outerAmount = 100时 算出来内部等于1分
     *
     * @param outerAmount 分
     * @return
     */
    public static String ofCent(String outerAmount) {
        if (outerAmount == null) {
            outerAmount = "0";
        }
        return new BigDecimal(outerAmount)
                .divide(BigDecimal.valueOf(unitOfCents), 2, RoundingMode.DOWN)
                .toString();
    }

    /**
     * 转自元
     * 比如 unitOfCents：100， outerAmount = 1时 算出来内部等于0.01元
     *
     * @param outerAmount 元
     * @return
     */
    public static String ofYuan(String outerAmount) {
        if (outerAmount == null) {
            outerAmount = "0";
        }
        return new BigDecimal(outerAmount)
                // 转成分
                .multiply(new BigDecimal(100))
                // 转成系统金额
                .divide(BigDecimal.valueOf(unitOfCents), 2, RoundingMode.DOWN)
                .toString();
    }

    public static void setUnitOfCents(Long unitOfCents) {
        CurrencyTools.unitOfCents = unitOfCents;
    }
}
