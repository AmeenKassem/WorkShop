package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Entity
public class ActivePurcheses {

    private static final Logger logger = LoggerFactory.getLogger(ActivePurcheses.class);

    @Id
    private int storeId;

    @OneToMany(mappedBy = "activePurcheses", cascade = CascadeType.ALL)
    @MapKey(name = "auctionId") // Use a field in Bid as the key
    private Map<Integer, Auction> activeAuction = new HashMap<>();


    @Transient
    private HashMap<Integer, BID> activeBid = new HashMap<>();


    @OneToMany(mappedBy = "activePurcheses", cascade = CascadeType.ALL)
    @MapKey(name = "randomId") 
    private Map<Integer, Random> activeRandom = new HashMap<>();

    @Transient
    private static AtomicInteger bidIdGen = new AtomicInteger();
    @Transient
    private static AtomicInteger randomIdGen = new AtomicInteger();

    @Transient
    private HashMap<Integer, List<BID>> productIdToBids = new HashMap<>();

    // private Map<Integer, List<Integer>> productIdToAuctions = new HashMap<>();
    @Transient
    private HashMap<Integer, List<Random>> productIdToRandoms = new HashMap<>();

    public ActivePurcheses(int storeId) {
        this.storeId = storeId;
    }

    public ActivePurcheses() {
    }

    // ========== Auction ==========

    public Auction addProductToAuction(int productId, int quantity, long time, double min) throws UIException {
        logger.debug("addProductToAuction called with productId={}, quantity={}, time={}", productId, quantity, time);

        if (quantity <= 0 || time <= 0) {
            logger.error("Invalid auction parameters: quantity={}, time={}", quantity, time);
            throw new UIException("Quantity and time must be positive!", ErrorCodes.INVALID_AUCTION_PARAMETERS);
        }
        // int id = auctionIdGen.incrementAndGet();
        Auction auction = new Auction();
        auction.setMaxBid(min);
        auction.setEndTimeMillis(System.currentTimeMillis() + time);
        auction.setProductId(productId);
        auction.setQuantity(quantity);
        // auction.setStoreId(storeId);
        auction.setActivePurchases(this);
        activeAuction.put(auction.getId(), auction);
        // productIdToAuctions.computeIfAbsent(productId, k -> new
        // ArrayList<>()).add(auction.getId());
        logger.debug("Auction created with id={}", auction.getId());

        return auction;
    }

    public UserAuctionBid addUserBidToAuction(int auctionId, int userId, double price)
            throws DevException, UIException {
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
            auction.endAuction();
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

    public Random addProductToRandom(int productId, int quantity, double productPrice, int storeId, long randomTime)
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
        //int id = randomIdGen.incrementAndGet();
        Random random = new Random(productId, quantity, productPrice, storeId, randomTime);
        random.setActivePurchases(this);
        activeRandom.put(random.getRandomId(), random);
        productIdToRandoms.computeIfAbsent(productId, k -> new ArrayList<>()).add(random);
        logger.debug("Random created with id={}", random.getRandomId());

        return random;
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
        return activeRandom.get(randomId).participateInRandom(userId, productPrice).toDTO();
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

    public ParticipationInRandomDTO getRandomCardforuser(int specialId, int userId) {
        if (activeRandom.containsKey(specialId)) {
            Random random = activeRandom.get(specialId);
            return random.getCard(userId).toDTO();
        } else
            return null;
    }

    public SingleBid getBidIfWinner(int specialId, int bidId, SpecialType type) {
        if (type == SpecialType.Auction) {
            if (activeAuction.containsKey(specialId)) {
                var auction = activeAuction.get(specialId);
                SingleBid bid = null;
                if (bid != null && bid.isWinner()) {
                    return new SingleBid();
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
                return null;
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
            return random.getCard(cardId).toDTO();
        }
        return null;
    }

    public int getProductIdForSpecial(int specialId, SpecialType type) {
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

    public List<RandomDTO> getRandomsForProduct(int productId, String storeName, String productName) {

        List<RandomDTO> result = new ArrayList<>();
        List<Random> randoms = productIdToRandoms.getOrDefault(productId, new ArrayList<>());
        for (Random random : randoms) {
            RandomDTO dto = random.getDTO();
            dto.storeName = storeName;
            dto.productName = productName;
            result.add(dto);
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

    public List<AuctionDTO> getAuctionsForProduct(int productId, String storeName, String productName) {
        List<AuctionDTO> result = new ArrayList<>();
        // List<Integer> auctions = productIdToAuctions.getOrDefault(productId, new
        // ArrayList<>());
        for (Auction auction : activeAuction.values()) {
            auction.endAuction();
            if (auction.getProductId() == productId &&
                    !auction.isEnded()) {
                AuctionDTO auctionDto = auction.getDTO();
                auctionDto.storeName = storeName;
                auctionDto.productName = productName;
                result.add(auctionDto);
            }
        }
        return result;
    }

    public void clear() {
        activeBid.clear();
        activeRandom.clear();
        activeAuction.clear();

        // Optionally reset static ID generators if needed
        bidIdGen.set(0);
        // auctionIdGen.set(0);
        randomIdGen.set(0);
    }

    public Auction getAuctionById(int res) {
        return activeAuction.get(res);
    }

    public Integer getStoreId() {
        return storeId;
    }

    public ParticipationInRandomDTO getRandomCard(int storeId2, int specialId, int userId) throws DevException {
       return getRandom(specialId).getCard(userId).toDTO();
    }

    public SingleBid getBid(int storeId2, int specialId, int bidId, SpecialType type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBid'");
    }

    public void endAuction(int randomId) {
        getAuctionById(randomId).endAuction();
    }

    public List<Integer> getParticpationsOnAuction(int auctionId) {
        return activeAuction.get(auctionId).getBidsUsersIds();
    }

    public boolean auctionHasNoWinner(int auctionId) {
        return activeAuction.get(auctionId).mustReturnToStock();
    }

    public int getCurrAuctionTop(int auctionId) {
        return activeAuction.get(auctionId).getTopId();
    }

    public List<Auction> getActiveAuctions() {
        List<Auction> res = new ArrayList<>();
        for (Auction iterable_element : activeAuction.values()) {
            if (!iterable_element.isEnded())
                res.add(iterable_element);
        }
        return res;
    }

    public List<Random> getActiveRandoms() {
        List<Random> res = new ArrayList<>();
        for (Random iterable_element : activeRandom.values()) {
            if (iterable_element.isActive())
                res.add(iterable_element);
        }
        return res;
    }

}
