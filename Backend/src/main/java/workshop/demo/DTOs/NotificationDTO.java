package workshop.demo.DTOs;

public class NotificationDTO {

    public enum NotificationType {
        NORMAL,
        OFFER
    }

    private String message;
    private String senderName;
    private int storeId;
    private String receiverName;
    private NotificationType type;
    private boolean toBeOwner;//else manager

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
}
