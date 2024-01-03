package com.github.shawven.calf.util.functions;

import com.github.shawven.calf.util.functions.ast.ExpressionAstParser;
import com.github.shawven.calf.util.functions.date.DATEFORMAT;
import com.github.shawven.calf.util.functions.date.NOW;
import com.github.shawven.calf.util.functions.date.TIMESTAMP;
import com.github.shawven.calf.util.functions.high.*;
import com.github.shawven.calf.util.functions.logic.*;
import com.github.shawven.calf.util.functions.math.*;
import com.github.shawven.calf.util.functions.string.TEXT;
import com.github.shawven.calf.util.functions.string.*;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.googlecode.aviator.*;
import com.googlecode.aviator.exception.ExpressionSyntaxErrorException;
import com.googlecode.aviator.lexer.token.OperatorType;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.core.internal.Function;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.shawven.calf.util.functions.FunctionTypeEnum.*;


/**
 * 公式计算
 *
 * @author xw
 * @date 2023/11/20
 */
class FormulaEvaluator {

    public static Map<FunctionTypeEnum, List<FunctionSpecification>> functionLib = new LinkedHashMap<>();

    private static Map<String, Function<String, String>> beforeCompliers = new ConcurrentHashMap<>();

    private static final AviatorEvaluatorInstance INSTANCE = AviatorEvaluator.getInstance();

    /**
     * 快速hash函数
     */
    static HashFunction hash = Hashing.murmur3_32();

    static {
        setOptions();
        loadFunctions();
    }

    private static void setOptions() {
        // 在new语句和静态方法调用中允许使用的类白名单 null:无限制, empty: 禁止所有
        INSTANCE.setOption(Options.ALLOWED_CLASS_SET, Collections.emptySet());
        INSTANCE.setOption(Options.ASSIGNABLE_ALLOWED_CLASS_SET, Collections.emptySet());

        // 语法糖访问(a.b.c)未找到属性时返回nil
        INSTANCE.setOption(Options.NIL_WHEN_PROPERTY_NOT_FOUND, true);

        // 循环最大次数 默认 0 表示无限制
        INSTANCE.setOption(Options.MAX_LOOP_COUNT, 10000);

        // 高精度运算， 强制将所有浮点数（包括科学计数法）都解析为 decimal 类型,
        // 仅仅对表达式里出现的浮点数字面量( literal ) 有效，传入的变量需要自行保证类型正确
        INSTANCE.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);

