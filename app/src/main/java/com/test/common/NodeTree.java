package com.test.common;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * @author Shoven
 * @date 2019-03-18 16:43
 */
public class NodeTree<T, R extends NodeTree.Node> implements Serializable {

    private List<T> data = new ArrayList<>();

    /**
     * 父节点选择器
     */
    private Predicate<T> topFilter;

    /**
     * 子节点选择器
     */
    private BiPredicate<T, T> childFilter;

    /**
     * 节点转换器
     */
    private Function<T, R> nodeConvert;

    public NodeTree() {
    }

    public NodeTree(List<T> data) {
        this.data = data;
    }

    /**
     * 生成树结构列表
     *
     * @return
     */
    public List<R> generate() {
        if (data == null || data.isEmpty()) {
            return null;
        }

        requireNonNull(topFilter, "父节点选择器不能为空");
        requireNonNull(childFilter, "孩子节点选择器不能为空");
        requireNonNull(nodeConvert, "节点转换器不能为空");

        List<T> nodes = getParents(data, topFilter);
        List<R> newNodes = new ArrayList<>();

        for (T node : nodes) {
            // 先把顶级节点转换成节点
            R newNode = nodeConvert.apply(node);
            // 寻找子节点
            newNode.setChildren((List<Node>)findChildren(node));
            newNodes.add(newNode);
        }

        return newNodes;
    }

    /**
     * 获取父节点列表
     *
     * @param nodes
     * @param parentSelector
     * @return
     */
    private List<T> getParents(List<T> nodes, Predicate<T> parentSelector) {
        return nodes.stream()
                .filter(current -> current != null && parentSelector.test(current))
                .collect(toList());
    }


    /**
     * 寻找孩子节点列表
     *
     * @param oldParent
     * @return
     */
    private List<R> findChildren(T oldParent) {
        return data.stream()
                // 继续寻找当前节点的子节点
                .filter(node -> childFilter.test(oldParent, node))
                .map(oldNode -> {
                    // 把旧节点转换成新节点
                    R newNode = nodeConvert.apply(oldNode);
                    // 递归下去
                    newNode.setChildren((List<Node>)findChildren(oldNode));
                    return newNode;
                })
                .collect(toList());
    }

    /**
     * 寻找当前树里的节点
     *
     * @param nodes
     * @param predicate
     * @param <N>
     * @return
     */
    public static <N extends Node> N findTreeNode(List<Node> nodes, Predicate<Node> predicate) {
        Node foundNode = null;
        for (Node node : nodes) {
            if ((foundNode = findNode(node, predicate)) != null) {
                break;
            }
        }
        return (N)foundNode;
    }

    /**
     * 寻找节点
     *
     * @param node
     * @param predicate
     * @return
     */
    private static <N extends Node> N findNode(Node node, Predicate<Node> predicate) {
        if (predicate.test(node)) {
            return (N)node;
        }
        Node foundNode = null;
        List<Node> children = node.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            for (Node child : children) {
                if ((foundNode = findNode(child, predicate)) != null) {
                    break;
                }
            }
        }
        return (N)foundNode;
    }

    public List<T> getData() {
        return data;
    }

    public NodeTree<T, R> setData(List<T> data) {
        this.data = data;
        return this;
    }

    /**
     * 顶级父节点元素过滤器
     *
     * @param topFilter
     * @return
     */
    public NodeTree<T, R> setTopFilter(Predicate<T> topFilter) {
        this.topFilter = topFilter;
        return this;
    }

    /**
     * 孩子节点元素选择器 childFilter => parent, child
     * 第一个为父元素， 第二个为待寻找的子元素
     *
     * @param childFilter
     * @return
     */
    public NodeTree<T, R> setChildFilter(BiPredicate<T, T> childFilter) {
        this.childFilter = childFilter;
        return this;
    }

    /**
     * 节点转换器
     *
     * @param nodeConvert
     * @return
     */
    public NodeTree<T, R> setNodeConvert(Function<T, R> nodeConvert) {
        this.nodeConvert = nodeConvert;
        return this;
    }

    /**
     * @author Shoven
     * @date 2019-04-08 13:47
     */
    public interface Node extends Serializable {
        /**
         * 获取所有孩子节点
         *
         * @return
         */
        List<Node> getChildren();


        /**
         * 设置一组孩子节点
         *
         * @param children
         */
        void setChildren(List<Node> children);
    }

    /**
     * <p>
     * 默认节点
     * </p>
     *
     * @author Shoveni
     * @date 2019-03-16
     */
    public static class DefaultNode implements NodeTree.Node {

        private static final long serialVersionUID = 1L;

        private Integer id;

        private String name;

        private List<Node> children;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public List<Node> getChildren() {
            return children;
        }

        @Override
        public void setChildren(List<Node> children) {
            this.children = children;
        }
    }
}

