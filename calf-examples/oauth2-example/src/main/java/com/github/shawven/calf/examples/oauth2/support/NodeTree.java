package com.github.shawven.calf.examples.oauth2.support;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

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

        Deque<N> queue = new LinkedList<>();
        nodes.forEach(queue::push);

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
     * 追踪节点（顶级节点到目标节点的路径）
     *
     * @param nodes 待查找节点列表
     * @param predicate 断言函数
     * @param <N> 返回类型
     * @return 继承于Node的具体类型
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
            while (!queue.isEmpty()) {
                // 先添加（形成一条最终链）在判断
                N elem = queue.pop();
                link.add(elem);

                List<N> children = elem.getChildren();
                if (children != null && !children.isEmpty()) {
                    children.forEach(queue::push);
                } else {
                    link.pollLast();
                    if (predicate.test(elem)) {
                        return link;
                    }
                }
            }
        }
        return emptyList();
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
        List<N> nodes = new ArrayList<>();
        nodes.add(node);
        return flatList(nodes);
    }

    /**
     * 压扁当前节点集合及其子节点成列表
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
            if (data == null || data.isEmpty()) {
                return emptyList();
            }

            Objects.requireNonNull(rootFilter, "父节点选择器不能为空");
            Objects.requireNonNull(childFilter, "孩子节点选择器不能为空");

            // 输入和返回都实现了Node，则无需转换
            if (data.iterator().next() instanceof Node) {
                nodeConvert = n -> (R)n;
            } else {
                Objects.requireNonNull(nodeConvert, "节点转换器不能为空");
            }

            Map<Boolean, List<T>> map = data.parallelStream().collect(groupingBy(o -> rootFilter.test(o)));
            // 根节点列表
            List<T> roots = map.get(true);
            if (roots == null) {
                roots = emptyList();
            }
            // 其他节点列表
            List<T> others = map.get(false);
            if (others == null) {
                others = emptyList();
            }

            // 保存原来的数据
            Deque<T> oldStack = new LinkedList<>();
            // 保存转换后的数据
            Deque<R> newStack = new LinkedList<>();
            List<R> newNodes = new ArrayList<>();

            // 遍历根节点
            for (T root : roots) {
                // 根节点转换
                R newRoot = nodeConvert.apply(root);
                // 根节点元素入栈，触发寻找子节点这个过程
                oldStack.push(root);
                newStack.push(newRoot);

                // 构建
                doBuild(others, oldStack, newStack);

                // 收集根节点
                newNodes.add(newRoot);
            }
            return newNodes;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void doBuild(List<T> others, Deque<T> oldStack, Deque<R> newStack) {
            while (!oldStack.isEmpty()) {
                // 弹出临时父节点
                T tempParent = oldStack.pop();
                // 弹出临时已转换的父节点
                R newTempParent = newStack.pop();

                // 遍历整个子列表，寻找根节点的直接直接子节点
                Iterator<T> iterator = others.iterator();
                while (iterator.hasNext()) {
                    T child = iterator.next();

                    // 已找到子节点
                    if (childFilter.test(tempParent, child)) {
                        // 子节点转换
                        R newChild = nodeConvert.apply(child);

                        if (newChild instanceof DeNode) {
                            ((DeNode) newChild).setParent((DeNode)newTempParent);
                        }

                        // 构造父子关系（最终返回的是转换后的节点，所有对转换后的节点设置）
                        addChild(newTempParent, newChild);

                        // 子节点入栈（成为新的临时父节点，依次往复）
                        oldStack.push(child);
                        newStack.push(newChild);

                        // 排除已找到挂载子节点
                        iterator.remove();
                    }
                }
            }
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
