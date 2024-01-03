package com.github.shawven.calf.util.functions;

import com.googlecode.aviator.exception.CompileExpressionErrorException;
import com.googlecode.aviator.runtime.RuntimeFunctionDelegator;
import com.googlecode.aviator.runtime.RuntimeUtils;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.function.LambdaFunction;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author xw
 * @date 2023/12/7
 */
public interface LightLambda {

    String ELEMENT = "__elem__";

    /**
     * 构建LambdaFunction
     *
     * @param env
     * @param arg
     * @param arity
     * @return
     */
    default AviatorObject buildFunction(Map<String, Object> env, AviatorObject arg, int arity) {
        String script;

        switch (arg.getAviatorType()) {
            case BigInt:
            case Decimal:
            case Long:
            case Double:
                script = String.valueOf(FunctionUtils.getNumberValue(arg, env));
                break;
            case String:
                script = FunctionUtils.getStringValue(arg, env);
                break;
            case Boolean:
                script = String.valueOf(FunctionUtils.getBooleanValue(arg, env));
                break;
            case Lambda:
                return arg;
            case JavaType:
                AviatorFunction function = FunctionUtils.getFunction(arg, env, arity);
                // 非函数
                if (function != null) {
                    // 函数是运行时函数
                    if (function instanceof RuntimeFunctionDelegator) {
                        // 判断运行时函数是否存在
                        Method method = ReflectionUtils.findMethod(RuntimeFunctionDelegator.class, "tryGetFuncFromEnv", Map.class);
                        if (method != null) {
                            method.setAccessible(true);
                            Object result = ReflectionUtils.invokeMethod(method, function, env);
                            if (result != null) {
                                return (AviatorObject) function;
                            }
                        }
                    } else {
                        return (AviatorObject) function;
                    }
                }
                script = ((AviatorJavaType) arg).getName();
                break;
            default:
                throw new CompileExpressionErrorException(arg.desc(env) + " is not a lambda expression");
        }

        String expression = String.format("lambda(%s) -> %s end", ELEMENT, script);
        return (LambdaFunction)  RuntimeUtils.getInstance(env).execute(expression, env, true);
    }
}
