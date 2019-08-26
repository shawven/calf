package com.test.app.controller.base.method;

import com.test.app.common.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.Serializable;

/**
 * @author Shoven
 * @date 2019-05-17 15:42
 */
public interface SelectOne<T> extends ServiceProvider<T> {
    /**
     * 根据主键id查找一条记录
     *
     * @param id 主键
     * @return
     */
    @GetMapping("{id}")
    default ResponseEntity selectOne(@PathVariable Serializable id) {
        T t = getBaseService().getById(id);
        if (t == null) {
            return Response.notFound("暂无数据！");
        }
        return Response.ok("获取数据成功！", t);
    }
}
