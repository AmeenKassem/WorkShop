package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.InfrastructureLayer.NodeJPARepository;
import workshop.demo.InfrastructureLayer.OfferJpaRepository;
import workshop.demo.InfrastructureLayer.StoreTreeJPARepository;

@Component
public class SuperDataStructure {

    @Autowired
    private StoreTreeJPARepository storeTreeJPARepo;
    @Autowired
    private OfferJpaRepository offerJPARepo;
    @Autowired
    private NodeJPARepository nodeJPARepo;

    private Map<Integer, Tree> employees;
    private final Map<Integer, List<Offer>> offers;//storeId, list of offers
    private final ConcurrentHashMap<Integer, ReentrantLock> storeLocks = new ConcurrentHashMap<>();

    @Autowired
    public SuperDataStructure() {
        employees = new ConcurrentHashMap<>();
        this.offers = new ConcurrentHashMap<>();
    }

    // //----loading data--------
    // //@PostConstruct
    // @Transactional
    // public void loadFromDB() {
    //     for (StoreTreeEntity entity : storeTreeJPARepo.findAll()) {
    //         try {
    //             Tree tree = new Tree(entity); // Tree constructor handles building from DB
    //             employees.put(entity.getStoreId(), tree);
    //         } catch (DevException e) {
    //             System.err.println("Failed to load store tree for storeId=" + entity.getStoreId());
    //             e.printStackTrace();
    //         }
    //     }
    // }
    public void addNewStore(int storeID, int bossId) {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            Tree tree = new Tree(storeID, bossId, false);
            this.employees.put(storeID, tree);
            StoreTreeEntity entity = new StoreTreeEntity(storeID, tree.getAllNodes());
            storeTreeJPARepo.save(entity);

        } finally {
            lock.unlock();
        }
    }

    public boolean checkToAddOwner(int storeID, int ownerId, int newOnwerId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeID)) {
                throw new Exception("store does not exist in supreDS");
            }
            if (this.employees.get(storeID).getNodeById(ownerId) == null) {
                throw new UIException("Owner does not exist in this store", ErrorCodes.NO_PERMISSION);
            }
            if (this.employees.get(storeID).getNodeById(ownerId).getIsManager()) {
                throw new UIException("Manager can not make offers in this store", ErrorCodes.NO_PERMISSION);
            }
            Node child = employees.get(storeID).getNodeById(newOnwerId);
            if (child != null && !child.getIsManager()) {
                throw new UIException("This worker is already an owner/manager", ErrorCodes.NO_PERMISSION);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean checkToAddManager(int storeID, int ownerId, int newOwnerId) throws UIException, DevException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeID)) {
                throw new DevException("store does not exist in superDS");
            }
            if (this.employees.get(storeID).getNodeById(ownerId) == null) {
                throw new UIException("Owner does not exist in this store", ErrorCodes.NO_PERMISSION);
            }
            if (this.employees.get(storeID).getNodeById(ownerId).getIsManager()) {
                throw new UIException("Manager can not make offers in this store", ErrorCodes.NO_PERMISSION);
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

    public void addNewOwner(int storeID, int ownerId, int newOnwerId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeID)) {
                throw new Exception("store does not exist in superDS");
            }
            Node child = employees.get(storeID).getNodeById(newOnwerId);
            if (employees.get(storeID).getNodeById(ownerId) == null) {
                throw new UIException("This worker is not an owner", ErrorCodes.USER_NOT_FOUND);
            }
            if (child != null && !child.getIsManager()) {
                throw new UIException("This worker is already an owner/manager", ErrorCodes.NO_PERMISSION);
            }
            Node newOwner = new Node(storeID, newOnwerId, false, this.employees.get(storeID).getNodeById(ownerId));
            this.employees.get(storeID).getNodeById(ownerId).addChild(newOwner);
            nodeJPARepo.save(newOwner);
            //storeTreeJPARepo.save(new StoreTreeEntity(storeID, employees.get(storeID).getAllNodes()));
        } finally {
            lock.unlock();
        }
    }

    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws UIException, DevException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeID)) {
                throw new DevException("store does not exist in superDS");
            }
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
            NodeKey key = new NodeKey(storeID, OwnerToDelete);
            nodeJPARepo.deleteById(key);

        } finally {
            lock.unlock();
        }
    }

    public void addNewManager(int storeID, int ownerId, int newManagerId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeID)) {
                throw new Exception("store does not exist in superDS");
            }
            if (this.employees.get(storeID).getNodeById(ownerId) == null) {
                throw new Exception("this user is not the owner of this store");
            }
            Node child = employees.get(storeID).getNodeById(newManagerId);
            System.out.println(ownerId);
            System.out.print(newManagerId);
            if (child != null && child.getIsManager()) {
                throw new UIException("This worker is already an owner/manager", ErrorCodes.NO_PERMISSION);
            }
            Node newManager = new Node(storeID, newManagerId, true, this.employees.get(storeID).getNodeById(ownerId));
            this.employees.get(storeID).getNodeById(ownerId).addChild(newManager);
            nodeJPARepo.save(newManager);
        } finally {
            lock.unlock();
        }
    }

    public void changeAuthoToManager(int storeID, int ownerID, int managerId, List<Permission> per) throws UIException, DevException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeID)) {
                throw new DevException("store does not exist in superDS");
            }
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
            nodeJPARepo.save(toChange);

        } finally {
            lock.unlock();
        }
    }

    public void deleteManager(int storeID, int ownerID, int managerId) throws UIException, DevException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeID)) {
                throw new DevException("store does not exist in superDS");
            }
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
            NodeKey key = new NodeKey(storeID, managerId);
            nodeJPARepo.deleteById(key);
        } finally {
            lock.unlock();
        }
    }

    public boolean checkDeactivateStore(int storeId, int ownerId) throws DevException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeId)) {
                throw new DevException("store does not exist in superDS");
            }
            if (this.employees.get(storeId).getRoot().getMyId() != ownerId) {
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public List<Integer> getWorkersInStore(int storeId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeId)) {
                throw new Exception("store does not exist in superDS");
            }
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

    public Tree getWorkersTreeInStore(int storeId) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeId)) {
                throw new Exception("store does not exist in superDS");
            }
            return employees.get(storeId);
        } finally {
            lock.unlock();
        }
    }

    /* 
    public List<WorkerDTO> getWorkerDTOsInStore(int storeId) throws Exception {
    ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
    lock.lock();
    try {
        if (!employees.containsKey(storeId)) {
            throw new Exception("store does not exist in superDS");
        }
        Tree tree = employees.get(storeId);
        List<WorkerDTO> result = new ArrayList<>();
        Node root = tree.getRoot();
        int ownerId = root.getMyId();
        int parentId = root.getParentId();

    }*/
    @Transactional
    public void closeStore(int storeID) throws Exception {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeID, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeID)) {
                throw new Exception("store does not exist in superDS");
            }
            this.employees.remove(storeID);
            this.offers.remove(storeID);
            nodeJPARepo.deleteByStoreId(storeID);
            storeTreeJPARepo.deleteById(storeID);

            offerJPARepo.deleteByIdStoreId(storeID);
        } finally {
            lock.unlock();
        }
    }

    public Map<Integer, Tree> getEmployees() {
        return employees;
    }

    public boolean checkStoreExist(int storeId) {
        return employees.containsKey(storeId);
    }

    //make offer delete offer
    public void makeOffer(Offer offer, int storeId) throws Exception {
        // Check in DB for existing offer
        if (offerJPARepo.existsByIdSenderIdAndIdReceiverId(
                offer.getSenderId(), offer.getReceiverId())) {
            throw new Exception("Duplicate offer already exists");
        }

        synchronized (offers) {
            offers.computeIfAbsent(storeId, k -> new ArrayList<>()).add(offer);
            offerJPARepo.save(offer);
        }
    }

    @Transactional
    public List<Permission> deleteOffer(int storeId, int senderId, int reciverId) throws Exception {
        synchronized (offers) {
            List<Offer> Offer = offers.get(storeId);
            if (Offer == null) {
                throw new Exception("store offers is null");
            }

            Iterator<Offer> iterator = Offer.iterator();
            while (iterator.hasNext()) {
                Offer offer = iterator.next();
                if (offer.getSenderId() == senderId && offer.getReceiverId() == reciverId) {
                    List<Permission> permissions = offer.getPermissions();
                    iterator.remove();
                    offerJPARepo.delete(offer);

                    // Clean up empty list
                    if (Offer.isEmpty()) {
                        offers.remove(storeId);
                    }

                    return permissions;
                }
            }
        }

        return null; // offer not found
    }

    public Offer getOffer(int storeId, int senderId, int receiverId) throws Exception {
        List<Offer> storeOffers = offers.get(storeId);
        if (storeOffers == null) {
            throw new Exception("No offers found for store ID: " + storeId);
        }

        for (Offer offer : storeOffers) {
            if (offer.getSenderId() == senderId && offer.getReceiverId() == receiverId) {
                return offer;
            }
        }

        throw new Exception("No offer found from sender " + senderId + " to receiver " + receiverId + " in store " + storeId);
    }

    public List<Integer> getStoresIdForUser(int userId) {
        List<Integer> result = new ArrayList<>();

        for (Map.Entry<Integer, Tree> entry : employees.entrySet()) {
            int storeId = entry.getKey();
            Tree tree = entry.getValue();

            if (tree.getNodeById(userId) != null) {
                result.add(storeId);
            }
        }
        System.out.println("llllllllllll");
        System.out.println(result.size());
        return result;
    }

    public int removeUserAccordingly(int userId) throws Exception {
        boolean userFound = false;

        // Remove user from all trees
        for (Tree tree : employees.values()) {
            if (tree.getNodeById(userId) != null) {
                tree.deleteNode(userId);
                userFound = true;
            }
        }

        // Remove offers where user is sender or receiver
        for (List<Offer> offerList : offers.values()) {
            boolean removed = offerList.removeIf(
                    offer -> offer.getSenderId() == userId || offer.getReceiverId() == userId
            );
            if (removed) {
                userFound = true;
            }
        }

        if (!userFound) {
            return -1;
        }
        return userId;
    }

    public void clearData() {
        employees.clear();

        offers.clear();
        storeLocks.clear();
    }

    public List<Node> getAllWorkers(int storeId) throws UIException {
        ReentrantLock lock = storeLocks.computeIfAbsent(storeId, k -> new ReentrantLock());
        lock.lock();
        try {
            if (!employees.containsKey(storeId)) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            Tree tree = employees.get(storeId);
            List<Node> result = new ArrayList<>();
            TreeIterator iterator = new TreeIterator(tree.getRoot());
            while (iterator.hasNext()) {
                Node node = iterator.next();
                result.add(node);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public Permission[] getPermissions(Node node) {
        if (node.getIsManager() && node.getMyAuth() != null) {
            List<Permission> allowedPermissions = new ArrayList<>();
            for (Map.Entry<Permission, Boolean> entry : node.getMyAuth().getMyAutho().entrySet()) {
                if (entry.getValue()) {
                    allowedPermissions.add(entry.getKey());
                }
            }
            return allowedPermissions.toArray(new Permission[0]);
        } else {
            return null; // Owner have no permessions
        }
    }

    public Map<Integer, List<Offer>> getOffers() {
        return this.offers;
    }

}
