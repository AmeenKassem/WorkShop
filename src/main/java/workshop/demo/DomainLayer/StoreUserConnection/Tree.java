package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.Iterator;

public class Tree implements Iterable<Node> {

    private Node root;//root is the boss

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
        if (root.getMyId() == userId) {
            root = null; // delete whole tree if root matches -> close store
            return true;
        }
        return root.deleteNode(userId);
    }

    public Node getNodeById(int searchId) {
        return root.getNode(searchId);

    }

    public Node getRoot() {
        return this.root;
    }

    @Override
    public Iterator<Node> iterator() {
        return new TreeIterator(root);
    }

}
