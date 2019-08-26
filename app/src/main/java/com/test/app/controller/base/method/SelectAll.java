package com.test.app.controller.base.method;

import com.test.app.common.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-05-17 15:38
 */
public interface SelectAll<T> extends ServiceProvider<T> {

    /**
     * 查询所有记录
     *
     * @return
     */
    @GetMapping("/all")
    default ResponseEntity selectAll(T entity) {
        List<T> list = getBaseService().list(entity);

        if (CollectionUtils.isEmpty(list)) {
            return Response.ok("暂无数据！");
        }

        return Response.ok("获取数据成功！", list);
    }
}
