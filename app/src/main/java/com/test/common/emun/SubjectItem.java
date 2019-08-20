package com.test.common.emun;

/**
 * @author Shoven
 * @date 2019-03-21 9:04
 */
public enum SubjectItem {
    /**
     * 库存现金 1001
     */
    KCXJ("库存现金", "1001"),
    /**
     * 银行存款 1002
     */
    YHCK("银行存款", "1002"),
    /**
     * 库存商品 1405
     */
    KCSP("库存商品", "1405"),
     /**
     * 应收账款 1122
     */
    YSZK("应收账款", "1122"),
     /**
     * 应付账款 2202
     */
    YFZK("应付账款", "2202"),
     /**
     * 其他应收款 1221
     */
    QTYSK("其他应收款", "1221"),
     /**
     * 其他应付款 2241
     */
    QTYFK("其他应付款", "2241"),
     /**
     * 主营业务收入 6001
     */
    ZYYWSR("主营业务收入", "6001"),
     /**
     * 管理费用 6602
     */
    GLFY("管理费用", "6602"),
     /**
     * 应付职工薪酬 2211
     */
    YFZGXC("应付职工薪酬", "2211");

    private String name;

    private String code;

    SubjectItem(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public static SubjectItem codeOf(String code) {
        for (SubjectItem subjectItem : values()) {
            if (subjectItem.getCode().equals(code)) {
                return subjectItem;
            }
        }
        return null;
    }
}
