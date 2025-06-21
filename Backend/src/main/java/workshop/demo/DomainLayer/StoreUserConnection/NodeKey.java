package workshop.demo.DomainLayer.StoreUserConnection;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class NodeKey implements Serializable {

    @Column(name = "store_id")
    private int storeId;

    @Column(name = "my_id")
    private int myId;

    public NodeKey() {
    } // required

    public NodeKey(int storeId, int myId) {
        this.storeId = storeId;
        this.myId = myId;
    }

    // Getters and setters
    public int getStoreId() {
        return storeId;
    }

    public int getMyId() {
        return myId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeKey)) {
            return false;
        }
        NodeKey that = (NodeKey) o;
        return storeId == that.storeId && myId == that.myId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, myId);
    }
}
