package com.github.shawven.calf.examples.oauth2.mapper;

import com.github.shawven.calf.examples.oauth2.domain.RbacRole;
import com.github.shawven.calf.examples.oauth2.mapper.base.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 账套角色关联表 Mapper 接口
 * </p>
 *
 * @author Generator
 * @date 2020-03-02
 */
public interface RbacRoleMapper extends BaseMapper<RbacRole> {

    /**
     * 查找角色列表
     *
     * @param ids
     * @return
     */
    List<RbacRole> selectEnabledRolesByIds(List<Long> ids);


    /**
     * 查询用户该账套拥有的角色
     *
     * @param userId
     * @param enterpriseId
     * @param accountId
     * @return
     */
    String selectAccountOwnedRoleIds(@Param("userId") Long userId,
                                     @Param("enterpriseId") Long enterpriseId,
                                     @Param("accountId") Long accountId);
}
