package com.example.nativepractice.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BaseDAO<T> {

	/**
	 * 根据id查询
	 *
	 * @param id
	 * @return
	 */
	T findById(String id);

	/**
	 * 保存entity，会清理缓存
	 *
	 * @param t
	 * @return
	 */
	T save(T t);
	/**
	 * 插入entity，会清理缓存
	 *
	 * @param t
	 * @return
	 */
	T insert(T t);

    /**
     * 批量插入entity
     *
     * @param list
     * @return
     */
    Collection<T> insert(Collection<T> list);

    /**
	 * 更新文档,只更新一条，注意啦，不会清理缓存
	 *
	 * @param query
	 * @param update
	 * @return
	 */
	void update(Query query, Update update);

	/**
	 * 不会清理缓存
	 */
	void remove(String ...id);


	/**
	 * 根据条件删除
	 * 软删除操作
	 */
	void remove(Query query,Update update);

	/**
	 * 批量更新对象，不会清理缓存
	 *
	 * @param query
	 * @param update
	 */
	void updateMulti(Query query, Update update);

	/**
	 * 分页查询
	 * @param query
	 * @param pageable
	 * @return
	 */
	Page<T> findPage(Query query,Pageable pageable);

	Page<T> findAll(Map<String, Object> searchMap, Pageable pageable);

	List<T> findByIds(List<String> ids);

	/**
	 * 不分页查找
	 * @param query
	 * @return
	 */
	List<T> find(Query query);
	/**
	 * 查找一个文档
	 * @param query
	 * @return t
	 */
	T findOne(Query query);

    /**
     * 存在记录
     *
     * @param query
     * @return
     */
    boolean exist(Query query);

    /**
     * 统计记录总数
     *
     * @param query
     * @return
     */
    long count(Query query);
}
