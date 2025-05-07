package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class SuperDataStructure {

    private Map<Integer, Tree> employees;
    private final ConcurrentHashMap<Integer, ReentrantLock> storeLocks = new ConcurrentHashMap<>();

    public SuperDataStructure() {
        employees = new ConcurrentHashMap<>();
    }

    public void addNewStore(int storeID, int bossId) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            this.employees.put(storeID, new Tree(bossId, false, -1));
        } finally {
            lock.unlock();
        }
    }

    public boolean checkToAdd(int storeID, int ownerId, int newOwnerId) throws UIException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (this.employees.get(storeID).getNodeById(ownerId) == null) {
                throw new UIException("Owner does not exist in this store", ErrorCodes.NO_PERMISSION);
            }
            Node child = employees.get(storeID).getNodeById(newOwnerId);
            if (child != null && !child.getIsManager()) {
                throw new UIException("This worker is already an owner/manager", ErrorCodes.NO_PERMISSION);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean checkToAddManager(int storeID, int ownerId, int newOwnerId) throws UIException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (this.employees.get(storeID).getNodeById(ownerId) == null) {
                throw new UIException("Owner does not exist in this store", ErrorCodes.NO_PERMISSION);
            }
            Node child = employees.get(storeID).getNodeById(newOwnerId);
            if (child != null && child.getIsManager()) {
                throw new UIException("This worker is already an owner/manager", ErrorCodes.NO_PERMISSION);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void addNewOwner(int storeID, int ownerId, int newOwnerId) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            this.employees.get(storeID).getNodeById(ownerId).addChild(new Node(newOwnerId, false, ownerId));
        } finally {
            lock.unlock();
        }
    }

    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws UIException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            Node toDelete = this.employees.get(storeID).getNodeById(OwnerToDelete);
            if (this.employees.get(storeID).getNodeById(ownerID) == null) {
                throw new UIException("Owner does not exist in this store", ErrorCodes.NO_PERMISSION);
            }
            if (toDelete == null) {
                throw new UIException("Cannot delete: user is not an owner", ErrorCodes.NO_PERMISSION);
            }
            if (toDelete.getParentId() != ownerID) {
                throw new UIException("You do not own this ownership", ErrorCodes.NO_PERMISSION);
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

    public void changeAuthoToManager(int storeID, int ownerID, int managerId, List<Permission> per) throws UIException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            Node toChange = this.employees.get(storeID).getNodeById(managerId);
            if (toChange == null) {
                throw new UIException("Manager not found", ErrorCodes.USER_NOT_FOUND);
            }
            if (!toChange.getIsManager()) {
                throw new UIException("User is not a manager", ErrorCodes.NO_PERMISSION);
            }
            if (toChange.getParentId() != ownerID) {
                throw new UIException("Owner does not have permission to change this manager", ErrorCodes.NO_PERMISSION);
            }
            toChange.updateAuthorization(per, ownerID);
        } finally {
            lock.unlock();
        }
    }

    public void deleteManager(int storeID, int ownerID, int managerId) throws UIException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            Node toDelete = this.employees.get(storeID).getNodeById(managerId);
            if (toDelete == null) {
                throw new UIException("Manager not found", ErrorCodes.USER_NOT_FOUND);
            }
            if (!toDelete.getIsManager()) {
                throw new UIException("User is not a manager", ErrorCodes.NO_PERMISSION);
            }
            if (toDelete.getParentId() != ownerID) {
                throw new UIException("Owner does not have permission to delete this manager", ErrorCodes.NO_PERMISSION);
            }
            this.employees.get(storeID).deleteNode(managerId);
        } finally {
            lock.unlock();
        }
    }

    public boolean checkDeactivateStore(int storeId, int ownerId) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
        lock.lock();
        try {
            return this.employees.get(storeId).getRoot().getMyId() == ownerId;
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

    public Map<Integer, Tree> getEmployees() {
        return employees;
    }
}
