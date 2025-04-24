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
        //also MUST CHECK if its a registed user in the service layer using User repo
        Tree currentWorkes = employees.get(storeID);
        if (!currentWorkes.isRootById(ownerId)) {
            throw new Exception("this is not the owner of this store!");
        }
        Node child = currentWorkes.getRoot().getChild(newOnwerId);
        if (child != null && !child.getIsManager()) {
            throw new Exception("this worker is already an owner");
        }

        return true;
    }

    public void addNewOwner(int storeID, int ownerId, int newOnwerId) {
        this.employees.get(storeID).getNodeById(ownerId).addChild(new Node(newOnwerId, false, ownerId));

    }

    public void addNewManager(int storeID, int ownerId, int newManagerId) {

    }

    public void addAuthoToManager(int storeID, int ownerID, int managerId, List<Permission> per) {

    }
}
