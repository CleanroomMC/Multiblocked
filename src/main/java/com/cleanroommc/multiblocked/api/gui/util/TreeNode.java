package com.cleanroommc.multiblocked.api.gui.util;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/***
 * Tree
 * @param <T> key
 * @param <K> leaf
 */
public class TreeNode<T, K> {
    public final int dimension;
    protected final T key;
    protected K content;
    protected List<TreeNode<T, K>> children;
    protected Predicate<TreeNode<T, K>> valid;

    public TreeNode(int dimension, T key) {
        this.dimension = dimension;
        this.key = key;
    }

    public TreeNode<T, K> setValid(Predicate<TreeNode<T, K>> valid) {
        this.valid = valid;
        return this;
    }

    public boolean isLeaf(){
        return getChildren() == null || getChildren().isEmpty();
    }

    public TreeNode<T, K> getOrCreateChild (T childKey) {
        TreeNode<T, K> result;
        if (getChildren() != null) {
            result = getChildren().stream().filter(child->child.key.equals(childKey)).findFirst().orElseGet(()->{
                TreeNode<T, K> newNode = new TreeNode<>(dimension + 1, childKey);
                getChildren().add(newNode);
                return newNode;
            });
        } else {
            children = new ArrayList<>();
            result = new TreeNode<>(dimension + 1, childKey);
            getChildren().add(result);
        }
        return result;
    }

    public TreeNode<T, K> getChild(T key) {
        if (getChildren() != null) {
            for (TreeNode<T, K> child : getChildren()) {
                if (child.key.equals(key)) {
                    return child;
                }
            }
        }
        return null;
    }

    public void addContent (T key, K content) {
        getOrCreateChild(key).content = content;
    }

    public T getKey() {
        return key;
    }

    public K getContent() {
        return content;
    }

    public List<TreeNode<T, K>> getChildren() {
        if (valid == null) return children;
        return children.stream().filter(valid).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
