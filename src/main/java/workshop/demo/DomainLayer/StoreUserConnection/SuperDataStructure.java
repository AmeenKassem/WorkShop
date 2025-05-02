package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SuperDataStructure {

    private Map<Integer, Tree> employees;//sotoreId, tree of the owners/managers 
    private final ConcurrentHashMap<Integer, ReentrantLock> storeLocks = new ConcurrentHashMap<>();

    public SuperDataStructure() {
        employees = new ConcurrentHashMap<>();
    }

    public void addNewStore(int storeID, int bossId) {
        //this line makes differents lock for each store:
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            this.employees.put(storeID, new Tree(bossId, false, -1));

        } finally {
            lock.unlock();
        }
    }

    public boolean checkToAdd(int storeID, int ownerId, int newOnwerId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (this.employees.get(storeID).getNodeById(ownerId) == null) {
                throw new Exception("can't manupulate ownership/managment: this is not the owner of this store!");
            }
            Node child = employees.get(storeID).getNodeById(newOnwerId);
            if (child != null && !child.getIsManager()) {
                throw new Exception("this worker is already an owner/manager");
            }

            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean checkToAddManager(int storeID, int ownerId, int newOnwerId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (this.employees.get(storeID).getNodeById(ownerId) == null) {
                throw new Exception("can't manupulate ownership/managment: this is not the owner of this store!");
            }
            Node child = employees.get(storeID).getNodeById(newOnwerId);
            if (child != null && child.getIsManager()) {
                throw new Exception("this worker is already an owner/manager");
            }

            return true;
        } finally {
            lock.unlock();
        }
    }

    public void addNewOwner(int storeID, int ownerId, int newOnwerId) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            this.employees.get(storeID).getNodeById(ownerId).addChild(new Node(newOnwerId, false, ownerId));
        } finally {
            lock.unlock();
        }

    }

    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }

    }

    public void addNewManager(int storeID, int ownerId, int newManagerId) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            this.employees.get(storeID).getNodeById(ownerId).addChild(new Node(newManagerId, true, ownerId));

        } finally {
            lock.unlock();
        }

    }

    public void changeAuthoToManager(int storeID, int ownerID, int managerId, List<Permission> per) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    public void deleteManager(int storeID, int ownerID, int managerId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    public boolean checkDeactivateStore(int storeId, int ownerId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
        lock.lock();
        try {
            if (this.employees.get(storeId).getRoot().getMyId() != ownerId) {
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }

    }

    public List<Integer> getWorkersInStore(int storeId) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
        lock.lock();
        try {
            Tree workers = this.employees.get(storeId);
            List<Integer> toReturn = new LinkedList<>();
            toReturn.add(workers.getRoot().getMyId());
            for (Node node : workers.getRoot().getChildren()) {
                toReturn.add(node.getMyId());
            }

            return toReturn;
        } finally {
            lock.unlock();
        }
    }

    public Tree getWorkersTreeInStore(int storeId) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
        lock.lock();
        try {
            return employees.get(storeId);
        } finally {
            lock.unlock();
        }
    }

    public void closeStore(int storeID) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            this.employees.remove(storeID);
        } finally {
            lock.unlock();
        }
    }

    //for tests:
    public Map<Integer, Tree> getEmployees() {
        return employees;
    }

    
}
