package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Offer;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.StoreUserConnection.SuperDataStructure;

@Repository
public class SUConnectionRepository implements ISUConnectionRepo {

    @Autowired
    private SuperDataStructure data;

    @Autowired
    public SUConnectionRepository() {
        //data = new SuperDataStructure();
    }

    @Override
    public boolean addNewStoreOwner(int storeId, int bossID) {
        data.addNewStore(storeId, bossID);
        return true;

    }

    @Override
    public boolean checkToAddOwner(int storeID, int ownerID, int newOwnerId) throws Exception {// for owner
        try {
            this.data.checkToAddOwner(storeID, ownerID, newOwnerId);
        } catch (Exception e) {
            throw e;
        }
        return true;
    }

    @Override
    public boolean AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception {
        try {
            this.data.addNewOwner(storeID, ownerID, newOwnerId);

        } catch (Exception e) {
            throw e;
        }
        return true;

    }

    @Override
    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception {

        try {
            this.data.DeleteOwnershipFromStore(storeID, ownerID, OwnerToDelete);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void checkToAddManager(int storeID, int ownerID, int newOwnerId) throws Exception {
        try {
            this.data.checkToAddManager(storeID, ownerID, newOwnerId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void AddManagerToStore(int storeID, int ownerId, int managerId) throws Exception {
        try {
            this.data.addNewManager(storeID, ownerId, managerId);

        } catch (Exception e) {
            throw e;
        }
    }

    // @Override
    // public void givePermissions(int ownerId, int managerId, int storeID,
    // List<Permission> autorization) throw Exception {
    // }
    @Override
    public void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization)
            throws Exception {
        try {
            this.data.changeAuthoToManager(storeID, ownerId, managerId, autorization);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void deleteManager(int storeId, int ownerId, int managerId) throws Exception {
        try {
            this.data.deleteManager(storeId, ownerId, managerId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Node getWorkerInStoreById(int storeId, int workerId) throws Exception {
        if (!this.data.checkStoreExist(storeId)) {
            throw new Exception("store does not exist in superDS");
        }
        return this.data.getWorkersTreeInStore(storeId).getNodeById(workerId);
    }

    @Override
    public List<Integer> getWorkersInStore(int storeId) throws Exception {
        return this.data.getWorkersInStore(storeId);
    }

    @Override
    public boolean checkDeactivateStore(int storeId, int ownerId) throws Exception {
        return this.data.checkDeactivateStore(storeId, ownerId);
    }

    @Override
    public void closeStore(int storeId) throws Exception {
        this.data.closeStore(storeId);
    }
    // changed userid to storeid

    @Override
    public boolean manipulateItem(int userId, int storeId, Permission permission) throws Exception {
        Node Worker = getWorkerInStoreById(storeId, userId);
        if (Worker == null) {
            throw new Exception("this user is not a worker in this store");
        }
        // owner is fully authorized:
        if (!Worker.getIsManager() || Worker.getMyAuth().hasAutho(permission)) {
            return true;
        } else {
            return false;
        }

    }

    // for tests
    public SuperDataStructure getData() {
        return this.data;
    }

    public void checkMainOwnerToDeactivateStore_ThrowException(int storeId, int userId) throws DevException {
        try {
            if (!checkDeactivateStore(storeId, userId)) {
                throw new DevException("Only the boss/main owner can perform this action on store " + storeId);
            }
        } catch (Exception e) {
            throw new DevException("failed to check ownership for this userid: " + e.getMessage());
        }
    }

    @Override
    public void makeOffer(int storeId, int senderId, int reciverId, boolean toBeOwner, List<Permission> per,
            String Message) throws Exception {
        Offer offer = new Offer(storeId, senderId, reciverId, toBeOwner, per, Message);
        this.data.makeOffer(offer, storeId);
    }

    @Override
    public List<Permission> deleteOffer(int storeId, int senderId, int reciverId) throws Exception {
        return this.data.deleteOffer(storeId, senderId, reciverId);
    }

    @Override
    public Offer getOffer(int storeId, int senderId, int reciverId) throws Exception {
        return this.data.getOffer(storeId, senderId, reciverId);
    }

    @Override
    public List<Integer> getStoresIdForUser(int userId) {
        return this.data.getStoresIdForUser(userId);
    }

    @Override
    public int removeUserAccordingly(int userId) throws Exception {
        return this.data.removeUserAccordingly(userId);
    }

    public void clear() {
        data.clearData();

        this.data = new SuperDataStructure();
    }

    @Override
    public Permission[] getPermissions(Node node) {
        return this.data.getPermissions(node);
    }

    @Override
    public List<Node> getAllWorkers(int storeId) throws Exception {
        return this.data.getAllWorkers(storeId);
    }

    @Override
    public boolean hasPermission(int userId, int storeId, Permission permission) {
        try {
            Node worker = getWorkerInStoreById(storeId, userId);
            if (worker == null) {
                return false;
            }

            // If the user is an owner, allow all
            if (!worker.getIsManager()) {
                return true;
            }

            // If the user is a manager, check for specific permission
            return worker.getMyAuth().hasAutho(permission);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Node> getOwnersInStore(int storeId) throws UIException, DevException {
        List<Node> owners = this.data.getAllWorkers(storeId);
        List<Node> ownersInStore = new ArrayList<>();
        for (Node worker : owners) {
            if (!worker.getIsManager()) { // If the worker is an owner
                ownersInStore.add(worker);
            }
        }
        return ownersInStore;
    }

}
