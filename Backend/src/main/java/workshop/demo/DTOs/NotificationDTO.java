package workshop.demo.DTOs;

public class NotificationDTO {

    public enum NotificationType {
        NORMAL,
        OFFER
    }

    private String message;
    private String receiverName;
    private NotificationType type;
    private boolean toBeOwner;//else manager

    public NotificationDTO(String message, String receiverName, NotificationType type, boolean toBeOwner) {
        this.message = message;
        this.receiverName = receiverName;
        this.type = type;
        this.toBeOwner = toBeOwner;
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
