package com.github.shawven.calf.util;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

/**
 * 节点树
 *
 * @author xw
 * @date 2023-08-07
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
     * 深度优先搜索
     *
     * @param node 待搜索节点
     * @param predicate 断言函数
     * @param <N> 泛型类型，继承于Node的具体类型
     * @return 目标节点
     */
    public static <N extends Node<N>> N dfs(N node, Predicate<N> predicate) {
        if (node == null) {
            return null;
        }
        if (predicate.test(node)) {
            return node;
        }
        return dfs(node.getChildren(), predicate);
    }

    /**
     * 深度优先搜索
     *
     * @param nodes 待搜索节点
     * @param predicate 断言函数
     * @param <N> 泛型类型，继承于Node的具体类型
     * @return 目标节点
     */
    public static <N extends Node<N>> N dfs(List<N> nodes, Predicate<N> predicate) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        LinkedList<N> linkedList = new LinkedList<>(nodes);

        while (!linkedList.isEmpty()) {
            N node = linkedList.pop();
            if (predicate.test(node)) {
                return node;
            }
            List<N> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                // 添加到队列尾
                linkedList.addAll(children);
            }
        }
        return null;
    }

    /**
     * 广度优先遍历
     *
     * @param nodes 待搜索节点
     * @param <N> 泛型类型
     */
    public static <N extends Node<N>> void bfs(List<N> nodes, Consumer<N> consumer) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        LinkedList<N> linkedList = new LinkedList<>(nodes);
        while (!linkedList.isEmpty()) {
            N node = linkedList.pop();
            consumer.accept(node);

            List<N> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                // 添加到队列尾
                linkedList.addAll(children);
            }
        }
    }

    /**
     * 深度优先遍历
     *
     * @param nodes 待搜索节点
     * @param <N> 泛型类型
     */
    public static <N extends Node<N>> void dfs(List<N> nodes, Consumer<N> consumer) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        LinkedList<N> linkedList = new LinkedList<>(nodes);
        while (!linkedList.isEmpty()) {
            N node = linkedList.pop();
            consumer.accept(node);

            List<N> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                // 添加到队列头
                linkedList.addAll(0, children);
            }
        }
    }

    /**
     * 追踪节点（顶级节点到目标节点的路径）
     *
     * @param nodes 待搜索节点
     * @param predicate 断言函数
     * @param <N> 返回类型，继承于Node的具体类型
     * @return 追踪链路
     */
    public static <N extends Node<N>> List<N> traceNode(List<N> nodes, Predicate<N> predicate) {
        if (nodes == null || nodes.isEmpty()) {
            return emptyList();
        }
        LinkedList<N> link = new LinkedList<>();
        Deque<N> queue = new LinkedList<>();

        for (N node : nodes) {
            queue.push(node);
            link.clear();

            boolean preNodeIsLeaf = false;
            while (!queue.isEmpty()) {
                N elem = queue.pop();

                List<N> children = elem.getChildren();
                // 当前节点是叶子节点
                boolean curNodeIsLeaf = children == null || children.isEmpty();

                // 当前节点非叶子节点且前一个节点是叶子节点，即遍历了所有的叶子节点回退到父节点
                if (preNodeIsLeaf && !curNodeIsLeaf) {
                    // 父节点已经判断过直接删除
                    link.removeLast();
                }

                // 找到退出
                if (predicate.test(elem)) {
                    link.addLast(elem);
                    return link;
                }

                if (curNodeIsLeaf) {
                    preNodeIsLeaf = true;
                } else {
                    // 前序遍历，先添加父节点
                    link.addLast(elem);
                    // 子节点入栈
                    children.forEach(queue::push);
                }
            }
        }
        return emptyList();
    }

    /**
     * 压扁节点树成列表（平铺当前节点及其子节点）
     *
     * @param node 待搜索节点
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
     * @param nodes 待搜索节点
     * @param <N> 节点类型
     * @return 扁平节点集合
     */
    public static <N extends Node<N>> List<N> flatList(List<N> nodes) {
        List<N> list = new ArrayList<>();
        dfs(nodes, n -> {
            list.add(n);
        });
        return list;
    }

    public static class TreeBuilder<T, R extends Node<R>> {

        /**
         * 源数据
         */
        private final List<T> data;

        /**
         * 根节点选择桥
         */
        private Predicate<T> selector;

        /**
         * 父子节点连接器
         */
        private BiPredicate<T, T> connector;

        /**
         * 节点转换器
         */
        private Function<T, R> nodeConvert;

        public TreeBuilder(List<T> data) {
            this.data = data;
        }

        /**
         * 设置根节点选择器
         *
         * @param selector 根节点选择器
         * @return NodeTree
         */
        public TreeBuilder<T, R> select(Predicate<T> selector) {
            this.selector = selector;
            return this;
        }

        /**
         * 设置父子节点连接器 parent, child 第一个为父节点， 第二个为待查找的子节点
         *
         * @param connector 子节点连接器
         * @return NodeTree2
         */
        public TreeBuilder<T, R> connect(BiPredicate<T, T> connector) {
            this.connector = connector;
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
            Objects.requireNonNull(selector, "跟节点选择器不能为空");
            Objects.requireNonNull(connector, "父子节点连接器不能为空");

            if (data == null || data.isEmpty()) {
                return emptyList();
            }
            // 输入和输出类型都实现了Node，则无需转换
            if (data.iterator().next() instanceof Node) {
                nodeConvert = n -> (R)n;
            } else {
                Objects.requireNonNull(nodeConvert, "输入节点转换器不能为空");
            }

            Map<Boolean, List<T>> map = data.stream()
                    .filter(Objects::nonNull)
                    .collect(groupingBy(o -> selector.test(o)));

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
                    if (connector.test(oldParent, child)) {
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
            return dfs(getChildren(), predicate);
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
