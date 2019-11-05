package com.starter.demo.common;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * 节点树，主要应用于无限极菜单树、分类树等类似结构
 *
 * @author Shoven
 * @date 2019-03-18 16:43
 */
public class NodeTree<T> implements Serializable {

    private List<T> data;

    /**
     * 根节点过滤器
     */
    private Predicate<T> rootFilter;

    /**
     * 子节点过滤器
     */
    private BiPredicate<T, T> childFilter;

    /**
     * 节点转换器
     */
    private Function<T, Node> nodeConvert;

    public NodeTree(List<T> data) {
        this.data = data;
    }

    /**
     * 生成树结构列表
     * 改为非递归，避免大数据量时栈溢出
     *
     * @return 树形节点集合
     */
    public <N extends Node<N>> List<N> generate() {
        if (data == null || data.isEmpty()) {
            return null;
        }

        requireNonNull(rootFilter, "父节点选择器不能为空");
        requireNonNull(childFilter, "孩子节点选择器不能为空");
        requireNonNull(nodeConvert, "节点转换器不能为空");

        // 寻找根节点列表
        List<T> roots = getRoots(data, rootFilter);
        // ①优化：排除根节点列表
        data.removeAll(roots);

        List<N> newNodes = new ArrayList<>();

        // 遍历根节点
        for (T root : roots) {
            // 保存原来的数据
            Stack<T> oldStack = new Stack<>();
            // 保存转换后的数据
            Stack<N> newStack = new Stack<>();
            // 根节点转换
            N newRoot = (N)nodeConvert.apply(root);
            // 根节点元素入栈，触发寻找子节点这个过程
            oldStack.push(root);
            newStack.push(newRoot);

            while (!oldStack.isEmpty()) {
                // 弹出临时父节点
                T tempParent = oldStack.pop();
                // 弹出临时已转换的父节点
                N newTempParent = newStack.pop();

                // 遍历整个子列表，寻找根节点的直接直接子节点
                Iterator<T> iterator = data.iterator();
                while (iterator.hasNext()) {
                    T child = iterator.next();

                    // 已找到子节点
                    if (childFilter.test(tempParent, child)) {
                        // 子节点转换
                        N newChild = (N)nodeConvert.apply(child);
                        // 构造父子关系（最终返回的是转换后的节点，所有对转换后的节点设置）
                        addChild(newTempParent, newChild);

                        // 子节点入栈（成为新的临时父节点，依次往复）
                        oldStack.push(child);
                        newStack.push(newChild);

                        // ②优化：排除已找到挂载子节点
                        iterator.remove();
                    }
                }
            }
            // 收集根节点
            newNodes.add(newRoot);
        }
        return newNodes;
    }

    /**
     * 添加子节点
     *
     * @param parent 父节点
     * @param child 子节点
     * @param <N> 继承于Node的具体类型
     */
    private <N extends Node<N>> void addChild(N parent, N child) {
        List<N> children = parent.getChildren();
        if (children == null) {
            children = new ArrayList<>();
            children.add(child);
            parent.setChildren(children);
        } else {
            children.add(child);
        }
    }

    /**
     * 获取父节点列表
     *
     * @param nodes 节点集合
     * @param rootFilter 根节点过滤器
     * @return 根节点集合
     */
    private List<T> getRoots(List<T> nodes, Predicate<T> rootFilter) {
        return nodes.parallelStream()
                .filter(current -> current != null && rootFilter.test(current))
                .collect(toList());
    }


    /**
     * 寻找当前树里的节点
     *
     * @param nodes 待查找节点列表
     * @param predicate 断言函数
     * @param <N> 返回类型
     * @return 继承于Node的具体类型
     */
    public static <N extends Node<N>> N findNode(List<N> nodes, Predicate<N> predicate) {
        if (nodes == null || nodes.isEmpty() || predicate == null) {
            return null;
        }
        Stack<N> stack = new Stack<>();
        nodes.forEach(stack::push);
        while (!stack.isEmpty()) {
            N node = stack.pop();
            if (predicate.test(node)) {
                return node;
            }
            List<N> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                children.forEach(stack::push);
            }
        }
        return null;
    }

    /**
     * 寻找节点
     *
     * @param node 待查找节点
     * @param predicate 断言函数
     * @param <N> 返回类型
     * @return 继承于Node的具体类型
     */
    public static <N extends Node<N>> N findNode(N node, Predicate<N> predicate) {
        if (node == null || predicate == null) {
            return null;
        }
        if (predicate.test(node)) {
            return node;
        }
        return findNode(node.getChildren(), predicate);
    }

    /**
     * 设置根节点过滤器
     *
     * @param rootFilter 根节点过滤器
     * @return NodeTree
     */
    public NodeTree<T> rootFilter(Predicate<T> rootFilter) {
        this.rootFilter = rootFilter;
        return this;
    }

    /**
     * 设置孩子节点过滤器 parent, child 第一个为父节点， 第二个为待查找的子节点
     *
     * @param childFilter 孩子节点过滤器
     * @return NodeTree
     */
    public NodeTree<T> childFilter(BiPredicate<T, T> childFilter) {
        this.childFilter = childFilter;
        return this;
    }

    /**
     * 设置节点转换器
     *
     * @param nodeConvert 节点转换器
     * @return NodeTree
     */
    public NodeTree<T> map(Function<T, Node> nodeConvert) {
        this.nodeConvert = nodeConvert;
        return this;
    }

    /**
     * @author Shoven
     * @date 2019-04-08 13:47
     */
    public interface Node<T extends Node> extends Serializable {
        /**
         * 获取所有孩子节点
         *
         * @return 返回一组子节点
         */
        List<T> getChildren();


        /**
         * 设置一组孩子节点
         *
         * @param children 子节点集合
         */
        void setChildren(List<T> children);
    }

    /**
     * <p>
     * 默认节点
     * </p>
     *
     * @author Shoveni
     * @date 2019-03-16
     */
    @Data
    public static class DefaultNode implements Node<DefaultNode> {

        private static final long serialVersionUID = 1L;

        private Integer id;

        private String name;

        private List<DefaultNode> children;
    }
}

