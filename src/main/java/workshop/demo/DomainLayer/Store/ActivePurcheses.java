package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class ActivePurcheses {

    private int storeId;

    private HashMap<Integer, BID> activeBid = new HashMap<>();
    private HashMap<Integer, Random> activeRandom = new HashMap<>();
    private HashMap<Integer, Auction> activeAuction = new HashMap<>();

    private static AtomicInteger bidIdGen = new AtomicInteger();
    private static AtomicInteger auctionIdGen = new AtomicInteger();
    private static AtomicInteger randomIdGen = new AtomicInteger();

    public ActivePurcheses(int storeId) {
        this.storeId = storeId;
    }

    // ========== Auction ==========

    public int addProductToAuction(int productId, int quantity, long time) throws UIException {
        if (quantity <= 0 || time <= 0)
            throw new UIException("Quantity and time must be positive!", ErrorCodes.INVALID_AUCTION_PARAMETERS);
        int id = auctionIdGen.incrementAndGet();
        Auction auction = new Auction(productId, quantity, time, id, storeId);
        activeAuction.put(id, auction);
        return id;
    }

    public SingleBid addUserBidToAuction(int auctionId, int userId, double price) throws DevException ,UIException {
        if (!activeAuction.containsKey(auctionId)) {
            throw new DevException("Auction ID not found in active auctions!");
        }
        return activeAuction.get(auctionId).bid(userId, price);
    }

    public AuctionDTO[] getAuctions() {
        AuctionDTO[] auctionDTOs = new AuctionDTO[activeAuction.size()];
        int i = 0;
        for (Auction auction : activeAuction.values()) {
            auctionDTOs[i] = auction.getDTO();
            i++;
        }
        return auctionDTOs;
    }

    // ========== BID ==========

    public int addProductToBid(int productId, int quantity) throws UIException {
        if (quantity <= 0)
            throw new UIException("Quantity must be positive!", ErrorCodes.INVALID_BID_PARAMETERS);
        int id = bidIdGen.incrementAndGet();
        BID bid = new BID(productId, quantity, id, storeId);
        activeBid.put(id, bid);
        return id;
    }

    public SingleBid addUserBidToBid(int bidId, int userId, double price) throws DevException ,UIException {
        if (!activeBid.containsKey(bidId)) {
            throw new DevException("Bid ID not found in active bids!");
        }
        return activeBid.get(bidId).bid(userId, price);
    }

    public BidDTO[] getBids() {
        BidDTO[] bidDTOs = new BidDTO[activeBid.size()];
        int i = 0;
        for (BID bid : activeBid.values()) {
            bidDTOs[i] = bid.getDTO();
            i++;
        }
        return bidDTOs;
    }

    public SingleBid acceptBid(int userBidId, int bidId) throws DevException ,UIException {
        if (!activeBid.containsKey(bidId)) {
            throw new DevException("Bid ID not found in active bids!");
        }
        return activeBid.get(bidId).acceptBid(userBidId);
    }

    public void rejectBid(int userBidId, int bidId) throws DevException ,UIException {
        if (!activeBid.containsKey(bidId)) {
            throw new DevException("Bid ID not found in active bids!");
        }
        activeBid.get(bidId).rejectBid(userBidId);
    }

    // ========== Random ==========

    public int addProductToRandom(int productId, int quantity, double productPrice, int storeId, long randomTime) throws UIException {
        if (quantity <= 0)
            throw new UIException("Quantity must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        if (productPrice <= 0)
            throw new UIException("Product price must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        if (randomTime <= 0)
            throw new UIException("Random time must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        int id = randomIdGen.incrementAndGet();
        Random random = new Random(productId, quantity, productPrice, id, storeId, randomTime);
        activeRandom.put(id, random);
        return id;
    }

    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, double productPrice) throws UIException {
        if (!activeRandom.containsKey(randomId))
            throw new UIException("Random ID not found!", ErrorCodes.RANDOM_NOT_FOUND);
        if (productPrice <= 0)
            throw new UIException("Product price must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        if (!activeRandom.get(randomId).isActive()) {
            activeRandom.remove(randomId);
            throw new UIException("Random has ended!", ErrorCodes.RANDOM_FINISHED);
        }
        return activeRandom.get(randomId).participateInRandom(userId, productPrice);
    }

    public ParticipationInRandomDTO endRandom(int randomId) throws DevException {
        if (!activeRandom.containsKey(randomId))
            throw new DevException("Random ID not found in active randoms!");
        return activeRandom.get(randomId).endRandom();
    }

    public RandomDTO[] getRandoms() {
        RandomDTO[] randomDTOs = new RandomDTO[activeRandom.size()];
        int i = 0;
        for (Random random : activeRandom.values()) {
            randomDTOs[i] = random.getDTO();
            i++;
        }
        return randomDTOs;
    }

    public Random getRandom(int randomId) throws DevException {
        if (!activeRandom.containsKey(randomId))
            throw new DevException("Random ID not found in active randoms!");
        return activeRandom.get(randomId);
    }

    public double getProductPrice(int randomId) throws DevException {
        if (!activeRandom.containsKey(randomId))
            throw new DevException("Random ID not found in active randoms!");
        return activeRandom.get(randomId).getProductPrice();
    }
}
