package workshop.demo.DomainLayer.Stock;

import java.util.List;

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
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;

public interface IStockRepo {

    int addProduct(String name, Category category, String description, String[] keywords) throws Exception; // Adds a
    // global
    // product

    Product findByIdInSystem(int productId) throws Exception;

    public ProductDTO[] getMatchesProducts(ProductSearchCriteria filter);

    public ProductDTO GetProductInfo(int productId) throws UIException;

    // auction
    public SingleBid bidOnAuction(int StoreId, int userId, int auctionId, double price)
            throws UIException, DevException;

    public int addAuctionToStore(int StoreId, int productId, int quantity, long tome, double startPrice)
            throws UIException, DevException;

    public AuctionDTO[] getAuctionsOnStore(int storeId) throws UIException, DevException;

    // bid
    public int addProductToBid(int storeId, int productId, int quantity) throws UIException, DevException;

    public SingleBid bidOnBid(int bidId, double price, int userId, int storeId) throws UIException, DevException;

    public BidDTO[] getAllBids(int storeId) throws UIException, DevException;

    public boolean rejectBid(int storeId, int bidId, int userBidId) throws Exception;

    public SingleBid acceptBid(int storeId, int bidId, int userBidId) throws UIException, DevException;

    // random
    public int addProductToRandom(int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws UIException, DevException;

    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid)
            throws UIException, DevException;

    public ParticipationInRandomDTO endRandom(int storeId, int randomId) throws Exception;

    public RandomDTO[] getRandomsInStore(int storeId) throws UIException, DevException;

    //stock management
    void addStore(int storeId);

     List<ItemStoreDTO> getProductsInStore(int storeId) throws UIException, DevException;
     

    item addItem(int storeId, int productId, int quantity, int price, Category category) throws UIException, DevException;

    void removeItem(int storeId, int productId) throws UIException, DevException;

    void decreaseQuantitytoBuy(int storeId, int productId, int quantity) throws UIException, DevException;

    void updateQuantity(int storeId, int productId, int newQuantity) throws UIException, DevException;

    void updatePrice(int storeId, int productId, int newPrice) throws UIException, DevException;

    void rankProduct(int storeId, int productId, int newRank) throws UIException, DevException;

    double getProductPrice(int storeId, int productId) throws UIException, DevException;

    item getItemByStoreAndProductId(int storeId, int productId) throws UIException, DevException;

    boolean checkAvailability(List<ItemCartDTO> cartItems);

    void validateAndDecreaseStock(int storeId, int productId, int amount) throws UIException, DevException;

    double calculateTotalPrice(List<ReceiptProduct> items);

    ParticipationInRandomDTO validatedParticipation(int userId, int randomId, int storeId, double amountPaid) throws UIException, DevException;

    List<ReceiptProduct> processCartItemsForStore(int storeId, List<ItemCartDTO> cartItems, boolean isGuest) throws Exception;

    ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria, ProductDTO[] matchesProducts) throws Exception;

void checkProductExists_ThrowException(int productId) throws UIException ; 
}
