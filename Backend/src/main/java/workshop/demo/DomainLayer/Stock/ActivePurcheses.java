package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.Random;

public class ActivePurcheses {

    private static final Logger logger = LoggerFactory.getLogger(ActivePurcheses.class);

    private int storeId;

    private HashMap<Integer, BID> activeBid = new HashMap<>(); 
    private HashMap<Integer, Random> activeRandom = new HashMap<>();
    private HashMap<Integer, Auction> activeAuction = new HashMap<>();

    private static AtomicInteger bidIdGen = new AtomicInteger();
    private static AtomicInteger auctionIdGen = new AtomicInteger();
    private static AtomicInteger randomIdGen = new AtomicInteger();

    private HashMap<Integer, List<BID>> productIdToBids = new HashMap<>(); 
    private HashMap<Integer, List<Auction>> productIdToAuctions = new HashMap<>();
    private HashMap<Integer, List<Random>> productIdToRandoms = new HashMap<>();



    public ActivePurcheses(int storeId) {
        this.storeId = storeId;
    }


    // ========== Auction ==========

    public int addProductToAuction(int productId, int quantity, long time) throws UIException {
        logger.debug("addProductToAuction called with productId={}, quantity={}, time={}", productId, quantity, time);

        if (quantity <= 0 || time <= 0) {
            logger.error("Invalid auction parameters: quantity={}, time={}", quantity, time);
            throw new UIException("Quantity and time must be positive!", ErrorCodes.INVALID_AUCTION_PARAMETERS);
        }
        int id = auctionIdGen.incrementAndGet();
        Auction auction = new Auction(productId, quantity, time, id, storeId);
        activeAuction.put(id, auction);
        productIdToAuctions.computeIfAbsent(productId, k -> new ArrayList<>()).add(auction);
        logger.debug("Auction created with id={}", id);


        return id;
    }

    public SingleBid addUserBidToAuction(int auctionId, int userId, double price) throws DevException, UIException {
        logger.debug("addUserBidToAuction called with auctionId={}, userId={}, price={}", auctionId, userId, price);

        if (!activeAuction.containsKey(auctionId)) {
            logger.error("Auction ID {} not found", auctionId);

            throw new DevException("Auction ID not found in active auctions!");
        }
        return activeAuction.get(auctionId).bid(userId, price);
    }

    public AuctionDTO[] getAuctions() {
        logger.debug("getAuctions called");

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
        logger.debug("addProductToBid called with productId={}, quantity={}", productId, quantity);

        if (quantity <= 0)
            throw new UIException("Quantity must be positive!", ErrorCodes.INVALID_BID_PARAMETERS);
        int id = bidIdGen.incrementAndGet();
        BID bid = new BID(productId, quantity, id, storeId);
        activeBid.put(id, bid);
        productIdToBids.computeIfAbsent(productId, k -> new ArrayList<>()).add(bid);
        logger.debug("Bid created with id={}", id);

        return id;
    }

    public SingleBid addUserBidToBid(int bidId, int userId, double price) throws DevException, UIException {
        logger.debug("addUserBidToBid called with bidId={}, userId={}, price={}", bidId, userId, price);

        if (!activeBid.containsKey(bidId)) {
            logger.error("Bid ID {} not found", bidId);

            throw new DevException("Bid ID not found in active bids!");
        }
        return activeBid.get(bidId).bid(userId, price);
    }

    public BidDTO[] getBids() {
        logger.debug("getBids called");

        BidDTO[] bidDTOs = new BidDTO[activeBid.size()];
        int i = 0;
        for (BID bid : activeBid.values()) {
            bidDTOs[i] = bid.getDTO();
            i++;
        }
        return bidDTOs;
    }

    public SingleBid acceptBid(int userBidId, int bidId) throws DevException, UIException {
        logger.debug("acceptBid called with userBidId={}, bidId={}", userBidId, bidId);

        if (!activeBid.containsKey(bidId)) {
            logger.error("Bid ID {} not found", bidId);

            throw new DevException("Bid ID not found in active bids!");
        }
        return activeBid.get(bidId).acceptBid(userBidId);
    }

    public boolean rejectBid(int userBidId, int bidId) throws DevException, UIException {
        logger.debug("rejectBid called with userBidId={}, bidId={}", userBidId, bidId);

        if (!activeBid.containsKey(bidId)) {
            logger.error("Bid ID {} not found", bidId);

            throw new DevException("Bid ID not found in active bids!");
        }
        return activeBid.get(bidId).rejectBid(userBidId);
    }

    // ========== Random ==========

