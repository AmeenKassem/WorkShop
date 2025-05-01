package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public class Tree implements Iterable<Node> {

    private Node root;//root is the boss
    private final ReentrantLock lock = new ReentrantLock();

    //FOR BOSS: isManager->false + parenetId-> -1
    public Tree(int myId, boolean isManager, int parentId) {
        this.root = new Node(myId, isManager, parentId);
    }

    public boolean isRoot(Node node) {
        return node.getParentId() == -1;
    }

    public boolean isRootById(int id) {
        return root.getMyId() == id;
    }

    //for delete-> must check it:
    public boolean deleteNode(int userId) {
        lock.lock();
        try {
            if (root.getMyId() == userId) {
                root = null; // delete whole tree if root matches -> close store
                return true;
            }
            return root.deleteNode(userId);
        } finally {
            lock.unlock();
        }
    }

    public Node getNodeById(int searchId) {
        lock.lock();
        try {
            return root.getNode(searchId);
        } finally {
            lock.unlock();
        }

    }

    public Node getRoot() {
        return this.root;
    }

    @Override
    public Iterator<Node> iterator() {
        synchronized (lock) {
            return new TreeIterator(root);
        }
    }

}
