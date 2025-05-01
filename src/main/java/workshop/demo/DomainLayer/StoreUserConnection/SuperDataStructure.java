package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.LinkedList;
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

    public boolean checkToAdd(int storeID, int ownerId, int newOnwerId) throws Exception {
        if (this.employees.get(storeID).getNodeById(ownerId) == null) {
            throw new Exception("can't manupulate ownership/managment: this is not the owner of this store!");
        }
        Node child = employees.get(storeID).getNodeById(newOnwerId);
        if (child != null && !child.getIsManager()) {
            throw new Exception("this worker is already an owner/manager");
        }

        return true;
    }

    public boolean checkToAddManager(int storeID, int ownerId, int newOnwerId) throws Exception {
        if (this.employees.get(storeID).getNodeById(ownerId) == null) {
            throw new Exception("can't manupulate ownership/managment: this is not the owner of this store!");
        }
        Node child = employees.get(storeID).getNodeById(newOnwerId);
        if (child != null && child.getIsManager()) {
            throw new Exception("this worker is already an owner/manager");
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
        this.employees.get(storeID).getNodeById(ownerId).addChild(new Node(newManagerId, true, ownerId));

    }

    public void changeAuthoToManager(int storeID, int ownerID, int managerId, List<Permission> per) throws Exception {
        Node toChange = this.employees.get(storeID).getNodeById(managerId);
        if (toChange == null) {
            throw new Exception(String.format("this user: {} is not a manager int this store", managerId));
        }
        if (!toChange.getIsManager()) {
            throw new Exception(String.format("this user: {} is not a manager-> owner", managerId));
        }
        if (toChange.getParentId() != ownerID) {
            throw new Exception(String.format("this owner: {} can't change manager's issue: {}", ownerID, managerId));
        }
        toChange.updateAuthorization(per, ownerID);
    }

    public void deleteManager(int storeID, int ownerID, int managerId) throws Exception {
        Node toDelete = this.employees.get(storeID).getNodeById(managerId);
        if (toDelete == null) {
            throw new Exception(String.format("this user: {} is not a manager in this store", managerId));
        }
        if (!toDelete.getIsManager()) {
            throw new Exception(String.format("this user: {} is not a manager-> owner", managerId));
        }
        if (toDelete.getParentId() != ownerID) {
            throw new Exception(String.format("this owner: {} can't change manager's issue: {}", ownerID, managerId));
        }
        this.employees.get(storeID).deleteNode(managerId);
    }

    public boolean checkDeactivateStore(int storeId, int ownerId) throws Exception {
        if (this.employees.get(storeId).getRoot().getMyId() != ownerId) {
            return false;
        }
        return true;

    }

    public List<Integer> getWorkersInStore(int storeId) {
        Tree workers = this.employees.get(storeId);
        List<Integer> toReturn = new LinkedList<>();
        toReturn.add(workers.getRoot().getMyId());
        for (Node node : workers.getRoot().getChildren()) {
            toReturn.add(node.getMyId());
        }

        return toReturn;
    }

    public Tree getWorkersTreeInStore(int storeId) {
        return employees.get(storeId);
    }

    public void closeStore(int storeID) {
        this.employees.remove(storeID);
    }

    //for tests:
    public Map<Integer, Tree> getEmployees() {
        return employees;
    }

    
}
