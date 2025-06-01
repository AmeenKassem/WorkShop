package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.List;

public class Offer {

    private int senderId;
    private int receiverId;
    private boolean toBeOwner;//false -> to be manager
    private List<Permission> permissions;
    private String message;
    private boolean approve;

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

    public String message() {
        return message;
    }

    public boolean getApprove() {
        return approve;
    }

    public void SetApprove(boolean app) {
        this.approve = app;
    }
}
