package com.test.controller.base.method;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.test.common.Response;
import com.test.controller.base.BaseController;
import com.test.controller.base.PropertyTranslator;
import com.test.controller.base.Query;
import com.test.controller.base.QueryParser;
import com.test.support.util.ReflectHelpers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-05-17 15:48
 */
public interface SelectPage<T> extends ServiceProvider<T> {

    /**
     * 查询分页记录
     *
     * @param entity 实体类
     * @return
     */
    @GetMapping
    default ResponseEntity selectPage(Query<T> query, T entity){
        Class<? extends ServiceProvider> currentCls = getClass();
        Class<T> modelCls;

        if (BaseController.class.isAssignableFrom(currentCls)) {
            modelCls = ReflectHelpers.getSuperClassGenericType(currentCls, 0);
        } else {
            modelCls = ReflectHelpers.getInterfaceGenericType(currentCls, 0);
        }

        query.setEq(entity);
        QueryParser<T> queryParser = new QueryParser<>(query, new PropertyTranslator(modelCls));

        // 应用查询条件
        QueryWrapper<T> queryWrapper = Wrappers.query(entity);
        queryParser.parseOrders((key, value) -> {
            if (Query.Order.valueOf(value) == Query.Order.ASC) {
                queryWrapper.orderByAsc(key);
            } else {
                queryWrapper.orderByDesc(key);
            }
        });
        queryParser.parseLikes(queryWrapper::like);
        queryParser.parseGreaterEqual(queryWrapper::ge);
        queryParser.parseLessEqual(queryWrapper::le);

        IPage<T> pageData;
        if (query.getCurrent() == -1) {
            List<T> list = getBaseService().list(queryWrapper);
            pageData = query.toPage();
            pageData.setTotal(list.size());
            pageData.setRecords(list);
        } else {
            pageData = getBaseService().page(query.toPage(), queryWrapper);
        }

        if (pageData.getTotal() == 0) {
            return Response.ok("暂无数据！", pageData);
        }
        return Response.ok(pageData);
    }
}