    public int addProductToRandom(int productId, int quantity, double productPrice, int storeId, long randomTime)
            throws UIException {
        logger.debug("addProductToRandom called with productId={}, quantity={}, price={}, randomTime={}", productId,
                quantity, productPrice, randomTime);

        if (quantity <= 0) {
            logger.error("Invalid random quantity: {}", quantity);

            throw new UIException("Quantity must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        }
        if (productPrice <= 0) {
            logger.error("Invalid random product price: {}", productPrice);

            throw new UIException("Product price must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        }
        if (randomTime <= 0) {
            logger.error("Invalid random time: {}", randomTime);

            throw new UIException("Random time must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        }
        int id = randomIdGen.incrementAndGet();
        Random random = new Random(productId, quantity, productPrice, id, storeId, randomTime);
        activeRandom.put(id, random);
        
        logger.debug("Random created with id={}", id);

        return id;
    }

    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, double productPrice)
            throws UIException {
        logger.debug("participateInRandom: userId={}, randomId={}, price={}", userId, randomId, productPrice);

        if (!activeRandom.containsKey(randomId)) {
            logger.error("Random ID {} not found", randomId);

            throw new UIException("Random ID not found!", ErrorCodes.RANDOM_NOT_FOUND);
        }
        if (productPrice <= 0) {
            logger.error("Invalid product price in random: {}", productPrice);

            throw new UIException("Product price must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        }
        if (!activeRandom.get(randomId).isActive()) {
            logger.warn("Random ID {} is not active anymore", randomId);

            activeRandom.remove(randomId);
            throw new UIException("Random has ended!", ErrorCodes.RANDOM_FINISHED);
        }
        return activeRandom.get(randomId).participateInRandom(userId, productPrice);
    }

    public ParticipationInRandomDTO endRandom(int randomId) throws DevException {
        logger.debug("endRandom: randomId={}", randomId);

        if (!activeRandom.containsKey(randomId)) {
            logger.error("Random ID {} not found on endRandom", randomId);

            throw new DevException("Random ID not found in active randoms!");
        }
        return activeRandom.get(randomId).endRandom();
    }

    public RandomDTO[] getRandoms() {
        logger.debug("getRandoms called");

        RandomDTO[] randomDTOs = new RandomDTO[activeRandom.size()];
        int i = 0;
        for (Random random : activeRandom.values()) {
            randomDTOs[i] = random.getDTO();
            i++;
        }
        return randomDTOs;
    }

    public Random getRandom(int randomId) throws DevException {
        logger.debug("getRandom: randomId={}", randomId);

        if (!activeRandom.containsKey(randomId))

        {
            logger.error("Random ID {} not found on getRandom", randomId);
            throw new DevException("Random ID not found in active randoms!");
        }
        return activeRandom.get(randomId);
    }

    public double getProductPrice(int randomId) throws DevException {
        logger.debug("getProductPrice: randomId={}", randomId);

        if (!activeRandom.containsKey(randomId)) {
            logger.error("Random ID {} not found on getProductPrice", randomId);
            throw new DevException("Random ID not found in active randoms!");
        }
        return activeRandom.get(randomId).getProductPrice();
    }

   

    public ParticipationInRandomDTO getRandomCardIfWinner(int specialId, int userId) {
        if (activeRandom.containsKey(specialId)) {
            Random random = activeRandom.get(specialId);
            if (random.userIsWinner(userId))
                return random.getWinner();
        }
        return null;
    }

    public SingleBid getBidIfWinner(int specialId, int bidId, SpecialType type) {
    if (type == SpecialType.Auction) {
        if (activeAuction.containsKey(specialId)) {
            var auction = activeAuction.get(specialId);
            SingleBid bid = auction.getBid(bidId);
            if (bid != null && bid.isWinner()) {
                return bid;
            }
        }
    } else { // BID
        if (activeBid.containsKey(specialId)) {
            var bidContainer = activeBid.get(specialId);
            SingleBid bid = bidContainer.getBid(bidId);
            if (bid != null && bid.isAccepted()) {
                return bid;
            }
        }
    }
    return null;
}


    public SingleBid getBidWithId(int specialId, int bidId, SpecialType type) {
        if (type == SpecialType.Auction) {
            if (activeAuction.containsKey(specialId)) {
                return activeAuction.get(specialId).getBid(bidId);
            }
        } else {
            if (activeBid.containsKey(specialId)) {
                return activeBid.get(specialId).getBid(bidId);
            }
        }
        return null;
    }

    public ParticipationInRandomDTO getCardWithId(int specialId, int cardId) {
        if (activeRandom.containsKey(specialId)) {
            Random random = activeRandom.get(specialId);
            // if (random.userIsWinner())
            return random.getCard(cardId);
        }
        return null;
    }

    public int getProductIdForSpecial(int specialId,SpecialType type){
        switch (type) {
            case Auction:
                return activeAuction.get(specialId).getProductId();
            case BID:
                return activeBid.get(specialId).getProductId();
            case Random:
                return activeRandom.get(specialId).getProductId();
            default:
                return -1;
        }
    }

    public List<RandomDTO> getRandomsForProduct(int productId) {
        List<RandomDTO> result = new ArrayList<>();
        List<Random> randoms = productIdToRandoms.getOrDefault(productId, new ArrayList<>());
        for (Random random : randoms) {
            result.add(random.getDTO());
        }
        return result;
    }

    public List<BidDTO> getBidsForProduct(int productId) {
        List<BidDTO> result = new ArrayList<>();
        List<BID> bids = productIdToBids.getOrDefault(productId, new ArrayList<>());
        for (BID bid : bids) {
            result.add(bid.getDTO());
        }
        return result;
    }

    public List<AuctionDTO> getAuctionsForProduct(int productId) {
        List<AuctionDTO> result = new ArrayList<>();
        List<Auction> auctions = productIdToAuctions.getOrDefault(productId, new ArrayList<>());
        for (Auction auction : auctions) {
            result.add(auction.getDTO());
        }
        return result;
    }

 
    public void clear() {
    activeBid.clear();
    activeRandom.clear();
    activeAuction.clear();

    // Optionally reset static ID generators if needed
    bidIdGen.set(0);
    auctionIdGen.set(0);
    randomIdGen.set(0);
}



}
