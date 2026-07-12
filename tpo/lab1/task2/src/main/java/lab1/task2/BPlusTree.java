package lab1.task2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BPlusTree {

    public static final int MAX_KEYS = 7;

    public static final String A = "A";
    public static final String B = "B";
    public static final String C = "C";
    public static final String D = "D";
    public static final String E = "E";
    public static final String F = "F";
    public static final String G = "G";
    public static final String H = "H";
    public static final String I = "I";

    public static final String J = "J";
    public static final String K = "K";
    public static final String L = "L";

    private Node root;
    private final List<String> trace = new ArrayList<>();

    abstract static class Node {
        int[] keys;
        int keyCount;
        InternalNode parent;

        Node(int capacity) {
            keys = new int[capacity + 1];
            keyCount = 0;
        }
    }

    static class LeafNode extends Node {
        LeafNode next;

        LeafNode() {
            super(MAX_KEYS);
        }

        void insertKey(int key) {
            int pos = keyCount - 1;
            while (pos >= 0 && keys[pos] > key) {
                keys[pos + 1] = keys[pos];
                pos--;
            }
            keys[pos + 1] = key;
            keyCount++;
        }

        boolean contains(int key) {
            for (int i = 0; i < keyCount; i++) {
                if (keys[i] == key) return true;
            }
            return false;
        }
    }

    static class InternalNode extends Node {
        Node[] children;

        InternalNode() {
            super(MAX_KEYS);
            children = new Node[MAX_KEYS + 2];
        }

        void insertSeparator(int key, Node rightChild) {
            int pos = keyCount - 1;
            while (pos >= 0 && keys[pos] > key) {
                keys[pos + 1] = keys[pos];
                children[pos + 2] = children[pos + 1];
                pos--;
            }
            keys[pos + 1] = key;
            children[pos + 2] = rightChild;
            rightChild.parent = this;
            keyCount++;
        }
    }

    public List<String> getTrace() {
        return Collections.unmodifiableList(trace);
    }

    public boolean search(int key) {
        trace.clear();
        trace.add(J);

        if (root == null) {
            trace.add(L);
            return false;
        }

        LeafNode leaf = findLeaf(key);
        if (leaf.contains(key)) {
            trace.add(K);
            return true;
        }

        trace.add(L);
        return false;
    }

    public void insert(int key) {
        trace.clear();
        trace.add(A);

        if (root == null) {
            trace.add(B);
            LeafNode leaf = new LeafNode();
            leaf.insertKey(key);
            root = leaf;
            return;
        }

        LeafNode leaf = findLeaf(key);
        trace.add(C);

        leaf.insertKey(key);

        if (leaf.keyCount <= MAX_KEYS) {
            trace.add(D);
            return;
        }

        trace.add(E);
        splitLeaf(leaf);
    }

    public boolean delete(int key) {
        if (root == null) return false;

        LeafNode leaf = findLeaf(key);
        int pos = -1;
        for (int i = 0; i < leaf.keyCount; i++) {
            if (leaf.keys[i] == key) {
                pos = i;
                break;
            }
        }
        if (pos == -1) return false;

        System.arraycopy(leaf.keys, pos + 1, leaf.keys, pos, leaf.keyCount - pos - 1);
        leaf.keyCount--;

        if (leaf.keyCount == 0 && leaf == root) {
            root = null;
        }
        return true;
    }

    public List<Integer> rangeSearch(int from, int to) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        LeafNode leaf = findLeaf(from);
        while (leaf != null) {
            for (int i = 0; i < leaf.keyCount; i++) {
                if (leaf.keys[i] > to) return result;
                if (leaf.keys[i] >= from) result.add(leaf.keys[i]);
            }
            leaf = leaf.next;
        }
        return result;
    }

    private LeafNode findLeaf(int key) {
        Node node = root;
        while (node instanceof InternalNode) {
            InternalNode internal = (InternalNode) node;
            int idx = 0;
            while (idx < internal.keyCount && key >= internal.keys[idx]) {
                idx++;
            }
            node = internal.children[idx];
        }
        return (LeafNode) node;
    }

    private void splitLeaf(LeafNode leaf) {
        int mid = (MAX_KEYS + 1) / 2; // 4

        LeafNode right = new LeafNode();
        right.keyCount = leaf.keyCount - mid;
        System.arraycopy(leaf.keys, mid, right.keys, 0, right.keyCount);

        leaf.keyCount = mid;

        right.next = leaf.next;
        leaf.next = right;

        int separator = right.keys[0];

        if (leaf.parent == null) {
            trace.add(F);
            InternalNode newRoot = new InternalNode();
            newRoot.keys[0] = separator;
            newRoot.keyCount = 1;
            newRoot.children[0] = leaf;
            newRoot.children[1] = right;
            leaf.parent = newRoot;
            right.parent = newRoot;
            root = newRoot;
        } else {
            right.parent = leaf.parent;
            pushSeparatorUp(leaf.parent, separator, right);
        }
    }

    private void pushSeparatorUp(InternalNode parent, int separator, Node rightChild) {
        parent.insertSeparator(separator, rightChild);

        if (parent.keyCount <= MAX_KEYS) {
            trace.add(G);
        } else {
            trace.add(H);
            splitInternal(parent);
        }
    }

    private void splitInternal(InternalNode node) {
        int mid = MAX_KEYS / 2;
        int pushUp = node.keys[mid];

        InternalNode right = new InternalNode();
        right.keyCount = node.keyCount - mid - 1;
        System.arraycopy(node.keys, mid + 1, right.keys, 0, right.keyCount);
        System.arraycopy(node.children, mid + 1, right.children, 0, right.keyCount + 1);

        for (int i = 0; i <= right.keyCount; i++) {
            if (right.children[i] != null) {
                right.children[i].parent = right;
            }
        }

        node.keyCount = mid;

        if (node.parent == null) {
            trace.add(I);
            InternalNode newRoot = new InternalNode();
            newRoot.keys[0] = pushUp;
            newRoot.keyCount = 1;
            newRoot.children[0] = node;
            newRoot.children[1] = right;
            node.parent = newRoot;
            right.parent = newRoot;
            root = newRoot;
        } else {
            right.parent = node.parent;
            pushSeparatorUp(node.parent, pushUp, right);
        }
    }


    public int getHeight() {
        if (root == null) return 0;
        int h = 1;
        Node node = root;
        while (node instanceof InternalNode) {
            node = ((InternalNode) node).children[0];
            h++;
        }
        return h;
    }

    public List<Integer> getAllKeys() {
        List<Integer> keys = new ArrayList<>();
        if (root == null) return keys;

        Node node = root;
        while (node instanceof InternalNode) {
            node = ((InternalNode) node).children[0];
        }
        LeafNode leaf = (LeafNode) node;
        while (leaf != null) {
            for (int i = 0; i < leaf.keyCount; i++) {
                keys.add(leaf.keys[i]);
            }
            leaf = leaf.next;
        }
        return keys;
    }
}
