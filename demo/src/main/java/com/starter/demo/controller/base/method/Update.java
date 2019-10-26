package com.starter.demo.controller.base.method;

import com.starter.demo.common.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.Valid;
import java.io.Serializable;

/**
 * @author Shoven
 * @date 2019-05-17 15:44
 */
public interface Update<T> extends ServiceProvider<T> {

    /**
     * 根据主键id更新记录
     *
     * @param id id
     * @param entity 实体类
     * @return
     */
    @PutMapping("{id}")
    default ResponseEntity update(@PathVariable Serializable id, @Valid T entity) {
        if (!getBaseService().updateById(entity)) {
            return Response.error("更新失败！");
        }
        return Response.created("更新成功！", entity);
    }
}
