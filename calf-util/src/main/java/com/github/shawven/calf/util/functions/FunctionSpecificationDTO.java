package com.github.shawven.calf.util.functions;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xw
 * @date 2023/12/9
 */
@Data
public class FunctionSpecificationDTO implements Serializable {

    private String name;

    private String output;

    private String desc;

    private String usage;

    private String example;
}
