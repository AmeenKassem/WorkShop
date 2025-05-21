package workshop.demo.DTOs;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class WorkerDTO {

    public String Username;
    public boolean isManager;
    public boolean isOwner;
    public String storeName;
    public Permission[] permessions;
    public boolean setByMe;
    public int workerId;

    //change
    public WorkerDTO(int workerid, String username, boolean isManager, boolean isOwner, String storeName, Permission[] permissions, boolean setByMe) {
        this.Username = username;
        this.isManager = isManager;
        this.isOwner = isOwner;
        this.storeName = storeName;
        this.permessions = permissions;
        this.setByMe = setByMe;
        this.workerId = workerid;
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
