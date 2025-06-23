package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Entity
public class Node {

    @Transient
    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    @EmbeddedId
    @jakarta.persistence.AttributeOverrides({
        @jakarta.persistence.AttributeOverride(name = "storeId", column = @Column(name = "store_id")),
        @jakarta.persistence.AttributeOverride(name = "myId", column = @Column(name = "my_id"))
    })
    private NodeKey key;

    //private int myId;//this is my id,the user id
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Authorization myAuth; // null if owner

    private boolean isManager; // false → owner

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Node> children = Collections.synchronizedList(new ArrayList<>());

    //private int parentId; // -1 if I'm the boss
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "store_id", referencedColumnName = "store_id", insertable = false, updatable = false),
        @JoinColumn(name = "parent_id", referencedColumnName = "my_id", insertable = false, updatable = false)
    })
    private Node parent;

    public Node(int storeId, int myId, boolean isManager, Node parent) {
        this.key = new NodeKey(storeId, myId);
        //this.myId = myId;
        if (isManager) {
            this.myAuth = new Authorization();
        } else {
            this.myAuth = null;
        }
        this.isManager = isManager;
        this.children = Collections.synchronizedList(new ArrayList<>());
        this.parent = parent;
        // if (parent != null) {
        //     this.parentId = parent.getMyId();
        // } else {
        //     this.parentId = -1;
        // }
    }

    public Node() {
        this.children = new ArrayList<>();
    }

    public void addChild(Node child) {
        synchronized (this) {
            this.children.add(child);
            child.parent = this;
            logger.debug("Child node {} added to parent {}", child.getMyId(), this.key.getMyId());
        }
    }

    public Node getNode(int searchId) {
        if (this.key.getMyId() == searchId) {
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
        logger.debug("getNode: Node with id={} not found under parent {}", searchId, this.key.getMyId());
        return null; // not found
    }

    public void addAuthrization(List<Permission> toAdd, int parentId) throws UIException {
        synchronized (this) {
            if (!isManager || myAuth == null) {
                logger.error("Attempt to addAuthrization failed: not a manager or no auth");
                throw new UIException("Owner is fully authorized; cannot manipulate authorization",
                        ErrorCodes.NO_PERMISSION);
            }
            if (this.parent.getMyId() != parentId) {
                logger.error("addAuthrization failed: parent mismatch (expected {}, found {})", parentId, this.parent.getMyId());
                throw new UIException("This owner cannot manipulate authorization for this manager",
                        ErrorCodes.NO_PERMISSION);
            }
            logger.debug("addAuthrization: Adding {} permissions to manager {}", toAdd.size(), this.key.getMyId());
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
            if (this.parent.getMyId() != parentId) {
                logger.error("updateAuthorization failed: parent mismatch (expected {}, found {})", parentId, this.parent.getMyId());
                throw new UIException("This owner cannot manipulate authorization for this manager",
                        ErrorCodes.NO_PERMISSION);
            }
            logger.debug("updateAuthorization: Updating {} permissions for manager {}", toAdd.size(), this.key.getMyId());
            myAuth.updateAuthorixation(toAdd);
        }
    }

    public Node getChild(int id) {
        for (Node chiNode : children) {
            if (chiNode.getMyId() == id) {
                logger.debug("getChild: Found child with id={} under parent {}", id, this.key.getMyId());
                return this;
            }
        }
        logger.debug("getChild: Child with id={} not found under parent {}", id, this.key.getMyId());
        return null;
    }

    public boolean deleteNode(int userId) {
        synchronized (this) {
            if (children == null || children.isEmpty()) {
                logger.debug("deleteNode: No children to search under node {}", this.key.getMyId());
                return false;
            }

            Iterator<Node> iterator = children.iterator();

            while (iterator.hasNext()) {
                Node child = iterator.next();
                if (child.getMyId() == userId) {
                    iterator.remove(); // safely remove the child node
                    logger.debug("deleteNode: Node {} deleted from parent {}", userId, this.key.getMyId());
                    return true;
                } else {
                    if (child.deleteNode(userId)) {
                        return true;
                    }
                }
            }
        }
        logger.debug("deleteNode: Node {} not found under parent {}", userId, this.key.getMyId());
        return false; // not found in this branch
    }

    public int getParentId() {
        return this.parent != null ? parent.getMyId() : -1;
    }

    public synchronized List<Node> getChildren() {
        return children;
    }

    public int getMyId() {
        return this.key.getMyId();
    }

    public boolean getIsManager() {
        return isManager;
    }

    public Authorization getMyAuth() {
        return myAuth;
    }

    public NodeKey getKey() {
        return this.key;
    }

    public int getStoreId() {
        return key.getStoreId();
    }
}
