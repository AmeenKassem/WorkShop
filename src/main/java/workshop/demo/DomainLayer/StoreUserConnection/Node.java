package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;

public class Node {

    private int myId;
    private Authorization myAuth; // null if owner
    private boolean isManager; // false → owner
    private final List<Node> children;
    private int parentId; // -1 if I'm the boss

    public Node(int myId, boolean isManager, int parentId) {
        this.myId = myId;
        if (isManager) {
            this.myAuth = new Authorization();
        } else {
            this.myAuth = null;
        }
        this.isManager = isManager;
        this.children = Collections.synchronizedList(new ArrayList<>());
        this.parentId = parentId;
    }

    public void addChild(Node child) {
        synchronized (this) {
            this.children.add(child);
            child.parentId = this.myId;
        }
    }

    public Node getNode(int searchId) {
        if (this.myId == searchId) {
            return this;
        }
        if (children != null) {
            synchronized (children) {
                for (Node child : children) {
                    Node result = child.getNode(searchId);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null; // not found
    }

    public void addAuthrization(List<Permission> toAdd, int parentId) throws UIException {
        synchronized (this) {
            if (!isManager || myAuth == null) {
                throw new UIException("Owner is fully authorized; cannot manipulate authorization",
                        ErrorCodes.NO_PERMISSION);
            }
            if (this.parentId != parentId) {
                throw new UIException("This owner cannot manipulate authorization for this manager",
                        ErrorCodes.NO_PERMISSION);
            }
            myAuth.addAuthorization(toAdd);
        }
    }

    public void updateAuthorization(List<Permission> toAdd, int parentId) throws UIException {
        synchronized (this) {
            if (!isManager || myAuth == null) {
                throw new UIException("Owner is fully authorized; cannot manipulate authorization",
                        ErrorCodes.NO_PERMISSION);
            }
            if (this.parentId != parentId) {
                throw new UIException("This owner cannot manipulate authorization for this manager",
                        ErrorCodes.NO_PERMISSION);
            }
            myAuth.updateAuthorixation(toAdd);
        }
    }

    public Node getChild(int id) {
        for (Node chiNode : children) {
            if (chiNode.getMyId() == id) {
                return this;
            }
        }
        return null;
    }

    public boolean deleteNode(int userId) {
        synchronized (this) {
            if (children == null || children.isEmpty()) {
                return false;
            }

            Iterator<Node> iterator = children.iterator();

            while (iterator.hasNext()) {
                Node child = iterator.next();
                if (child.getMyId() == userId) {
                    iterator.remove(); // safely remove the child node
                    return true;
                } else {
                    if (child.deleteNode(userId)) {
                        return true;
                    }
                }
            }
        }
        return false; // not found in this branch
    }

    public int getParentId() {
        return this.parentId;
    }

    public synchronized List<Node> getChildren() {
        return children;
    }

    public int getMyId() {
        return myId;
    }

    public boolean getIsManager() {
        return isManager;
    }

    public Authorization getMyAuth() {
        return myAuth;
    }
}
