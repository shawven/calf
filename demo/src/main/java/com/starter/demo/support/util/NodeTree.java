package com.starter.demo.support.util;

import org.apache.commons.lang3.SerializationException;

import java.io.*;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

/**
 * @author Shoven
 * @date 2019-11-20
 */
public class NodeTree {

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
     * 寻找当前树里的节点
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
     * 压扁当前节点及其子节点成列表
     *
     * @param node
     * @param <N>
     * @return
     */
    public static <N extends Node<N>> List<N> flatList(N node) {
        if (node == null) {
            return null;
        }
        List<N> nodes = new ArrayList<>();
        nodes.add(node);
        return flatList(nodes);
    }

    /**
     * 压扁当前节点集合及其子节点成列表
     *
     * @param nodes
     * @param <N>
     * @return
     */
    public static <N extends Node<N>> List<N> flatList(List<N> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return emptyList();
        }
        LinkedList<N> queue = nodes.parallelStream().map(NodeTree::clone).collect(toCollection(LinkedList::new));
        List<N> list = new ArrayList<>();

        while (!queue.isEmpty()) {
            N node = queue.pop();
            List<N> children = node.getChildren();
            // 清除子节点指针
            node.setChildren(null);
            list.add(node);
            if (children != null && !children.isEmpty()) {
                // 添加到队列头
                queue.addAll(0, children);
            }
        }
        return list;
    }

    private static <T extends Serializable> T clone(T object) {
        if (object == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        try (ObjectOutputStream out = new ObjectOutputStream(baos)){
            out.writeObject(object);
        } catch (IOException ex) {
            throw new SerializationException(ex);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream in = new ObjectInputStream(bais)) {
            //noinspection unchecked
            return (T) in.readObject();
        } catch (ClassNotFoundException ex) {
            throw new SerializationException("ClassNotFoundException while reading cloned object data", ex);
        } catch (IOException ex) {
            throw new SerializationException("IOException while reading or closing cloned object data", ex);
        }
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
            Objects.requireNonNull(nodeConvert, "节点转换器不能为空");

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

            List<R> newNodes = new ArrayList<>();
            // 遍历根节点
            for (T root : roots) {
                // 保存原来的数据
                Stack<T> oldStack = new Stack<>();
                // 保存转换后的数据
                Stack<R> newStack = new Stack<>();
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

        private void doBuild(List<T> others, Stack<T> oldStack, Stack<R> newStack) {
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

        default T findChild(Predicate<T> predicate) {
            return (T) findNode(getChildren(), predicate);
        }

        default List<T> flatChildren(Predicate<T> predicate) {
            return (List<T>) findNode(getChildren(), predicate);
        }
    }
}
