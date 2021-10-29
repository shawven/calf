package com.github.shawven.calf.examples.oauth2.support;

import com.github.shawven.calf.examples.oauth2.domain.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 上下文数据
 *
 * @author Shoven
 * @date 2019-11-14
 */
@Data
@Accessors(chain = true)
public class CtxData implements Serializable {

    private static final long serialVersionUID = -2060986206122982395L;

    /**
     * 用户上下文
     */
    @Getter(AccessLevel.NONE)
    private transient CtxDataAccessor ctxDataAccessor;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 管理员
     */
    private boolean admin;

    /**
     * 会话管理
     */
    private Map<String, String> clientSession;

    /**
     * 角色权限列表
     */
    private List<Integer> permissions;

    /**
     * 不允许外部构造
     */
    private CtxData() {}

    /**
     * 设置当前用户
     *
     * @param user 用户信息
     */
    public CtxData setUser(User user) {
        this.userId = user.getId();
        this.phone = user.getPhone();
        return this;
    }

    /**
     * 返回匿名用户上下文
     *
     * @return 上下文数据
     */
    public static CtxData anonymous() {
        User anonymous = new User()
                .setId(0L)
                .setPhone("")
                .setUsername("anonymous");
        return new CtxData().setUser(anonymous);
    }

    /**
     * 是否匿名用户
     *
     * @return 判断结果
     */
    public boolean isAnonymous() {
        return userId == 0L;
    }

    /**
     * 上下文持久化
     *
     */
    public void save() {
        if (isAnonymous()) {
            throw new IllegalStateException("匿名用户无法保存数据");
        }
        // 缓存过期时间要大于token过期时间
        ctxDataAccessor.save(this);
    }

    @Override
    public String toString() {
        return "ContextData{" +
                ", userId=" + userId +
                ", phone='" + phone + '\'' +
                '}';
    }
}
