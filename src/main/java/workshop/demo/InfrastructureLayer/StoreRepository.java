    package workshop.demo.InfrastructureLayer;

        import java.util.Collections;
    import java.util.LinkedList;
    import java.util.List;
    import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Random;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.Store.item;

    public class StoreRepository implements IStoreRepo {

    private final List<Store> stores;

    // switch it when use database!!
    private static final AtomicInteger counterSId = new AtomicInteger(1);

        public static int generateId() {
            return counterSId.getAndIncrement();
        }

    public StoreRepository() {
        this.stores = Collections.synchronizedList(new LinkedList<>());
    }

    @Override
    public int addStoreToSystem(int bossID, String storeName, String Category) {
        int storeId = generateId();
        stores.add(new Store(storeId, storeName, Category));
        return storeId;
    }

    @Override
    public void deactivateStore(int storeId, int ownerId) throws Exception {
        try {
            if (findStoreByID(storeId) == null) {
                throw new Exception("can't deactivate store: store does not exist");
            }
            if (!findStoreByID(storeId).isActive()) {
                throw new Exception("can't deactivate an DEactivated store");
            }

            findStoreByID(storeId).setActive(false);

        
        }

    @Override
    public void closeStore(int storeId) throws Exception {
        try {
            stores.removeIf(store -> store.getStoreID() == storeId);
        } catch (Exception e) {
            throw e;

            }

        }

        @Override
        public Store findStoreByID(int ID) {
            for (Store store : this.stores) {
                if (store.getStoreID() == ID) {
                    return store;
                }
            }
            return null;
        }

    @Override
    public boolean StoreExistsByID(int ID) {
        for (Store store : this.stores) {
            if (store.getStoreID() == ID) {
                return true;
            }
        }
        return false;
    }

        @Override
        public List<StoreDTO> viewAllStores() {// here must check it view it with products??
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'viewAllStores'");
        }

        // stock managment:
        @Override
        public List<ItemStoreDTO> getProductsInStore(int storeId) throws UIException, DevException  {
            if (findStoreByID(storeId) == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }

            return findStoreByID(storeId).getProductsInStore();

        }

        @Override
        public item addItem(int storeId, int productId, int quantity, int price, Category category) throws UIException, DevException  {
            Store store = findStoreByID(storeId);
            if (store == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            item toAdd = new item(productId, quantity, price, category);
            store.addItem(toAdd);
            return toAdd;
        }

        @Override
        public void removeItem(int storeId, int productId) throws UIException, DevException  {
            Store store = findStoreByID(storeId);
            if (store == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            store.removeItem(productId);
        }

        @Override
        public void decreaseQtoBuy(int storeId, int productId, int quantity) throws UIException, DevException  {
            Store store = findStoreByID(storeId);
            if (store == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            store.decreaseQtoBuy(productId, quantity);

        }

        @Override
        public void updateQuantity(int storeId, int productId, int newQuantity) throws UIException, DevException  {
            Store store = findStoreByID(storeId);
            if (store == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            store.changeQuantity(productId, newQuantity);
        }

        @Override
        public void updatePrice(int storeId, int productId, int newPrice) throws UIException, DevException  {
            Store store = findStoreByID(storeId);
            if (store == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            store.updatePrice(productId, newPrice);
        }

    @Override
    public void rankProduct(int storeId, int productId, int newRank) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        store.rankProduct(productId, newRank);
    }

        public List<Store> getStores() {
            return stores;
        }

        public String getStoreNameById(int storeId) throws UIException {
            Store store = findStoreByID(storeId);
            if (store != null) {
                return store.getStoreName();
            } else {
                throw new UIException("Store not found for ID: " + storeId, ErrorCodes.STORE_NOT_FOUND);
            }
        }

        // RANK STORE:
        @Override
        public void rankStore(int storeId, int newRank) throws UIException, DevException  {
            Store store = findStoreByID(storeId);
            if (store == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            store.rankStore(newRank);
        }

        @Override
        public int getFinalRateInStore(int storeId) throws UIException, DevException  {
            Store store = findStoreByID(storeId);
            if (store == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            return store.getFinalRateInStore(storeId);

        }

        // ======================================
        @Override
        public SingleBid bidOnAuction(int StoreId, int userId, int auctionId, double price) throws UIException, DevException  {
            if (findStoreByID(StoreId) == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            return findStoreByID(StoreId).bidOnAuctionProduct(auctionId, userId, price);
        }

    @Override
    public int addAuctionToStore(int StoreId, int userId, int productId, int quantity, long tome, double startPrice)
            throws Exception {
        // if (findStoreByID(StoreId) == null) {
        //     throw new Exception("can't delete manager: store does not exist");
        // }
        // if (!manipulateItem(userId, StoreId, Permission.SpecialType)) {
        //     throw new UIException("you have no permession to add auction.");
        // }

            return findStoreByID(StoreId).addProductToAuction(userId, productId, quantity, startPrice, tome);

        }

    @Override
    public AuctionDTO[] getAuctionsOnStore(int storeId, int userId) throws Exception {
        return findStoreByID(storeId).getAllAuctions();
    }

    // =============Bid
    @Override
    public int addProductToBid(int storeId, int userId, int productId, int quantity) throws Exception {
        return findStoreByID(storeId).addProductToBid(userId, productId, quantity);
    }

        @Override
        public SingleBid bidOnBid(int bidId, double price, int userId, int storeId) throws UIException, DevException  {
            if (findStoreByID(storeId) == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }

            return findStoreByID(storeId).bidOnBid(bidId, userId, price);
        }

        @Override
        public BidDTO[] getAllBids(int userId, int storeId) throws UIException, DevException  {
            if (findStoreByID(storeId) == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            if (!manipulateItem(userId, storeId, Permission.SpecialType)) {
                throw new UIException("You have no permission to add auction", ErrorCodes.NO_PERMISSION);
            }

            return findStoreByID(storeId).getAllBids();
        }

        @Override
        public SingleBid acceptBid(int storeId, int bidId, int userId, int userBidId) throws UIException, DevException  {
            if (findStoreByID(storeId) == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            if (!manipulateItem(userId, storeId, Permission.SpecialType)) {
                throw new UIException("You have no permission to add auction", ErrorCodes.NO_PERMISSION);
            }

            return findStoreByID(storeId).acceptBid(bidId, userBidId);
        }
    @Override
    public SingleBid acceptBid(int storeId, int bidId, int userId, int userBidId) throws Exception {
        return findStoreByID(storeId).acceptBid(bidId, userBidId);
    }

        @Override
        public boolean rejectBid(int userId, int storeId, int bidId, int userBidId) throws UIException, DevException  {
            if (findStoreByID(storeId) == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            if (!manipulateItem(userId, storeId, Permission.SpecialType)) {
                throw new UIException("You have no permission to add auction", ErrorCodes.NO_PERMISSION);
            }
            return findStoreByID(storeId).rejectBid(bidId, userBidId);
        }
    @Override
    public boolean rejectBid(int userId, int storeId, int bidId, int userBidId) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("store does not exist.");
        }
        // if (!manipulateItem(userId, storeId, Permission.SpecialType)) {
        //     throw new UIException("you have no permession to see auctions info.");
        // } --. where?? it has been changed
        return findStoreByID(storeId).rejectBid(bidId, userBidId);
    }

    // ===================Random
    @Override
    public int addProductToRandom(int userId, int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws Exception {
        return findStoreByID(storeId).addProductToRandom(productId, quantity, productPrice, storeId, RandomTime);
    }

        @Override
        public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid)
                throws UIException, DevException  {
            if (findStoreByID(storeId) == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            return findStoreByID(storeId).participateInRandom(userId, randomId, amountPaid);
        }

    @Override
    public ParticipationInRandomDTO endRandom(int storeId, int userId, int randomId) throws Exception {
        return findStoreByID(storeId).end(randomId);
    }

    @Override
    public RandomDTO[] getRandomsInStore(int storeId, int userId) throws Exception {
        return findStoreByID(storeId).getRandoms();
    }

        // public double getPriceForCard(int storeId, int randomId) throws UIException, DevException  {
        // if (findStoreByID(storeId) == null) {
        // throw new Exception("can't delete manager: store does not exist.");
        // }
        // return findStoreByID(storeId).getCardPrice(randomId);
        // }
        public double getPriceForCard(int storeId, int randomId) throws UIException, DevException  {
            if (findStoreByID(storeId) == null) {
                throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
            }
            return findStoreByID(storeId).getProductPrice(randomId);
        }

        public double getProductPrice(int storeId, int randomId) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getProductPrice'");
        }

        @Override
        public ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria, ProductDTO[] matchesProducts) throws UIException, DevException  {

            List<ItemStoreDTO> itemList = new LinkedList<>();
            if (criteria.getStoreId() == -1) {// search in all stores
                for (Store store : stores) {
                    List<item> stock = store.getAllItemsInStock();
                    for (item item1 : stock) {
                        for (ProductDTO pro : matchesProducts) {
                            if (item1.getProductId() == pro.getProductId() && criteria.matchesForStore(item1)) {
                                ItemStoreDTO toAdd = new ItemStoreDTO(
                                        item1.getProductId(),
                                        item1.getQuantity(),
                                        item1.getPrice(),
                                        item1.getCategory(),
                                        item1.getFinalRank(),
                                        store.getStoreID());
                                itemList.add(toAdd);
                                break; // found matching product -> no need to check other products
                            }
                        }
                    }
                }

            } else {// search in a spicific store
                Store store = findStoreByID(criteria.getStoreId());
                if (store == null) {
                    throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
                }
                for (ProductDTO pro : matchesProducts) {
                    item item1 = store.getProductById(pro.getProductId());
                    if (item1 != null && criteria.matchesForStore(item1)) {
                        ItemStoreDTO toAdd = new ItemStoreDTO(item1.getProductId(), item1.getQuantity(), item1.getPrice(),
                                item1.getCategory(), item1.getFinalRank(), store.getStoreID());
                        itemList.add(toAdd);
                    }
                }

            }

            return itemList.toArray(new ItemStoreDTO[0]);
        }

        @Override
        public List<WorkerDTO> ViewRolesAndPermissions(int storeId) throws UIException, DevException  {
            List<WorkerDTO> toReturn = new LinkedList<>();

            return toReturn;
        }

        public Random getRandomById(int randomId) throws UIException, DevException  {
            for (Store store : stores) {
                try {
                    return store.getRandom(randomId);
                } catch (Exception e) {
                    // Ignore the exception
                }
            }
            throw new UIException("Random with ID " + randomId + " not found in any store.", ErrorCodes.RANDOM_NOT_FOUND);
        }

        @Override
        public boolean checkAvailability(List<ItemCartDTO> cartItems) {
            for (ItemCartDTO itemDTO : cartItems) {
                Store store = findStoreByID(itemDTO.storeId);
                if (store == null) {
                    return false;
                }
                item storeItem = store.getItemByProductId(itemDTO.productId);
                if (storeItem == null || storeItem.getQuantity() < itemDTO.quantity) {
                    return false;
                }
            }
            return true;
        }

    @Override
    public item getItemByStoreAndProductId(int storeId, int productId) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("Store with ID " + storeId + " not found.");
        }
        return store.getItemByProductId(productId);
    }

    @Override
    public void validateAndDecreaseStock(int storeId, int productId, int amount) throws Exception {
        item storeItem = getItemByStoreAndProductId(storeId, productId);
        if (storeItem == null || storeItem.getQuantity() < amount) {
            throw new Exception("unvaliable");
        }
        decreaseQtoBuy(storeId, productId, amount);
    }

    public double calculateTotalPrice(List<ReceiptProduct> items) {
        double total = 0;
        for (ReceiptProduct item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public ParticipationInRandomDTO validatedParticipation(int userId, int randomId, int storeId, double amountPaid) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store not found");
        }

        double requiredPrice = store.getProductPrice(randomId);
        if (amountPaid < requiredPrice) {
            throw new Exception("Insufficient payment");
        }
        return store.participateInRandom(userId, randomId, amountPaid);
    }

    public List<ReceiptProduct> processCartItemsForStore(int storeId, List<ItemCartDTO> cartItems, boolean isGuest) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("Store not found");
        }
        String storeName = store.getStoreName();
        return store.ProcessCartItems(cartItems, isGuest, storeName);
    }

    }
        