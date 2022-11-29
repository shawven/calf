package com.example.nativepractice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author xw
 * @date 2022/11/25
 */
@Data
public class UserCreateDTO {

    @NotBlank(message = "名字不为空")
    private String name;
}
