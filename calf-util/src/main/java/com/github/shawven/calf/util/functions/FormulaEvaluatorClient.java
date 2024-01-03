package com.github.shawven.calf.util.functions;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公式计算器客户端
 *
 * @author xw
 * @date 2023/11/21
 */
public class FormulaEvaluatorClient {

    private static final Logger logger = LoggerFactory.getLogger(FormulaEvaluatorClient.class);

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        // 传入的变量需要自行保证类型正确，保持与计算引擎一致
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
        OBJECT_MAPPER.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
    }

    public static Map<String, List<FunctionSpecificationDTO>> listFunctionsSpecs() {
        Map<FunctionTypeEnum, List<FunctionSpecification>> functionLibs = FormulaEvaluator.functionLib;

        Map<String, List<FunctionSpecificationDTO>> result = new LinkedHashMap<>();

        functionLibs.forEach((k, v) -> {
            List<FunctionSpecificationDTO> value = v.stream().map(fn -> {
                FunctionSpecificationDTO spec = new FunctionSpecificationDTO();
                spec.setName(fn.getName());
                spec.setOutput(fn.output() != null ? fn.output().name(): "");

                FunctionDoc doc = fn.doc();
                if (doc != null) {
                    spec.setDesc(doc.getDesc());
                    spec.setUsage(doc.getUsage());
                    spec.setExample(doc.getExample());
                }
                return spec;
            }).collect(Collectors.toList());

            result.put(k.name(), value);
        });

        return result;
    }


    /**
     * 执行表达式
     *
     * @param request 表达式
     * @return
     */
    public static FormulaEvaluationResult execute(FormulaEvaluationRequest request) {
        FormulaTypeEnum returnType = request.getReturnType();
        try {
            Object result = FormulaEvaluator.execute(request.getExpression(), adaptArgs(request.getArgs()));
            Object value = returnType == null ? result : adaptValue(result, returnType);
            return FormulaEvaluationResult.success(value);
        } catch (FormulaException e) {
            logger.error(e.getMessage(), e);
            Object value = returnType == null ? null : returnType.getDefaultValue();
            return FormulaEvaluationResult.error(value, 500, e.getMessage());
        }
    }


    /**
     * 执行表达式，无编译缓存
     *
     * @param request
     * @return
     */
    public static FormulaEvaluationResult executeOnce(FormulaEvaluationRequest request) {
        FormulaTypeEnum returnType = request.getReturnType();
        try {
            Object result = FormulaEvaluator.executeOnce(request.getExpression(), adaptArgs(request.getArgs()));

            Object value = returnType == null ? result : adaptValue(result, returnType);
            return FormulaEvaluationResult.success(value);
        } catch (FormulaException e) {
            logger.error(e.getMessage(), e);
            Object value = returnType == null ? null : returnType.getDefaultValue();
            return FormulaEvaluationResult.error(value, 500, e.getMessage());
        }
    }

    /**
     * 获取表达式变量
     *
     * @param expression 表达式
     * @return
     */
    public static List<String> getVars(String expression) {
        try {
            return FormulaEvaluator.compileExpression(expression).getVariableFullNames();
        } catch (FormulaException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 适应参数
     *
     * @param args 参数
     * @return
     */
    private static Map<String, Object> adaptArgs(Map<String, Object> args) {
        if (args == null || args.isEmpty()) {
            return Collections.emptyMap();
        }
        return OBJECT_MAPPER.convertValue(args, Map.class);
    }

    /**
     * 适应值
     *
     * @param value 值
     * @param type  类型
     * @return
     */
    private static Object adaptValue(Object value, FormulaTypeEnum type) {
        if (value == null) {
            return type.getDefaultValue();
        }
        switch (type) {
            case number: {
                if (value instanceof BigDecimal) {
                    return value;
                }

                // 尝试转数字
                String string = String.valueOf(value);
                if (NumberUtils.isCreatable(string)) {
                    return NumberUtils.createBigDecimal(string);
                }
            }

            case string: {
                if (value instanceof CharSequence) {
                    return value;
                }
                // 转字符串
                return String.valueOf(value);
            }

            case timestamp: {
                return 0;
            }

            case bool: {
                if (value instanceof Boolean) {
                    return value;
                }

                // 数字判断大于0
                String string = String.valueOf(value);
                if (NumberUtils.isCreatable(string)) {
                    return BooleanUtils.toBoolean(NumberUtils.createInteger(string));
                }

                return false;
            }

            case array: {
                if (value.getClass().isArray()) {
                    return value;
                }
                return Collections.singletonList(value);
            }

            case object: {
                return value;
            }

            default:
        }
        return type.getDefaultValue();
    }
}
