package com.test.app.controller.base.method;

import com.test.app.common.Response;
import com.test.app.service.base.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;

/**
 * @author Shoven
 * @date 2019-05-17 15:46
 */
public interface Delete<T> extends ServiceProvider<T> {

    /**
     * 根据id删除记录
     *
     * @param id 主键id 或者 主键id字符串11,22,33
     * @return
     */
    @DeleteMapping("{id}")
    default ResponseEntity delete(@PathVariable(value = "id") String id) {
        BaseService<T> service = getBaseService();
        if (StringUtils.isBlank(id)) {
            return Response.badRequest("未指定要删除的id！");
        }
        String[] ids = StringUtils.split(id.trim(), ",");
        if (ids.length == 1) {
            if (!service.removeById(id)) {
                return Response.error("删除失败！");
            }
        } else if (!service.removeByIds(Arrays.asList(ids))) {
            return Response.error("批量删除失败！");
        }
        return Response.noContent();
    }
}
