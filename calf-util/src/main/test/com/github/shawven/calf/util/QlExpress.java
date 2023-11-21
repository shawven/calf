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
                        "56f7dcc0538a40ac8766a443b38b9adc": {
                          "_S_TITLE": "肖文的基础资料",
                          "_S_SERIAL": "",
                          "_S_APPLY": [
                            "604aca0ce4b093bbf35ada1e"
                          ],
                          "_S_DEPT": [
                            "6840e7aa-5b62-11ed-9523-2298c557103a"
                          ],
                          "_S_DATE": 1699867605393,
                          "Nu_0": 2,
                          "Mo_0": 3,
                          "Mo_1": 2,
                       },
                       "a":3,
                       "b":4,
                       "abc": {
                         "a":3,
                         "b":4
                       }
                       
                   }
                """;

        String exp = "(#{56f7dcc0538a40ac8766a443b38b9adc.Mo_0} * #{56f7dcc0538a40ac8766a443b38b9adc.Mo_0}) / 2" +
                " ++ #{56f7dcc0538a40ac8766a443b38b9adc.Mo_1}";


        String newExp = RegExUtils.replaceAll(exp, "(?<=#)\\{(.+?)\\}", "$1");

        DefaultContext<String, Object> context = new DefaultContext<String, Object>();
        context.putAll(JSONObject.parseObject(str));
        Object execute = new ExpressRunner().execute(newExp, context, null, true, false);

        System.out.println(execute);
    }

}
