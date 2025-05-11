<<<<<<< HEAD:Backend/src/main/java/workshop/demo/DomainLayer/StoreUserConnection/Node.java
package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class Node {

        private static final Logger logger = LoggerFactory.getLogger(Node.class);


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
            logger.debug("Child node {} added to parent {}", child.getMyId(), this.myId);
        }
    }

    public Node getNode(int searchId) {
        if (this.myId == searchId) {
            logger.debug("getNode: Found node with id={}", searchId);
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
        logger.debug("getNode: Node with id={} not found under parent {}", searchId, this.myId);
        return null; // not found
    }

    public void addAuthrization(List<Permission> toAdd, int parentId) throws UIException {
        synchronized (this) {
            if (!isManager || myAuth == null) {
                logger.error("Attempt to addAuthrization failed: not a manager or no auth");
                throw new UIException("Owner is fully authorized; cannot manipulate authorization",
                        ErrorCodes.NO_PERMISSION);
            }
            if (this.parentId != parentId) {
                logger.error("addAuthrization failed: parent mismatch (expected {}, found {})", parentId, this.parentId);
                throw new UIException("This owner cannot manipulate authorization for this manager",
                        ErrorCodes.NO_PERMISSION);
            }
            logger.debug("addAuthrization: Adding {} permissions to manager {}", toAdd.size(), this.myId);
            myAuth.addAuthorization(toAdd);
        }
    }

    public void updateAuthorization(List<Permission> toAdd, int parentId) throws UIException {
        synchronized (this) {
            if (!isManager || myAuth == null) {
                logger.error("Attempt to updateAuthorization failed: not a manager or no auth");
                throw new UIException("Owner is fully authorized; cannot manipulate authorization",
                        ErrorCodes.NO_PERMISSION);
            }
            if (this.parentId != parentId) {
                logger.error("updateAuthorization failed: parent mismatch (expected {}, found {})", parentId, this.parentId);
                throw new UIException("This owner cannot manipulate authorization for this manager",
                        ErrorCodes.NO_PERMISSION);
            }
            logger.debug("updateAuthorization: Updating {} permissions for manager {}", toAdd.size(), this.myId);
            myAuth.updateAuthorixation(toAdd);
        }
    }

    public Node getChild(int id) {
        for (Node chiNode : children) {
            if (chiNode.getMyId() == id) {
                logger.debug("getChild: Found child with id={} under parent {}", id, this.myId);
                return this;
            }
        }
        logger.debug("getChild: Child with id={} not found under parent {}", id, this.myId);
        return null;
    }

    public boolean deleteNode(int userId) {
        synchronized (this) {
            if (children == null || children.isEmpty()) {
                logger.debug("deleteNode: No children to search under node {}", this.myId);
                return false;
            }

            Iterator<Node> iterator = children.iterator();

            while (iterator.hasNext()) {
                Node child = iterator.next();
                if (child.getMyId() == userId) {
                    iterator.remove(); // safely remove the child node
                    logger.debug("deleteNode: Node {} deleted from parent {}", userId, this.myId);
                    return true;
                } else {
                    if (child.deleteNode(userId)) {
                        return true;
                    }
                }
            }
        }
        logger.debug("deleteNode: Node {} not found under parent {}", userId, this.myId);
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
=======
package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class Node {

        private static final Logger logger = LoggerFactory.getLogger(Node.class);


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
            logger.debug("Child node {} added to parent {}", child.getMyId(), this.myId);
        }
    }

    public Node getNode(int searchId) {
        if (this.myId == searchId) {
            logger.debug("getNode: Found node with id={}", searchId);
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
        logger.debug("getNode: Node with id={} not found under parent {}", searchId, this.myId);
        return null; // not found
    }

    public void addAuthrization(List<Permission> toAdd, int parentId) throws UIException {
        synchronized (this) {
            if (!isManager || myAuth == null) {
                logger.error("Attempt to addAuthrization failed: not a manager or no auth");
                throw new UIException("Owner is fully authorized; cannot manipulate authorization",
                        ErrorCodes.NO_PERMISSION);
            }
            if (this.parentId != parentId) {
                logger.error("addAuthrization failed: parent mismatch (expected {}, found {})", parentId, this.parentId);
                throw new UIException("This owner cannot manipulate authorization for this manager",
                        ErrorCodes.NO_PERMISSION);
            }
            logger.debug("addAuthrization: Adding {} permissions to manager {}", toAdd.size(), this.myId);
            myAuth.addAuthorization(toAdd);
        }
    }

    public void updateAuthorization(List<Permission> toAdd, int parentId) throws UIException {
        synchronized (this) {
            if (!isManager || myAuth == null) {
                logger.error("Attempt to updateAuthorization failed: not a manager or no auth");
                throw new UIException("Owner is fully authorized; cannot manipulate authorization",
                        ErrorCodes.NO_PERMISSION);
            }
            if (this.parentId != parentId) {
                logger.error("updateAuthorization failed: parent mismatch (expected {}, found {})", parentId, this.parentId);
                throw new UIException("This owner cannot manipulate authorization for this manager",
                        ErrorCodes.NO_PERMISSION);
            }
            logger.debug("updateAuthorization: Updating {} permissions for manager {}", toAdd.size(), this.myId);
            myAuth.updateAuthorixation(toAdd);
        }
    }

    public Node getChild(int id) {
        for (Node chiNode : children) {
            if (chiNode.getMyId() == id) {
                logger.debug("getChild: Found child with id={} under parent {}", id, this.myId);
                return this;
            }
        }
        logger.debug("getChild: Child with id={} not found under parent {}", id, this.myId);
        return null;
    }

    public boolean deleteNode(int userId) {
        synchronized (this) {
            if (children == null || children.isEmpty()) {
                logger.debug("deleteNode: No children to search under node {}", this.myId);
                return false;
            }

            Iterator<Node> iterator = children.iterator();

            while (iterator.hasNext()) {
                Node child = iterator.next();
                if (child.getMyId() == userId) {
                    iterator.remove(); // safely remove the child node
                    logger.debug("deleteNode: Node {} deleted from parent {}", userId, this.myId);
                    return true;
                } else {
                    if (child.deleteNode(userId)) {
                        return true;
                    }
                }
            }
        }
        logger.debug("deleteNode: Node {} not found under parent {}", userId, this.myId);
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
>>>>>>> a5d20fe39422f9af1fbf9b58efcc0388f6605f11:src/main/java/workshop/demo/DomainLayer/StoreUserConnection/Node.java
