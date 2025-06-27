package workshop.demo.ApplicationLayer;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LockManager {

    private final ConcurrentMap<Integer, Object> randomLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Object> auctionLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Object> bidLocks = new ConcurrentHashMap<>();

    public Object getRandomLock(int randomId) {
        return randomLocks.computeIfAbsent(randomId, id -> new Object());
    }

    public Object getAuctionLock(int auctionId) {
        return auctionLocks.computeIfAbsent(auctionId, id -> new Object());
    }

    public Object getBidLock(int bidId) {
        return bidLocks.computeIfAbsent(bidId, id -> new Object());
    }

    // Optional cleanup methods
    public void removeRandomLock(int randomId) {
        randomLocks.remove(randomId);
    }

    public void removeAuctionLock(int auctionId) {
        auctionLocks.remove(auctionId);
    }

    public void removeBidLock(int bidId) {
        bidLocks.remove(bidId);
    }
}

