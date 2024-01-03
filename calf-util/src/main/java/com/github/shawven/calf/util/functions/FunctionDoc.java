package com.github.shawven.calf.util.functions;


import lombok.Getter;

/**
 * @author xw
 * @date 2023/12/9
 */
@Getter
public class FunctionDoc {

    private String desc;

    private String usage;

    private String example;

    public static FunctionDoc builder() {
        return new FunctionDoc();
    }

    public FunctionDoc desc(String text) {
        this.desc = text;
        return this;
    }

    public FunctionDoc usage(String text) {
        this.usage = text;
        return this;
    }

    public FunctionDoc example(String text) {
        this.example = text;
        return this;
    }
}
