package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class StoreTreeEntity {

    @Id
    private int storeId; // same as key in map

    // @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    // @JoinColumn(name = "store_id") // FOREIGN KEY stored in Node table
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "store_id") // matches foreign key in Node
    private List<Node> allNodes = new ArrayList<>();

    public StoreTreeEntity(int storeId, List<Node> allNodes) {
        this.storeId = storeId;
        this.allNodes = allNodes;
    }

    public StoreTreeEntity() {
        // JPA requires this constructor
    }

    public List<Node> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(List<Node> allNodes) {
        this.allNodes = allNodes;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

}
