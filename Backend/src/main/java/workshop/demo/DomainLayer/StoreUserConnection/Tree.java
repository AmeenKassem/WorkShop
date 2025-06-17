package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DomainLayer.Exceptions.DevException;

public class Tree implements Iterable<Node> {

    private static final Logger logger = LoggerFactory.getLogger(Tree.class);

    private Node root;// root is the boss

    private final ReentrantLock lock = new ReentrantLock();

    // FOR BOSS: isManager->false + parenetId-> -1
    public Tree(int storeId, int myId, boolean isManager) {
        this.root = new Node(storeId, myId, isManager, null);
        logger.debug("Tree initialized with root ID={}, isManager={}", myId, isManager);

    }

    // Constructor to build the tree from the StoreTreeEntity (used when loading from DB)
    public Tree(StoreTreeEntity entity) throws DevException {
        lock.lock();
        try {
            // Step 1: Build a map from NodeKey → Node
            Map<NodeKey, Node> keyToNode = new HashMap<>();

            List<Node> nodes = new ArrayList<>(entity.getAllNodes());
            //parent nodes are processed before children
            nodes.sort(Comparator.comparingInt(Node::getParentId));
            for (Node node : nodes) {
                node.getChildren().clear(); // reset children list
                keyToNode.put(node.getKey(), node); // use full composite key
            }

            // Step 2: Re-link children to parents
            for (Node node : nodes) {
                int parentId = node.getParentId();
                if (parentId == -1) {
                    this.root = node;
                } else {
                    NodeKey parentKey = new NodeKey(entity.getStoreId(), parentId);
                    Node parent = keyToNode.get(parentKey);
                    if (parent != null) {
                        parent.addChild(node);
                    } else {
                        throw new DevException("Missing parent with key: " + parentKey + " for node " + node.getKey());
                    }
                }
            }

            if (this.root == null) {
                throw new DevException("No root node found (parentId == -1)");
            }

            logger.debug("Tree initialized from StoreTreeEntity (storeId={})", entity.getStoreId());
        } finally {
            lock.unlock();
        }
    }

    public boolean isRoot(Node node) {
        boolean res = node.getParentId() == -1;
        logger.debug("isRoot called for node ID={}, result={}", node.getMyId(), res);

        return res;
    }

    public boolean isRootById(int id) {
        boolean res = root.getMyId() == id;
        logger.debug("isRootById called for ID={}, result={}", id, res);
        return res;
    }

    // for delete-> must check it:
    public boolean deleteNode(int userId) {
        logger.debug("deleteNode called for userId={}", userId);
        lock.lock();
        try {
            if (root.getMyId() == userId) {
                root = null; // delete whole tree if root matches -> close store
                logger.debug("Root node deleted for userId={}", userId);
                return true;
            }
            boolean result = root.deleteNode(userId);
            logger.debug("Node deletion result for userId={}: {}", userId, result);
            return result;
        } finally {
            lock.unlock();
        }
    }

    public Node getNodeById(int searchId) {
        logger.debug("getNodeById called with searchId={}", searchId);
        lock.lock();
        try {
            Node node = root.getNode(searchId);
            logger.debug("getNodeById result for searchId={}: {}", searchId, node != null ? "Found" : "Not Found");
            return node;
        } finally {
            lock.unlock();
        }

    }

    public Node getRoot() {
        logger.debug("getRoot called");
        return this.root;
    }

    public List<Node> getAllNodes() {
        logger.debug("getAllNodes() called");
        lock.lock();
        try {
            List<Node> allNodes = new ArrayList<>();
            TreeIterator iterator = new TreeIterator(root);
            while (iterator.hasNext()) {
                allNodes.add(iterator.next());
            }
            return allNodes;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<Node> iterator() {
        logger.debug("iterator() called, returning TreeIterator");
        synchronized (lock) {
            return new TreeIterator(root);
        }
    }

}
