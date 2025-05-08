package workshop.demo.DomainLayer.Stock;

import java.util.List;

import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.Random;

public interface IStockRepo {

    int addProduct(String name, Category category, String description, String[] keywords) throws Exception; // Adds a
                                                                                                            // global
                                                                                                            // product
    // String removeProduct(int productID);

    Product findById(int productId) throws Exception;

    public ProductDTO[] getMatchesProducts(ProductSearchCriteria filter);

    public ProductDTO GetProductInfo(int productId);

    // auction
    public SingleBid bidOnAuction(int StoreId, int userId, int auctionId, double price)
            throws UIException, DevException;

    public int addAuctionToStore(int StoreId, int userId, int productId, int quantity, long tome, double startPrice)
            throws UIException, DevException;

    public AuctionDTO[] getAuctionsOnStore(int userId, int storeId) throws UIException, DevException;

    // bid
    public int addProductToBid(int storeId, int userid, int productId, int quantity) throws UIException, DevException;

    public SingleBid bidOnBid(int bidId, double price, int userId, int storeId) throws UIException, DevException;

    public BidDTO[] getAllBids(int userId, int storeId) throws UIException, DevException;

    public boolean rejectBid(int userId, int storeId, int bidId, int userBidId) throws Exception;

    public SingleBid acceptBid(int storeId, int bidId, int userId, int userBidId) throws UIException, DevException;

    // random
    public int addProductToRandom(int userId, int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws UIException, DevException;

    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid)
            throws UIException, DevException;

    public ParticipationInRandomDTO endRandom(int storeId, int userId, int randomId) throws Exception;

    public RandomDTO[] getRandomsInStore(int storeId, int userId) throws UIException, DevException;


    // public Random getRandomById(int randomId) throws UIException, DevException;

}
