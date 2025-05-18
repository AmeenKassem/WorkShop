package workshop.demo.UnitTests.StoreTests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Tree;

public class TreeTests {

    private Tree tree;
    private Node root;//root is the boss
    private Node owner1;
    private Node owner12;
    private Node manager1;

    @BeforeEach
    void setUp() throws UIException {
        tree = new Tree(0, false, -1);
        root = tree.getRoot();
        owner1 = new Node(1, false, 0);
        owner12 = new Node(2, false, 1);
        manager1 = new Node(10, true, 0);
        root.addChild(manager1);
        root.addChild(owner1);
        owner1.addChild(owner12);
    }

    @Test
    void testAddChild() {
        List<Node> children = root.getChildren();
        assertEquals(2, children.size());
        assertTrue(children.contains(manager1));
        assertTrue(children.contains(owner1));
    }

    @Test
    void testGetNodeById() {
        Node found = root.getNode(2);
        assertNotNull(found);
        assertEquals(2, found.getMyId());
        Node notFound = root.getNode(99);
        assertNull(notFound);
    }

    @Test
    void testDeleteLeafNode() {
        assertTrue(root.deleteNode(10));
        assertNull(root.getNode(10));
    }

    @Test
    void testDeleteSubtree() {
        assertTrue(root.deleteNode(1));
        assertNull(root.getNode(1));
        assertNull(root.getNode(2)); // child of deleted node should also be gone
        assertNotNull(root.getNode(10));
    }

    @Test
    void testIsManagerAndParentId() {
        assertTrue(manager1.getIsManager());
        assertEquals(0, manager1.getParentId());

        assertFalse(owner1.getIsManager());
        assertEquals(0, owner1.getParentId());
    }

}
