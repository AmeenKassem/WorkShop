package workshop.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class WorkerDTO {

    private String username;
    private boolean manager;
    private boolean owner;
    private String storeName;
    private Permission[] permissions;
    private boolean setByMe;
    private int workerId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public WorkerDTO(int workerId, String username, boolean manager, boolean owner, String storeName, Permission[] permissions, boolean setByMe) {
        this.username = username;
        this.manager = manager;
        this.owner = owner;
        this.storeName = storeName;
        this.permissions = permissions;
        this.setByMe = setByMe;
        this.workerId = workerId;
    }

    public WorkerDTO() {
    }

    public String getUsername() {
        return username;
    }

    public boolean isManager() {
        return manager;
    }

    public boolean isOwner() {
        return owner;
    }

    public String getStoreName() {
        return storeName;
    }

    public Permission[] getPermessions() {
        return permissions;
    }

    public boolean isSetByMe() {
        return setByMe;
    }

    public int getWorkerId() {
        return workerId;
    }

}
