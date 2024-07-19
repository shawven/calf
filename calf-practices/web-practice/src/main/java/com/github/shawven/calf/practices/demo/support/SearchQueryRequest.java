package com.github.shawven.calf.practices.demo.support;

import com.github.shawven.calf.practices.demo.support.validator.EnumValue;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author xw
 * @date 2021/10/12
 */
@Data
public class SearchQueryRequest {

    @NotBlank(message = "名词不能为空")
    private String name;

    @Min(message = "不能小于1", value = 1)
    @NotNull
    private Long num;

    @EnumValue(message = "枚举有误", enumClass = Type.class)
    private Type type;


    enum Type {
        ONE, TWO, THREE, FOUR
    }
}