        // 启用特性
        INSTANCE.setOption(Options.FEATURE_SET, Feature.asSet(Feature.Lambda));
    }

    private static void loadFunctions() {
        try {
            // operator
            addOperator(OPERATOR, OperatorType.ADD.getToken());
            addOperator(OPERATOR, "-");
            addOperator(OPERATOR, OperatorType.MULT.getToken());
            addOperator(OPERATOR, OperatorType.DIV.getToken());
            addOperator(OPERATOR, OperatorType.MOD.getToken());
            addOperator(OPERATOR, OperatorType.GT.getToken());
            addOperator(OPERATOR, OperatorType.GE.getToken());
            addOperator(OPERATOR, OperatorType.LT.getToken());
            addOperator(OPERATOR, OperatorType.LE.getToken());
            addOperator(OPERATOR, OperatorType.EQ.getToken());
            addOperator(OPERATOR, OperatorType.NEQ.getToken());

            // logic
            addFunction(LOGIC, new AND());
            addFunction(LOGIC, new OR());
            addFunction(LOGIC, new XOR());
            addFunction(LOGIC, new NOT());
//            addFunction(new EQ());
//            addFunction(new NE());
//            addFunction(new GT());
//            addFunction(new GE());
//            addFunction(new LT());
//            addFunction(new LE());
            addFunction(LOGIC, new IF());
            addFunction(LOGIC, new CASE());
            addFunction(LOGIC, new ISEMPTY());
            addFunction(LOGIC, new ISNULL());

            // math
            addFunction(MATH, new ABS());
            addFunction(MATH, new AVERAGE());
            addFunction(MATH, new FIXED());
            addFunction(MATH, new ROUND());
            addFunction(MATH, new MAX());
            addFunction(MATH, new MIN());
            addFunction(MATH, new POW());
            addFunction(MATH, new SQRT());
            addFunction(MATH, new SUM());

            // string
            addFunction(TEXT, new CONCATENATE());
            addFunction(TEXT, new CONTAINS());
            addFunction(TEXT, new ENDSWITH());
            addFunction(TEXT, new LEFT());
            addFunction(TEXT, new LEN());
            addFunction(TEXT, new RIGHT());
            addFunction(TEXT, new SPLIT());
            addFunction(TEXT, new STARTSWITH());
            addFunction(TEXT, new TEXT());
            addFunction(TEXT, new TRIM());
            addFunction(TEXT, new VALUE());

            // date
            addFunction(DATE, new NOW());
            addFunction(DATE, new TIMESTAMP());
            addFunction(DATE, new DATEFORMAT());

            // hign
            addFunction(HIGH, new ARRAY());
            addFunction(HIGH, new ARRAYGET());
            addFunction(HIGH, new FILTER());
            addFunction(HIGH, new INCLUDE());
            addFunction(HIGH, new MAP());
            addFunction(HIGH, new RAND());
            addFunction(HIGH, new REVERSE());
            addFunction(HIGH, new SORT());
            addFunction(HIGH, new CURRENTDEPT());
            addFunction(HIGH, new CURRENTUSER());
            addFunction(HIGH, new USERDEPT());
            addFunction(HIGH, new USERNAME());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加操作符
     *
     * @param operator
     */
    public static void addOperator(FunctionTypeEnum functionType, String operator) {
        functionLib.computeIfAbsent(functionType, k -> new ArrayList<>())
                .add(() -> operator);
    }

    /**
     * 添加函数
     *
     * @param function
     */
    public static void addFunction(FunctionTypeEnum functionType, AviatorFunction function) {
        INSTANCE.addFunction(function);

        if (function instanceof FunctionSpecification) {
            functionLib.computeIfAbsent(functionType, k -> new ArrayList<>())
                    .add((FunctionSpecification) function);
        }

        // lambada函数
        if (function instanceof LightLambda) {
            // 借助spring ast 语法解析, 把lambda表达式body变成字符串
            beforeCompliers.put(function.getName(), lightLambdaProcessor(function));
        }
    }

    /**
     * 轻量级lambda 函数体处理器，
     * 例如__elem__ % 2 == 0  解析为 '__elem__ % 2 == 0',
     * 再通过 LightLambda转成Lambda函数：lambda(__elem__) -> __elem__ % 2 == 0 end
     * @see LightLambda
     * @param function
     * @return
     */
    private static Function<String, String> lightLambdaProcessor(AviatorFunction function) {
        return exp -> {
            int[] position = ExpressionAstParser.methodArgFinder()
                    .setExpression(exp)
                    .setSearchMethod(function.getName())
                    .setArgIndex(1)
                    .getPosition();
            int start = position[0];
            int end = position[1];

            String substr = StringUtils.substring(exp, start, end);
            // 最外层是函数调用，比如：RAND([任何情况])
            if (ExpressionAstParser.isMethod(substr)) {
                // 未引用元素变量，直接返回，比如RAND(10)，不是RAND(_elem__)这种
                if (!substr.contains(LightLambda.ELEMENT)) {
                    return exp;
                }
            }
            // 单独只有一个函数引用，即只有函数名没有函数括号，比如:RAND
            if (INSTANCE.getFuncMap().keySet().stream().anyMatch(substr::equals)) {
                return exp;
            }
            // 转字符串
            String prefix = StringUtils.substring(exp, 0, start);
            String suffix = StringUtils.substring(exp, end);
            return prefix +  "'" + substr + "'" + suffix;
        };
    }

    /**
     * 编译器前拦截器
     *
     * @param expression
     * @return
     */
    private static String compileBeforeInterceptor(String expression) {
        try {
            for (Map.Entry<String, Function<String, String>> entry : beforeCompliers.entrySet()) {
                if (expression.contains(entry.getKey())) {
                    expression = entry.getValue().apply(expression);
                }
            }
        } catch (Exception ignored) {
            // 不抛出异常
            throw ignored;
        }
        return expression;
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @return           结果
     */
    public static Object execute(String expression) {
        return execute(expression, Collections.emptyMap());
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @param args       参数
     * @return           结果
     */
    public static Object execute(String expression, Map<String, Object> args) {
        Expression exp = compileExpression(expression);

        Map<String, Object> env = exp.newEnv();
        env.putAll(args);

        return executeExpression(exp, env);
    }

    /**
     * 执行表达式一次（无缓存）
     *
     * @param expression 表达式
     * @return           结果
     */
    public static Object executeOnce(String expression) {
        return execute(expression, Collections.emptyMap());
    }

    /**
     * 执行表达式一次（无缓存）
     *
     * @param expression 表达式
     * @param args       参数
     * @return           结果
     */
    public static Object executeOnce(String expression, Map<String, Object> args) {
        Expression exp = compileExpression(expression, expression, false);

        Map<String, Object> env = exp.newEnv();
        env.putAll(args);

        return executeExpression(exp, env);
    }

    /**
     * 编译表达式，出错抛出异常
     *
     * @param expression 表达式
     * @return
     */
    public static Expression compileExpression(String expression) {
        String cacheKey = hashCode(expression);
        return compileExpression(cacheKey, expression, true);
    }

    /**
     * 编译表达式，出错抛出异常
     *
     * @param cacheKey   缓存key
     * @param expression 表达式
     * @param cache      是否缓存
     * @return           编译后的表达式对象
     */
    public static Expression compileExpression(String cacheKey, String expression, boolean cache) {
        try {
            // 未缓存
            if (!INSTANCE.isExpressionCached(cacheKey)) {
                expression = compileBeforeInterceptor(expression);
            }

            return INSTANCE.compile(cacheKey, expression, cache);
        } catch (ExpressionSyntaxErrorException e) {
            throw new FormulaExpressionException(e);
        } catch (Exception e) {
            throw new FormulaExpressionException(e);
        }
    }

    /**
     * 执行表达式，出错返回null值
     *
     * @param expression 表达式
     * @param env        环境变量
     * @return           结果
     */
    public static Object executeExpression(Expression expression, Map<String, Object> env) {
        try {
            return expression.execute(env);
        } catch (Exception e) {
            throw new FormulaExpressionException( e);
        }
    }

    /**
     * 表达式hash
     *
     * @param expression 表达式
     * @return           hashKey
     */
    private static String hashCode(String expression) {
        return hash.hashString(expression, StandardCharsets.UTF_8).toString();
    }
}

