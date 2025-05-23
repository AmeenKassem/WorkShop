package workshop.demo.ApplicationLayer;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

@Service
public class ReviewService {

    private IReviewRepo reviewRepo;
    private IAuthRepo authRepo;
    private IUserRepo userRepo;
    private IStoreRepo storeRepo;
    private IStockRepo stockRepo;

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    public ReviewService(IReviewRepo reviewRepo, IAuthRepo authRepo, IUserRepo userRepo, IStoreRepo storeRepo,IStockRepo stockRepo) {
        this.authRepo = authRepo;
        this.reviewRepo = reviewRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.stockRepo=stockRepo;
        logger.info("created review service");
    }

    public boolean AddReviewToProduct(String token, int storeId, int productId, String review) throws UIException, DevException {
    logger.info("About to add review to product: {} in store: {}", productId, storeId);

    // Check user and store
    authRepo.checkAuth_ThrowTimeOutException(token, logger);
    storeRepo.checkStoreExistance(storeId);

    ItemStoreDTO[] items = stockRepo.getProductsInStore(storeId);
    boolean found = Arrays.stream(items)
        .anyMatch(item -> item.getId() == productId);

    if (!found) {
        throw new UIException("Product with ID " + productId + " not found in store " + storeId, ErrorCodes.PRODUCT_NOT_FOUND);
    }

    int userId = authRepo.getUserId(token);
    String username = authRepo.getUserName(token);
    reviewRepo.AddReviewToProduct(storeId, productId, userId, username, review);

    logger.info("Added review successfully!");
    return true;
}


    public boolean AddReviewToStore(String token, int storeId, String review) throws UIException {
        logger.info("about to add review to store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        storeRepo.checkStoreExistance(storeId);
        reviewRepo.AddReviewToStore(storeId, authRepo.getUserId(token), authRepo.getUserName(token), review);
        logger.info("added review successfully!");
        return true;
    }

    public List<ReviewDTO> getReviewsForStore(int storeId) throws UIException {
        storeRepo.checkStoreExistance(storeId);
        return reviewRepo.getReviewsForStore(storeId);
    }

    public List<ReviewDTO> getReviewsForProduct(int storeId, int productId) throws UIException {
        storeRepo.checkStoreExistance(storeId);
        return reviewRepo.getReviewsForProduct(storeId, productId);
    }
}
