package com.test.controller.base.method;

import com.test.common.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

/**
 * @author Shoven
 * @date 2019-05-17 15:43
 */
public interface Insert<T> extends ServiceProvider<T>, CacheDeletable {

    /**
     * 添加记录
     *
     * @param entity 实体类
     * @return
     */
    @PostMapping
    default ResponseEntity insert(@Valid T entity) {
        if (!getBaseService().save(entity)) {
            return Response.error("添加失败！");
        }
        deleteCache();
        return Response.created("添加成功！");
    }
}
