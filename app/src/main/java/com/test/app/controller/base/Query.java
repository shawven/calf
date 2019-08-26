package com.test.app.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;
import java.util.Objects;

/**
 * @author Shoven
 * @date 2019-05-19 23:56
 */
public class Query<T> extends Page<T> {

    private T eq;

    private Map<String, Integer> sort;

    private Map<String, String> like;

    private Map<String, String> ge;

    private Map<String, String> le;

    public Page<T> toPage() {
        return new Page<>(getCurrent(), getSize());
    }

    public T getEq() {
        return eq;
    }

    public void setEq(T eq) {
        this.eq = eq;
    }

    public Map<String, Integer> getSort() {
        return sort;
    }

    public void setSort(Map<String, Integer> sort) {
        this.sort = sort;
    }

    public Map<String, String> getLike() {
        return like;
    }

    public void setLike(Map<String, String> like) {
        this.like = like;
    }

    public Map<String, String> getGe() {
        return ge;
    }

    public void setGe(Map<String, String> ge) {
        this.ge = ge;
    }

    public Map<String, String> getLe() {
        return le;
    }

    public void setLe(Map<String, String> le) {
        this.le = le;
    }

    public enum Order {
        /**
         * 升序
         */
        ASC(1, "ASC"),
        /**
         * 降序
         */
        DESC(2, "DESC");

        private Integer value;

        private String dir;

        Order(Integer value, String dir) {
            this.value = value;
            this.dir = dir;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public static Order valueOf(Integer id) {
            return Objects.equals(id, ASC.value) ? ASC : DESC;
        }
    }
}
