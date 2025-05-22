package workshop.demo.DTOs;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class WorkerDTO {


    private String username;
    private boolean manager;
    private boolean owner;
    private String storeName;
    private Permission[] permessions;
    private boolean setByMe;
    private int workerId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public WorkerDTO(int workerid, String username, boolean isManager, boolean isOwner, String storeName, Permission[] permissions, boolean setByMe) {
        this.username = username;
        this.manager = isManager;
        this.owner = isOwner;
        this.storeName = storeName;
        this.permessions = permissions;
        this.setByMe = setByMe;
        this.workerId = workerid;
    }

    public WorkerDTO() {
    }

    public String getUsername() {
        return username;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public boolean isOwner() {
        return owner;
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
