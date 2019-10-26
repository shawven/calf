package com.starter.demo.controller.base;

import com.starter.demo.controller.base.method.*;
import com.starter.demo.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Shoven
 * @date 2018-11-09
 */
public abstract class BaseController<T> implements SelectAll<T>, SelectOne<T>, SelectPage<T>, Insert<T>, Update<T>, Delete<T> {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    private BaseService<T> service;

    @Override
    public BaseService<T> getBaseService() {
        return service;
    }
}
