package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SuperDataStructure {

    private Map<Integer, Tree> employees;//sotoreId, tree of the owners/managers 

    public SuperDataStructure() {
        employees = new ConcurrentHashMap<>();
    }

    public void addNewStore(int storeID, int bossId) {
        this.employees.put(storeID, new Tree(bossId, false, -1));
    }

    public boolean checkAddNewOwner(int storeID, int ownerId, int newOnwerId) throws Exception {
        if (this.employees.get(storeID).getNodeById(ownerId) == null) {
            throw new Exception("can't manupulate ownership: this is not the owner of this store!");
        }
        Node child = employees.get(storeID).getNodeById(newOnwerId);
        if (child != null && !child.getIsManager()) {
            throw new Exception("this worker is already an owner");
        }

        return true;
    }

    public void addNewOwner(int storeID, int ownerId, int newOnwerId) {
        this.employees.get(storeID).getNodeById(ownerId).addChild(new Node(newOnwerId, false, ownerId));

    }

    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception {
        Node toDelete = this.employees.get(storeID).getNodeById(OwnerToDelete);
        if (this.employees.get(storeID).getNodeById(ownerID) == null) {
            throw new Exception("this owner does not own this store:");
        }
        if (toDelete == null) {
            throw new Exception("can't delete this owner: does not own this store");
        }
        if (toDelete.getParentId() != ownerID) {
            throw new Exception(String.format("this owner: %d does own the ownership of: %d ", ownerID, OwnerToDelete));
        }
        this.employees.get(storeID).deleteNode(OwnerToDelete);

    }

    public void addNewManager(int storeID, int ownerId, int newManagerId) {

    }

    public void addAuthoToManager(int storeID, int ownerID, int managerId, List<Permission> per) {

    }

    //for tests:
    public Map<Integer, Tree> getEmployees() {
        return employees;
    }
}
