package workshop.demo.DTOs;

public class MessageDTO {

    private int senderId;
    private int receiverId;
    private String content;

    public MessageDTO(int senderId, int receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }
}
