package com.cleanroommc.multiblocked.api.gui.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileNode extends TreeNode<File, File> {

    public FileNode(File dir){
        this(0, dir);
    }

    private FileNode(int dimension, File key) {
        super(dimension, key);
    }

    @Override
    public boolean isLeaf() {
        return getKey().isFile();
    }

    @Override
    public File getContent() {
        return isLeaf() ? getKey() : null;
    }

    @Override
    public List<TreeNode<File, File>> getChildren() {
        if (children == null && !isLeaf()) {
            children = new ArrayList<>();
            Arrays.stream(key.listFiles()).sorted((a, b)->{
                if (a.isFile() && b.isFile()) {
                    return a.compareTo(b);
                } else if (a.isDirectory() && b.isDirectory()) {
                    return a.compareTo(b);
                } else if(a.isDirectory()) {
                    return -1;
                }
                return 1;
            }).forEach(file -> children.add(new FileNode(dimension + 1, file).setValid(valid)));
        }
        return super.getChildren();
    }

    @Override
    public String toString() {
        return getKey().getName();
    }
}
