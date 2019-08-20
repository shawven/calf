package com.test.domain;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商品(es_goods)
 * </p>
 *
 * @author lxb
 * @date 2019-08-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("es_goods")
public class Goods implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品编号
     */
    private String sn;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 商品类型normal普通point积分
     */
    private String goodsType;

    /**
     * 重量
     */
    private BigDecimal weight;

    /**
     * 上架状态 1上架  0下架
     */
    private Integer marketEnable;

    /**
     * 详情
     */
    private String intro;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 成本价格
     */
    private BigDecimal cost;

    /**
     * 市场价格
     */
    private BigDecimal mktprice;

    /**
     * 是否有规格0没有 1有
     */
    private Integer haveSpec;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long lastModify;

    /**
     * 浏览数量
     */
    private Integer viewCount;

    /**
     * 购买数量
     */
    private Integer buyCount;

    /**
     * 是否被删除0 删除 1未删除
     */
    private Integer disabled;

    /**
     * 库存
     */
    private Integer quantity;

    /**
     * 可用库存
     */
    private Integer enableQuantity;

    /**
     * 如果是积分商品需要使用的积分
     */
    private Integer point;

    /**
     * seo标题
     */
    private String pageTitle;

    /**
     * seo关键字
     */
    private String metaKeywords;

    /**
     * seo描述
     */
    private String metaDescription;

    /**
     * 商品好评率
     */
    private BigDecimal grade;

    /**
     * 缩略图路径
     */
    private String thumbnail;

    /**
     * 大图路径
     */
    private String big;

    /**
     * 小图路径
     */
    private String small;

    /**
     * 原图路径
     */
    private String original;

    /**
     * 卖家id
     */
    private Integer sellerId;

    /**
     * 店铺分类id
     */
    private Integer shopCatId;

    /**
     * 评论数量
     */
    private Integer commentNum;

    /**
     * 运费模板id
     */
    private Integer templateId;

    /**
     * 谁承担运费0：买家承担，1：卖家承担
     */
    private Integer goodsTransfeeCharge;

    /**
     * 卖家名字
     */
    private String sellerName;

    /**
     * 0 需要审核 并且待审核，1 不需要审核 2需要审核 且审核通过 3 需要审核 且审核未通过
     */
    private Integer isAuth;

    /**
     * 审核信息
     */
    private String authMessage;

    /**
     * 是否是自营商品 0 不是 1是
     */
    private Integer selfOperated;

    /**
     * 下架原因
     */
    private String underMessage;

    /**
     * 税率编码
     */
    private String taxCode;


}
