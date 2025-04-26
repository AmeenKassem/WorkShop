package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Node {

    private int myId;
    private Authorization myAuth;//null if owner
    private boolean isManager;//false->owner
    private List<Node> children;
    private int parentId;//-1 if I'm the boss

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
        this.children.add(child);
        child.parentId = this.myId;

    }

    public Node getNode(int searchId) {
        if (this.myId == searchId) {
            return this;
        }
        if (children != null) {
            for (Node child : children) {
                Node result = child.getNode(searchId);
                if (result != null) {
                    return result;
                }
            }
        }

        return null; // not found
    }

    //might delete later
    public void addAuthrization(List<Permission> toAdd, int parentId) throws Exception {
        if (!isManager || myAuth == null) {
            throw new Exception("the owner is fully authorized, can't manipulate the aothrization!");
        }
        if (this.parentId != parentId) {
            throw new Exception("this owner can't manipulate the authorization for this manager");
        }
        myAuth.addAuthorization(toAdd);

    }

    public void updateAuthorization(List<Permission> toAdd, int parentId) throws Exception {
        if (!isManager || myAuth == null) {
            throw new Exception("the owner is fully authorized, can't manipulate the aothrization!");
        }
        if (this.parentId != parentId) {
            throw new Exception("this owner can't manipulate the authorization for this manager");
        }
        myAuth.updateAuthorixation(toAdd);
    }

    //returns an instance of node and null iff not a child
    public Node getChild(int id) {
        for (Node chiNode : children) {
            if (chiNode.getMyId() == id) {
                return this;
            }
        }
        return null;
    }

    //here must check if it really deletes it----------------------
    public boolean deleteNode(int userId) {

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
                // recursively search in child subtree
                if (child.deleteNode(userId)) {
                    return true;
                }
            }
        }
        return false; // not found in this branch
    }

    public int getParentId() {
        return this.parentId;
    }

    public List<Node> getChildren() {
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
