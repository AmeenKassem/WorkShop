package workshop.demo.DTOs;

import java.security.Permission;

public class WorkerDTO {

    public int workerId;
    public String username;
    public boolean isManager;
    public boolean isOwner;
    public String storeName;
    public Permission[] permissions;

    public WorkerDTO(int workerId, String username, boolean isManager, boolean isOwner, String storeName, Permission[] permissions) {
        this.workerId = workerId;
        this.username = username;
        this.isManager = isManager;
        this.isOwner = isOwner;
        this.storeName = storeName;
        this.permissions = permissions;
    }

}
