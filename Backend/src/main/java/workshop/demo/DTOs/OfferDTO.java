package workshop.demo.DTOs;

import java.util.List;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class OfferDTO {

    private int senderId;
    private int receiverId;
    private boolean toBeOwner;//false -> to be manager
    List<Permission> permissions;

    public OfferDTO(int senderId, int receiverId, boolean toBeOwner, List<Permission> permissions) {
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
}
