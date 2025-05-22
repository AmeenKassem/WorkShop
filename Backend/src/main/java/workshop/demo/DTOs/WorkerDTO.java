package workshop.demo.DTOs;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class WorkerDTO {

    private String Username;
    private boolean isManager;
    private boolean isOwner;
    private String storeName;
    private Permission[] permessions;
    private boolean setByMe;
    private int workerId;

    public WorkerDTO(int workerId, String username, boolean isManager, boolean isOwner, String storeName, Permission[] permissions, boolean setByMe) {
        this.Username = username;
        this.isManager = isManager;
        this.isOwner = isOwner;
        this.storeName = storeName;
        this.permessions = permissions;
        this.setByMe = setByMe;
        this.workerId = workerId;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        this.Username = username;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Permission[] getPermessions() {
        return permessions;
    }

    public void setPermessions(Permission[] permessions) {
        this.permessions = permessions;
    }

    public boolean isSetByMe() {
        return setByMe;
    }

    public void setSetByMe(boolean setByMe) {
        this.setByMe = setByMe;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }
}
