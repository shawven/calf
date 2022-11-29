package com.example.nativepractice.dao;

import com.example.nativepractice.entity.Entity;
import com.example.nativepractice.util.ReflectHelpers;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class BaseDAOImpl<T extends Entity> implements BaseDAO<T> {
    @Autowired
    protected MongoTemplate mongoTemplate;

    public MongoTemplate getMongoTemplate() {
        return this.mongoTemplate;
    }

    @Override
    public T save(T t) {
        if (t == null) {
            return null;
        }
        if (StringUtils.isBlank(t.getId())) {
            t.setId(null);
        }
        t.setDelete(false);
        mongoTemplate.save(t, getTableName());
        return t;
    }

    @Override
    public T insert(T t) {
        if (t == null) {
            return null;
        }
        t.setDelete(false);
        mongoTemplate.insert(t, getTableName());
        return t;
    }

    @Override
    public Collection<T> insert(Collection<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        list.forEach(t -> {
            if (StringUtils.isBlank(t.getId())) {
                t.setId(null);
            }
            t.setDelete(false);
        });
        mongoTemplate.insert(list, getTableName());
        return list;
    }

    /**
     * 根据实体主键更新
     *
     * @param t
     * @return
     */
    public boolean updateById(T t) {
        Map<String, Object> map = ReflectHelpers.objectToMap(t);
        String id = (String) map.remove("id");
        Assert.hasText(id, "id must not be null");

        if (map.isEmpty()) {
            return false;
        }

        Update update = new Update();
        Query query = new Query(Criteria.where(Entity.ID).is(id).and(Entity.DELETE).is(false));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                update.set(entry.getKey(), entry.getValue());
            }
        }

        return mongoTemplate.updateFirst(query, update, getTableName()).wasAcknowledged();
    }

    /**
     * 只更新一条，要注意啦
     *
     * @param query
     * @param update
     */
    @Override
    public void update(Query query, Update update) {
        Criteria criteria = new Criteria();
        criteria.and(Entity.DELETE).is(false);
        query.addCriteria(criteria);
        mongoTemplate.updateFirst(query, update, this.getEntityClass(), getTableName());
    }

    @Override
    public void updateMulti(Query query, Update update) {
        Criteria criteria = new Criteria();
        criteria.and(Entity.DELETE).is(false);
        query.addCriteria(criteria);
        mongoTemplate.updateMulti(query, update, this.getEntityClass(), getTableName());
    }

    @Override
    public T findById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        Criteria criteria = new Criteria(Entity.ID).is(id).and(Entity.DELETE).is(false);
        Query query = Query.query(criteria);
        return mongoTemplate.findOne(query, this.getEntityClass(), getTableName());
    }

    @Override
    public T findOne(Query query) {
        Criteria criteria = new Criteria();
        criteria.and(Entity.DELETE).is(false);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, getEntityClass(), getTableName());
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getEntityClass() {
        Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return entityClass;
    }

    public String getTableName() {
        String tableName = null;
        Class<T> entityClass = getEntityClass();
        if (entityClass.getAnnotations().length > 0) {
            Document annotation = AnnotatedElementUtils.findMergedAnnotation(entityClass, Document.class);
            if (annotation != null && StringUtils.isNotEmpty(annotation.collection())) {
                tableName = annotation.collection();
            }
        } else {
            tableName = mongoTemplate.getCollectionName(entityClass);
        }
        return tableName;
    }

    @Override
    public void remove(String... ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        mongoTemplate.updateMulti(Query.query(Criteria.where(Entity.ID).in(ids)), Update.update(Entity.DELETE, true), getTableName());
    }

    /**
     * 根据条件删除 软删除操作
     */
    @Override
    public void remove(Query query, Update update) {
        update.set(Entity.DELETE, true);
        mongoTemplate.updateFirst(query, update, getTableName());
    }

    @Override
    public Page<T> findPage(Query query, Pageable pageable) {
        query.addCriteria(new Criteria(Entity.DELETE).is(false));
        long total = mongoTemplate.count(query, this.getEntityClass());
        List<T> list = mongoTemplate.find(query.with(pageable), this.getEntityClass());
        Page<T> page = new PageImpl<T>(list, pageable, total);
        return page;
    }

    @Override
    public Page<T> findAll(Map<String, Object> searchMap, Pageable pageable) {
        Criteria criteria = new Criteria();
        criteria.and(Entity.DELETE).is(false);
        Query query = Query.query(criteria);
        return findPage(query, pageable);
    }

    @Override
    public List<T> findByIds(List<String> ids) {
        Criteria criteria = new Criteria();
        criteria.and(Entity.DELETE).is(false);
        criteria.and(Entity.ID).in(ids);
        Query query = Query.query(criteria);
        List<T> list = mongoTemplate.find(query, this.getEntityClass());
        return list;
    }

    public List<T> find(T t) {
        List<T> list = mongoTemplate.find(build(t), this.getEntityClass());
        return list;
    }

    @Override
    public List<T> find(Query query) {
        Criteria criteria = new Criteria();
        criteria.and(Entity.DELETE).is(false);
        query.addCriteria(criteria);
        List<T> list = mongoTemplate.find(query, this.getEntityClass());
        return list;
    }

    @Override
    public long count(Query query) {
        Criteria delete = new Criteria();
        delete.and(Entity.DELETE).is(false);
        query.addCriteria(delete);
        return mongoTemplate.count(query, this.getEntityClass());
    }

    @Override
    public boolean exist(Query query) {
        Criteria criteria = new Criteria();
        criteria.and(Entity.DELETE).is(false);
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, getTableName());
    }

    private Query build(T t) {
        Criteria criteria = new Criteria();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                Object v = FieldUtils.readDeclaredField(t, field.getName(), true);
                if (null != v) {
                    criteria.and(field.getName()).is(v);
                }
            } catch (Exception ignore) {
            }
        }
        criteria.and(Entity.DELETE).is(false);
        return new Query(criteria);
    }

    protected void updater(Update update, String oid) {
        update.set("updater", oid);
    }
}
