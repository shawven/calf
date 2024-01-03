package com.github.shawven.calf.util.functions.ast;

import com.github.shawven.calf.util.ReflectHelpers;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.ast.*;
import org.springframework.expression.spel.standard.SpelExpression;

import java.util.function.Predicate;

/**
 * 表达式语法树解析器
 *
 * @author xw
 * @date 2023/12/8
 */

public class ExpressionAstParser {

    private static final ExpressionParser PARSER = new CustomSpelExpressionParser(new SpelParserConfiguration());


    /**
     * 节点是方法
     *
     * @param expression
     * @return
     */
    public static boolean isMethod(String expression) {
        return parse(expression) instanceof MethodReference;
    }

    /**
     * 获取语法数
     *
     * @param expression
     * @return
     */
    public static SpelNode parse(String expression) {
        try {
            SpelExpression spelExpression = (SpelExpression) PARSER.parseExpression(expression);
            return (SpelNode) ReflectHelpers.getProperty(spelExpression, "ast");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查找节点
     *
     * @param node
     * @param predicate
     * @return
     */
    static SpelNode findNode(SpelNode node, Predicate<SpelNode> predicate) {
        if (predicate.test(node)) {
            return node;
        }

        int childCount = node.getChildCount();
        if (childCount == 0) {
            return null;
        }

        for (int i = 0; i < childCount; i++) {
            SpelNode child = node.getChild(i);

            SpelNode result = findNode(child, predicate);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 操作符查找器
     *
     * @return
     */
    public static OpFinder opFinder() {
        return new OpFinder();
    }

    @Setter
    @Accessors(chain = true)
    public static class OpFinder {

        public static final String PLUS = "+";
        public static final String MINUS = "-";
        public static final String MULTIPLY = "*";
        public static final String DIVIDE = "/";

        /**
         * 表达式
         */
        private String expression;

        /**
         * 变量
         */
        private String variable;

        public String getOperator() {
            SpelNode root = parse(expression);

            // 查找变量
            PropertyOrFieldReference property = (PropertyOrFieldReference) findNode(root, spelNode -> {
                if (spelNode instanceof PropertyOrFieldReference) {
                    return ((PropertyOrFieldReference) spelNode).getName().equals(variable);
                }
                return false;
            });

            if (property == null) {
                throw new IllegalArgumentException("Illegal expression: " + expression + " not found variable：" + variable);
            }

            // 获取父节点
            SpelNode parent;
            try {
                parent = (SpelNode) ReflectHelpers.getProperty(property, "parent");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (!(parent instanceof Operator)) {
                throw new IllegalArgumentException("Illegal expression: " + expression);
            }

            Operator operator = (Operator) parent;
            if (operator instanceof OpPlus
                    || operator instanceof OpMinus
                    || operator instanceof OpMultiply
                    || operator instanceof OpDivide) {
                return operator.getOperatorName();
            }

            throw new IllegalArgumentException("Illegal expression: " + expression
                    + " not supported operator：" + operator.getOperatorName());
        }
    }

    /**
     * 方法参数查找器
     *
     * @return
     */
    public static MethodArgFinder methodArgFinder() {
        return new MethodArgFinder();
    }

    @Setter
    @Accessors(chain = true)
    public static class MethodArgFinder {

        /**
         * 表达式
         */
        private String expression;

        /**
         * 方法
         */
        private String searchMethod;

        /**
         * 参数位置
         */
        private int argIndex;

        /**
         * 获取参数在主表达式中的起始和终止索引位置
         *
         * @return
         */
        public int[] getPosition() {
            SpelNode root = parse(expression);

            // 查找方法
            MethodReference method = (MethodReference) findNode(root, spelNode -> {
                if (spelNode instanceof MethodReference) {
                    return ((MethodReference) spelNode).getName().equals(searchMethod);
                }
                return false;
            });

            // 获取参数阶段
            SpelNode argNode = method.getChild(argIndex);

            int startIndex;
            int endIndex;

            if (argNode instanceof MethodReference) {
                // 如果是方法引用
                startIndex = argNode.getStartPosition();
                endIndex = getEndIndexInWholeNode(argNode) + 1;
            } else {
                startIndex = getStartIndexInWholeNode(argNode);
                endIndex = getEndIndexInWholeNode(argNode);
            }

            return new int[]{startIndex, endIndex};
        }

        private static int getStartIndexInWholeNode(SpelNode node) {
            if (node.getChildCount() == 0) {
                return node.getStartPosition();
            }
            return getStartIndexInWholeNode(node.getChild(0));
        }

        private static int getEndIndexInWholeNode(SpelNode node) {
            if (node.getChildCount() == 0) {
                return node.getEndPosition();
            }
            return getEndIndexInWholeNode(node.getChild(node.getChildCount() - 1));
        }
    }
}
