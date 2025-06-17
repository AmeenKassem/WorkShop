package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // Primary key

    private int senderId;
    private int receiverId;
    private boolean toBeOwner;//false -> to be manager

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<Permission> permissions;
    private String message;
    private boolean approve;

    public Offer() {
    } // JPA requires this

    public Offer(int senderId, int receiverId, boolean toBeOwner, List<Permission> permissions, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.toBeOwner = toBeOwner;
        this.permissions = permissions;// null if owner
    }

    // Getters
    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
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

    public void SetApprove(boolean app) {
        this.approve = app;
    }
}
