package workshop.demo.DTOs;

import elemental.json.JsonValue;

public class NotificationDTO {

    public enum NotificationType {
        NORMAL,
        OFFER,
        USER_OFFER
    }

    private String message;
    private String senderName;
    private int storeId;
    private String receiverName;
    private NotificationType type;
    private boolean toBeOwner;//else manager
    private int bidId;

    public NotificationDTO(String message, String receiverName, NotificationType type, boolean toBeOwner,
            String senderName, int storeId) {
        this.message = message;
        this.receiverName = receiverName;
        this.type = type;
        this.toBeOwner = toBeOwner;
        this.senderName = senderName;
        this.storeId = storeId;
    }



    public NotificationDTO() {
    }

    public NotificationDTO(String message2, NotificationType userOffer, String senderName2, int storeId2, int bidId) {
        this.message = message2;
        this.type = userOffer;
        this.senderName = senderName2;
        this.storeId = storeId2;
        this.bidId = bidId;
    }



    // Getters
    public String getMessage() {
        return message;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public NotificationType getType() {
        return type;
    }

    public boolean getToBeOwner() {
        return this.toBeOwner;
    }

    public String getSenderName() {
        return senderName;
    }

    public int getStoreId() {
        return storeId;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public int getBidId() {
        return bidId;
    }
}
