package workshop.demo.DTOs;

import java.util.Set;

public class ManagerDTO {

    private int managerId;
    private String managerName;
    private int storeId;
    private Set<String> permissions;

    public ManagerDTO(int managerId, String managerName, int storeId, Set<String> permissions) {
        this.managerId = managerId;
        this.managerName = managerName;
        this.storeId = storeId;
        this.permissions = permissions;
    }

}
