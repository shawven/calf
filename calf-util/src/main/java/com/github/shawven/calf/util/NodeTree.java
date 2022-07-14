package com.github.shawven.calf.util;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

/**
 * @author Shoven
 * @date 2019-11-20
 */
public class NodeTree {

    /**
     * 返回节点树构造器
     *
     * @param data 数据集合
     * @param <T> 输入类型
     * @param <R> 返回类型
     * @return TreeBuilder
     */
    public static <T, R extends Node<R>> TreeBuilder<T, R> from(List<T> data) {
        return new TreeBuilder<>(data);
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
        if (node == null) {
            return null;
        }
        return predicate.test(node) ? node : findNode(node.getChildren(), predicate);
    }

    /**
     * 寻找节点
     *
     * @param nodes 待查找节点列表
     * @param predicate 断言函数
     * @param <N> 返回类型
     * @return 继承于Node的具体类型
     */
    public static <N extends Node<N>> N findNode(List<N> nodes, Predicate<N> predicate) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        Deque<N> queue = new LinkedList<>(nodes);

        while (!queue.isEmpty()) {
            N node = queue.pop();
            if (predicate.test(node)) {
                return node;
            }
            List<N> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                children.forEach(queue::push);
            }
        }
        return null;
    }

    /**
     * 压扁节点树成列表（平铺当前节点及其子节点）
     *
     * @param node 待处理节点
     * @param <N> 节点类型
     * @return 扁平节点集合
     */
    public static <N extends Node<N>> List<N> flatList(N node) {
        if (node == null) {
            return emptyList();
        }
        return flatList(singletonList(node));
    }

    /**
     * 压扁多个节点树成列表（平铺当前节点及其子节点）
     *
     * @param nodes 待处理节点集合
     * @param <N> 节点类型
     * @return 扁平节点集合
     */
    public static <N extends Node<N>> List<N> flatList(List<N> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return emptyList();
        }
        LinkedList<N> linkedList = new LinkedList<>(nodes);
        List<N> list = new ArrayList<>();

        while (!linkedList.isEmpty()) {
            N node = linkedList.pop();
            List<N> children = node.getChildren();
            list.add(node);
            if (children != null && !children.isEmpty()) {
                // 添加到队列头
                linkedList.addAll(0, children);
            }
        }
        return list;
    }

    public static class TreeBuilder<T, R extends Node<R>> {

        /**
         * 源数据
         */
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
        private Function<T, R> nodeConvert;

        public TreeBuilder(List<T> data) {
            this.data = data;
        }

        /**
         * 设置根节点过滤器
         *
         * @param rootFilter 根节点过滤器
         * @return NodeTree2
         */
        public TreeBuilder<T, R> rootFilter(Predicate<T> rootFilter) {
            this.rootFilter = rootFilter;
            return this;
        }

        /**
         * 设置孩子节点过滤器 parent, child 第一个为父节点， 第二个为待查找的子节点
         *
         * @param childFilter 孩子节点过滤器
         * @return NodeTree2
         */
        public TreeBuilder<T, R> childFilter(BiPredicate<T, T> childFilter) {
            this.childFilter = childFilter;
            return this;
        }

        /**
         * 设置节点转换器
         *
         * @param nodeConvert 节点转换器
         * @return NodeTree2
         */
        public TreeBuilder<T, R> map(Function<T, R> nodeConvert) {
            this.nodeConvert = nodeConvert;
            return this;
        }

        public List<R> build() {
            Objects.requireNonNull(rootFilter, "父节点选择器不能为空");
            Objects.requireNonNull(childFilter, "孩子节点选择器不能为空");

            if (data == null || data.isEmpty()) {
                return emptyList();
            }
            // 输入和返回都实现了Node，则无需转换
            if (data.iterator().next() instanceof Node) {
                nodeConvert = n -> (R)n;
            } else {
                Objects.requireNonNull(nodeConvert, "节点转换器不能为空");
            }

            Map<Boolean, List<T>> map = data.stream()
                    .filter(Objects::nonNull)
                    .collect(groupingBy(o -> rootFilter.test(o)));

            // 子节点列表
            List<T> children = map.getOrDefault(false, emptyList());
            // 根节点
            LinkedList<T> roots = new LinkedList<>(map.getOrDefault(true, emptyList()));

            // 新根节点
            LinkedList<R> newRoots = roots.stream().map(nodeConvert).collect(toCollection(LinkedList::new));
            // 返回结果
            List<R> res = new ArrayList<>(newRoots);

            while (!roots.isEmpty()) {
                // 弹出临时父节点
                T oldParent = roots.pop();
                // 弹出临时已转换的父节点
                R newParent = newRoots.pop();

                // 遍历整个子列表，寻找根节点的直接直接子节点
                Iterator<T> iterator = children.iterator();
                while (iterator.hasNext()) {
                    T child = iterator.next();

                    // 已找到子节点
                    if (childFilter.test(oldParent, child)) {
                        // 子节点转换
                        R newChild = nodeConvert.apply(child);

                        if (newChild instanceof DeNode) {
                            ((DeNode) newChild).setParent((DeNode)newParent);
                        }

                        // 构造父子关系.最终返回的是转换后的节
                        addChild(newParent, newChild);

                        // 子节点入栈
                        roots.push(child);
                        newRoots.push(newChild);

                        // 排除已找到挂载子节点
                        iterator.remove();
                    }
                }
            }

            return res;
        }

        /**
         * 添加子节点，如果是第一次添加返回true
         *
         * @param parent 父节点
         * @param child 子节点
         * @param <N> 继承于Node的具体类型
         */
        private <N extends Node<N>> void addChild(N parent, N child) {
            List<N> children = parent.getChildren();
            // 防止默认值为emptyList时不能添加数据
            if (children == null || children.equals(emptyList())) {
                children = new ArrayList<>();
                children.add(child);
                parent.setChildren(children);
            } else {
                children.add(child);
            }
        }
    }

    /**
     * 节点
     *
     * @param <T>
     */
    public interface Node<T extends Node<T>> extends Serializable {
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

        /**
         * 寻找子节点
         *
         * @param predicate 断言函数
         * @return 子节点集合
         */
        default T findChild(Predicate<T> predicate) {
            return findNode(getChildren(), predicate);
        }

        /**
         * 扁平子节点集合
         *
         * @return 子节点集合
         */
        default List<T> flatChildren() {
            return flatList(getChildren());
        }
    }

    /**
     * 双向节点
     *
     * @param <T>
     */
    public interface DeNode<T extends DeNode<T>> extends Node<T> {

        /**
         * 获取父节点
         *
         * @return 父节点
         */
        T getParent();

        /**
         * 设置父节点
         *
         * @param parent 父节点
         */
        void setParent(T parent);

        /**
         * 追踪祖先节点
         *
         * @return 祖先节点集合
         */
        default List<T> traceAncestors() {
            LinkedList<T> ancestors = new LinkedList<>();
            T parent  = getParent();
            while (parent != null) {
                ancestors.addFirst(parent);
                parent = parent.getParent();
            }
            return new ArrayList<>(ancestors);
        }
    }
}
