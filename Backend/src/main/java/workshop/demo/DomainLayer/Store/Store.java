package workshop.demo.DomainLayer.Store;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.OfferDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class Store {

    private static final Logger logger = LoggerFactory.getLogger(Store.class);

    private int storeID;
    private String storeName;
    private String category;
    private boolean active;
    private AtomicInteger[] rank;//rank[x] is the number of people who ranked i+1
    private List<OfferDTO> offers;
    //must add something for messages
    private List<String> messgesInStore;

    public Store(int storeID, String storeName, String category) {
        logger.debug("Creating store: ID={}, Name={}, Category={}", storeID, storeName, category);

        this.storeID = storeID;
        this.storeName = storeName;
        this.category = category;
        this.active = true;
        this.rank = new AtomicInteger[5];
        for (int i = 0; i < 5; i++) {
            rank[i] = new AtomicInteger(0);
        }
        this.messgesInStore = Collections.synchronizedList(new LinkedList<>());
        this.offers = Collections.synchronizedList(new LinkedList<>());
    }

    public int getStoreID() {
        return storeID;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void setActive(boolean active) {
        logger.debug("Setting active status for store {} to {}", storeID, active);

        this.active = active;
    }

    // rank store:
    public boolean rankStore(int i) {
        if (i < 1 || i > 5) {
            logger.error("Invalid store rank: {}", i);

            return false;
        }
        rank[i - 1].incrementAndGet();
        logger.debug("Ranking store {} with {}", storeID, i);

        return true;
    }

    public int getFinalRateInStore(int storeId) {
        logger.debug("Calculating final rank for store {}", storeId);

        int totalVotes = 0;
        int WRank = 0;
        for (int i = 0; i < rank.length; i++) {
            int count = rank[i].get(); // votes for rank (i+1)
            totalVotes += count;
            WRank += (i + 1) * count;
        }
        if (totalVotes == 0) {
            return 3;// defult rank

        }
        int avgRank = (int) Math.round((double) WRank / totalVotes);
        return Math.max(1, Math.min(5, avgRank));// to make surre the result is between 1 and 5

    }

    public StoreDTO getStoreDTO() {
        return new StoreDTO(storeID, storeName, category, active, getFinalRateInStore(storeID));
    }

    public void makeOffer(int senderId, int receiverId, boolean toBeOwner, List<Permission> per) {
        OfferDTO newOffer = new OfferDTO(senderId, receiverId, toBeOwner, per);
        offers.add(newOffer);
    }

    public List<Permission> deleteOffer(int senderId, int receiverId) {
        synchronized (offers) {
            Iterator<OfferDTO> iterator = offers.iterator();
            while (iterator.hasNext()) {
                OfferDTO offer = iterator.next();
                if (offer.getSenderId() == senderId && offer.getReceiverId() == receiverId) {
                    List<Permission> permissions = offer.getPermissions();
                    iterator.remove();
                    return permissions;
                }
            }

        }
        return null; // no matching offer found
    }

}
