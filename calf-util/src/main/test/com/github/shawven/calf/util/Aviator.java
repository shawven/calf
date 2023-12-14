package com.github.shawven.calf.util;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.RegExUtils;
import org.junit.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xw
 * @date 2023/11/13
 */
public class Aviator {

    @Test
    public void compile() throws IOException {
        String path = "D:\\Workspace\\calf\\calf-util\\src\\main\\test\\com\\github\\shawven\\calf\\util\\";
        Expression expression = AviatorEvaluator.getInstance().compileScript(path + "new.av", true);
        expression.execute();
    }

    @Test
    public void compile2() throws IOException {
//        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
//        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_INTEGRAL_NUMBER_INTO_DECIMAL, true);

        ImmutableMap<String, Object> args = ImmutableMap.of("a", 0.1, "b", 0.2, "c", 3.0);
        String expression = """
                (a + b) / 2        
                """;
        System.out.println(AviatorEvaluator.getInstance().execute(expression, args));
        System.out.println(AviatorEvaluator.getInstance().execute(expression, JSONObject.from(args)));
    }

    @Test
    public void simpleTest(){
        String expression = "3 + 2 * 6";
        Object result = AviatorEvaluator.execute(expression);
        System.out.println(result);


        SpelExpressionParser parser = new SpelExpressionParser();
        System.out.println(parser.parseExpression("T(Math).pow(2, T(org.apache.commons.lang3.RandomUtils).nextInt(1,9))").getValue());
    }

    @Test
    public void simpleTest2(){
        AviatorEvaluator.setOption(Options.NIL_WHEN_PROPERTY_NOT_FOUND, true);

        String expression = "#b.c.d";
        ImmutableMap<String, Object> env = ImmutableMap.of(
                "a", 1,
                "b", ImmutableMap.of("d", 1)
        );
        System.out.println(AviatorEvaluator.execute(expression, env));

        AviatorEvaluator.setOption(Options.NIL_WHEN_PROPERTY_NOT_FOUND, false);

//        System.out.println(AviatorEvaluator.execute(expression, env));
    }


    @Test
    public void variableTest(){
        // 1.定义变量
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("job", "程序员");
        map.put("a", "1");
        map.put("b", "2");

        // 2.定义表达式
        String exp = "'你好，我是'+ name + '，我的职业是' + job + '，很高兴认识你 :' + a + b";

        // 3.使用Aviator执行表达式
        Object result = AviatorEvaluator.execute(exp, map);

        // 4.输出结果
        System.out.println(result);

    }

    @Test
    public void customFuncTest1(){
        // 将自定义函数注册到Aviator中
        AviatorEvaluator.addFunction(new Function());

        // 执行
        Long result = (Long) AviatorEvaluator.execute("customFunc(50,20)");

        // 输出结果
        System.out.println(result);
    }

    @Test
    public void customFuncTest2(){
        // 声明变量
        Map<String, Object> map = new HashMap<>();
        map.put("a", 50);
        map.put("b", 20);
        // 将自定义函数注册到Aviator中
        AviatorEvaluator.addFunction(new Function());

        // 执行
        Long result = (Long) AviatorEvaluator.execute("customFunc(a,b)", map);

        // 输出结果
        System.out.println(result);
    }

    @Test
    public void customSequence(){
        // 声明变量
        Map<String, Object> map = new HashMap<>();
        map.put("a", 50);
        map.put("b", 20);

        // 执行
        Object execute = AviatorEvaluator.execute("""
                        let r = range(-5, 5);
                        let es = filter(r, lambda(x) -> x %2 == 0 end);
                        println("bs is: "  + es);
                """, map);

        // 输出结果
        System.out.println(execute);
    }


    public static class Function extends AbstractFunction {

        @Override
        public String getName() {
            return "customFunc";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
            Number num1 = arg1.numberValue(env);
            Number num2 = arg2.numberValue(env);
            Long sum = num1.longValue() + num2.longValue();
            return AviatorLong.valueOf(sum);
        }

    }

    @Test
    public void testFlot() {
        System.out.println(AviatorEvaluator.execute("0.1 + 0.2"));
        System.out.println(AviatorEvaluator.execute("0.01 + 2.01"));

        AviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);

        System.out.println(AviatorEvaluator.execute("0.1 + 0.2"));
        System.out.println(AviatorEvaluator.execute("0.01 + 2.01"));

    }

    @Test
    public void testFormCalc() {
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
                       "b":4
                   }
                """;

        String exp = "(#{56f7dcc0538a40ac8766a443b38b9adc.Mo_0} * #{56f7dcc0538a40ac8766a443b38b9adc.Mo_0}) / 2" +
                " ++ #{56f7dcc0538a40ac8766a443b38b9adc.Mo_1}";


        String newExp = RegExUtils.replaceAll(exp, "(?<=#)\\{(.+?)\\}", "$1");
        Expression expression = AviatorEvaluator.compile(newExp);

        Object result = expression.execute(JSONObject.parseObject(str));

        System.out.println(result);
    }

    @Test
    public void testFormCalc2() throws IOException {
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
                       }
                   }
                """;

        String exp = """
                p(__args__);
                return (#{56f7dcc0538a40ac8766a443b38b9adc.Mo_0} * #{56f7dcc0538a40ac8766a443b38b9adc.Mo_0}) / 2" +
                                       " ++ #{56f7dcc0538a40ac8766a443b38b9adc.Mo_1}
                                
                """;

        Object result = AviatorEvaluator.execute(exp, JSONObject.parseObject(str));
        System.out.println(result);
    }

}
