package workshop.demo.DTOs;

public class WorkerDTO {

    private String username;
    private boolean isManager;
    private boolean isOwner;
    private String storeName;

    public WorkerDTO(String username, boolean isManager, boolean isOwner, String storeName) {
        this.username = username;
        this.isManager = isManager;
        this.isOwner = isOwner;
        this.storeName = storeName;
    }

    public String getUsername() {
        return username;
    }

    public boolean isManager() {
        return isManager;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public String getStoreName() {
        return storeName;
    }
}
