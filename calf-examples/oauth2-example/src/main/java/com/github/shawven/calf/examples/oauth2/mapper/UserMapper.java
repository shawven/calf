package com.github.shawven.calf.examples.oauth2.mapper;

import com.github.shawven.calf.examples.oauth2.domain.User;
import com.github.shawven.calf.examples.oauth2.mapper.base.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Generator
 * @date 2019-11-06
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 更新登录记录
     *
     * @param user
     */
    boolean updateLoginRecord(User user);

    /**
     * 禁止访问
     *
     * @param user
     */
    boolean updateErrorCountAndDisabled(User user);

    /**
     * @param userId
     * @param enterpriseId
     * @return
     */
    boolean saveChooseAccountIdAndEnterpriseId(@Param("userId") Long userId,
                                               @Param("accountId") Long accountId,
                                               @Param("enterpriseId") Long enterpriseId);
    /**
     * 清除选择的账套I
     *
     * @param userId
     * @return
     */
    boolean removeChooseAccountId(Long userId);
    /**
     * 清除选择的账套ID和企业ID
     *
     * @param userId
     * @return
     */
    boolean removeChooseAll(Long userId);

    /**
     * 清除这个已选择的账套
     *
     * @param accountId
     * @return
     */
    int removeThisChooseAccountId(Long accountId);
}
