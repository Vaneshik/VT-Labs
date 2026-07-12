package lab1.task2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static lab1.task2.BPlusTree.*;
import static org.junit.jupiter.api.Assertions.*;

class BPlusTreeTest {

    private BPlusTree tree;

    @BeforeEach
    void setUp() {
        tree = new BPlusTree();
    }

    @Test
    void insertIntoEmptyTree_checkpoints() {
        List<String> reference = List.of(A, B);
        tree.insert(42);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void insertWithoutSplit_checkpoints() {
        List<String> reference = List.of(A, C, D);
        tree.insert(10);
        tree.insert(5);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void insertFillingLeafToCapacity_checkpoints() {
        List<String> reference = List.of(A, C, D);
        for (int i = 1; i <= 6; i++) tree.insert(i);
        tree.insert(7);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void insertCausingLeafRootSplit_checkpoints() {
        List<String> reference = List.of(A, C, E, F);
        for (int i = 1; i <= 7; i++) tree.insert(i);
        tree.insert(8);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void insertCausingLeafSplitWithSpaceInParent_checkpoints() {
        List<String> reference = List.of(A, C, E, G);
        for (int i = 1; i <= 11; i++) tree.insert(i);
        tree.insert(12);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void insertCausingRootInternalSplit_checkpoints() {
        List<String> reference = List.of(A, C, E, H, I);
        for (int i = 1; i <= 35; i++) tree.insert(i);
        tree.insert(36);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void singleInsert_treeHasOneKey() {
        tree.insert(15);
        assertEquals(List.of(15), tree.getAllKeys());
    }

    @Test
    void sevenInserts_allStoredInLeafRoot() {
        for (int i = 7; i >= 1; i--) tree.insert(i);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), tree.getAllKeys());
        assertEquals(1, tree.getHeight());
    }

    @Test
    void eightInserts_treeHasTwoLevels() {
        for (int i = 1; i <= 8; i++) tree.insert(i);
        assertEquals(2, tree.getHeight());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), tree.getAllKeys());
    }

    @Test
    void insertionsKeepKeysSorted() {
        int[] data = {50, 10, 30, 20, 40, 60, 15, 25, 35, 45, 55, 65};
        for (int k : data) tree.insert(k);
        List<Integer> keys = tree.getAllKeys();
        for (int i = 0; i < keys.size() - 1; i++) {
            assertTrue(keys.get(i) < keys.get(i + 1));
        }
    }

    @Test
    void manyInserts_heightGrowsCorrectly() {
        for (int i = 1; i <= 36; i++) tree.insert(i);
        assertEquals(3, tree.getHeight());
    }

    @Test
    void insertOrderDoesNotAffectContent() {
        BPlusTree t1 = new BPlusTree();
        BPlusTree t2 = new BPlusTree();
        for (int i = 1; i <= 20; i++) t1.insert(i);
        for (int i = 20; i >= 1; i--) t2.insert(i);
        assertEquals(t1.getAllKeys(), t2.getAllKeys());
    }

    @Test
    void searchInEmptyTree_checkpoints() {
        List<String> reference = List.of(J, L);
        tree.search(1);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void searchExistingKey_checkpoints() {
        List<String> reference = List.of(J, K);
        tree.insert(5);
        tree.search(5);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void searchMissingKey_checkpoints() {
        List<String> reference = List.of(J, L);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);
        tree.search(99);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void searchInMultiLevelTree_checkpoints() {
        List<String> reference = List.of(J, K);
        for (int i = 1; i <= 16; i++) tree.insert(i);
        tree.search(13);
        assertEquals(reference, tree.getTrace());
    }

    @Test
    void searchReturnsFalseForEmptyTree() {
        assertFalse(tree.search(1));
    }

    @Test
    void searchReturnsTrueForInsertedKey() {
        tree.insert(7);
        assertTrue(tree.search(7));
    }

    @Test
    void searchReturnsFalseForAbsentKey() {
        tree.insert(7);
        assertFalse(tree.search(8));
    }

    @Test
    void searchAfterManyInserts() {
        for (int i = 1; i <= 50; i++) tree.insert(i);
        for (int i = 1; i <= 50; i++) assertTrue(tree.search(i));
        assertFalse(tree.search(0));
        assertFalse(tree.search(51));
    }

    @Test
    void searchForMinAndMaxKey() {
        for (int i = 10; i >= 1; i--) tree.insert(i);
        assertTrue(tree.search(1));
        assertTrue(tree.search(10));
    }

    @Test
    void deleteFromEmptyTree_returnsFalse() {
        assertFalse(tree.delete(1));
    }

    @Test
    void deleteExistingKey() {
        tree.insert(5);
        assertTrue(tree.delete(5));
        assertFalse(tree.search(5));
    }

    @Test
    void deleteAbsentKey_returnsFalse() {
        tree.insert(1);
        tree.insert(2);
        assertFalse(tree.delete(99));
    }

    @Test
    void deleteDoesNotAffectOtherKeys() {
        for (int i = 1; i <= 10; i++) tree.insert(i);
        tree.delete(5);
        for (int i = 1; i <= 10; i++) {
            if (i == 5) assertFalse(tree.search(i));
            else assertTrue(tree.search(i));
        }
    }

    @Test
    void deleteLastKeyEmptiesTree() {
        tree.insert(42);
        assertTrue(tree.delete(42));
        assertFalse(tree.search(42));
    }

    @Test
    void rangeSearchInEmptyTree() {
        assertEquals(List.of(), tree.rangeSearch(1, 10));
    }

    @Test
    void rangeSearchAllKeys() {
        for (int i = 1; i <= 5; i++) tree.insert(i);
        assertEquals(List.of(1, 2, 3, 4, 5), tree.rangeSearch(1, 5));
    }

    @Test
    void rangeSearchSubset() {
        for (int i = 1; i <= 20; i++) tree.insert(i);
        assertEquals(List.of(5, 6, 7, 8, 9, 10), tree.rangeSearch(5, 10));
    }

    @Test
    void rangeSearchNoMatch() {
        for (int i = 1; i <= 10; i++) tree.insert(i);
        assertEquals(List.of(), tree.rangeSearch(11, 20));
    }

    @Test
    void rangeSearchSingleElement() {
        for (int i = 1; i <= 10; i++) tree.insert(i);
        assertEquals(List.of(7), tree.rangeSearch(7, 7));
    }
}
