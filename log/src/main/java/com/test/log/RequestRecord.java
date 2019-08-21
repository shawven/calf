package com.test.log;


import com.test.log.emun.LogType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Shoven
 * @date 2019-07-26 17:20
 */
@Getter
@Setter
public class RequestRecord extends DefaultRecord {
    /**
     * 主键ID
     */

    private Long id;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private Integer type;

    /**
     * 操作类型名称
     */
    private String typeName;

    /**
     * 操作描述
     */
    private String desc;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求路径
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestHeaders;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 操作ip
     */
    private String ip;

    /**
     * 操作地址
     */
    private String address;

    /**
     * 耗时
     */
    private Long cost;

    /**
     * 创建时间
     */
    private Date createTime;

    @Override
    public void setLogType(LogType type) {
        this.type = type.getType();
        this.typeName = type.getTypeName();
    }
}
