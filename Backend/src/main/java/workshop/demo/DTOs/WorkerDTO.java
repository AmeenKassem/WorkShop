package workshop.demo.DTOs;

import java.security.Permission;

public class WorkerDTO {

    public String username;
    public boolean isManager;
    public boolean isOwner;
    public String storeName;
    public Permission[] permissions;

    public WorkerDTO(String username, boolean isManager, boolean isOwner, String storeName,Permission[] permissions) {
        this.username = username;
        this.isManager = isManager;
        this.isOwner = isOwner;
        this.storeName = storeName;
        this.permissions = permissions;
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
