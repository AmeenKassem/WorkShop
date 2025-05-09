package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tree implements Iterable<Node> {

    private static final Logger logger = LoggerFactory.getLogger(Tree.class);

    private Node root;// root is the boss
    private final ReentrantLock lock = new ReentrantLock();

    // FOR BOSS: isManager->false + parenetId-> -1
    public Tree(int myId, boolean isManager, int parentId) {
        this.root = new Node(myId, isManager, parentId);
        logger.debug("Tree initialized with root ID={}, isManager={}, parentId={}", myId, isManager, parentId);

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

    @Override
    public Iterator<Node> iterator() {
        logger.debug("iterator() called, returning TreeIterator");
        synchronized (lock) {
            return new TreeIterator(root);
        }
    }

}
