package com.github.shawven.calf.util;

import com.alibaba.fastjson2.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.apache.commons.lang3.RegExUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xw
 * @date 2023/11/13
 */
public class QlExpress {


    @Test
    public void testFormCalc() throws Exception {
        String str = """
                   {
                       "a":1,
                       "b":2,
                       "c":3.0
                   }
                """;

        String exp = "(a * b) / c";


        DefaultContext<String, Object> context = new DefaultContext<String, Object>();
        context.putAll(JSONObject.parseObject(str));
        Object execute = new ExpressRunner().execute(exp, context, null, true, false);

        System.out.println(execute);
    }

}
