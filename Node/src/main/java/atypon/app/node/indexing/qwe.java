package atypon.app.node.indexing;
import atypon.app.node.indexing.bplustree.BPlusTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class qwe<K extends Comparable<K>> {

    public static void main(String[] args) {
        BPlusTree<String, List<String>> bPlusTree = new BPlusTree<>();
        List<String> list1 = new ArrayList<>();
        list1.add("A1");
        list1.add("B1");
        bPlusTree.insert("t1", list1);
        System.out.println(bPlusTree.search("t1"));
    }
}