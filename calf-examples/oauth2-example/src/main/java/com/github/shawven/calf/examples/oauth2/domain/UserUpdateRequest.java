package com.github.shawven.calf.examples.oauth2.domain;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Shoven
 * @date 2020-03-09
 */
@Data
public class UserUpdateRequest {

    private Long id;

    private String username;

    private String nickname;

    private String fullName;

    public User newUser() {
        if (StringUtils.isBlank(username)) {
            username = null;
        }
        return new User()
                .setId(id)
                .setUsername(username)
                .setNickname(nickname)
                .setFullName(fullName);
    }
}
