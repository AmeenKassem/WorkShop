package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;

@Entity
public class Offer {

    @EmbeddedId
    private OfferKey id;

    private boolean toBeOwner;//false -> to be manager

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<Permission> permissions;

    private String message;

    private boolean approve;

    public Offer() {
    } // JPA requires this

    public Offer(int storeId, int senderId, int receiverId, boolean toBeOwner, List<Permission> permissions, String message) {
        this.id = new OfferKey(storeId, senderId, receiverId);
        this.toBeOwner = toBeOwner;
        this.permissions = permissions;// null if owner
    }

    // Getters
    public OfferKey getId() {
        return id;
    }

    public boolean isToBeOwner() {
        return toBeOwner;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public String getMessage() {
        return message;
    }

    public boolean getApprove() {
        return approve;
    }

    public void setApprove(boolean app) {
        this.approve = app;
    }

    // Convenience methods
    public int getStoreId() {
        return id.getStoreId();
    }

    public int getSenderId() {
        return id.getSenderId();
    }

    public int getReceiverId() {
        return id.getReceiverId();
    }
}
