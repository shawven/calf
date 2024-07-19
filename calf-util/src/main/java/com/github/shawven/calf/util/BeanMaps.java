package com.github.shawven.calf.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Bean 映射复制工具
 * 还支持Map
 *
 * @see BeanUtils
 * @author xw
 * @date 2022/10/20
 */
public class BeanMaps {

    public static <T> T map(Object source, T target) {
        if (source == null) {
            return null;
        }

        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T> T map(Object source, Class<T> targetCls) {
        if (source == null) {
            return null;
        }

        T target = BeanUtils.instantiateClass(targetCls);
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T> List<T> mapList(Collection<?> sourceList, Class<T> targetCls) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> targetList = new ArrayList<T>();
        for (Object source : sourceList) {
            targetList.add(map(source, targetCls));
        }
        return targetList;
    }

    public static <T> List<T> objToList(Object obj, Class<T> targetCls){
        List<T> list=new ArrayList<T>();
        if (obj instanceof ArrayList<?>){
            for (Object o :(List<?>) obj){
                list.add(targetCls.cast(o));
            }
            return list;
        }
        return null;
    }
}
