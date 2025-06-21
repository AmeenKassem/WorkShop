package workshop.demo.ApplicationLayer;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.DataAccessLayer.UserSuspensionJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
// import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

@Service
public class ReviewService {

    private IReviewRepo reviewRepo;
    private IAuthRepo authRepo;
    private IStoreRepo storeRepo;
    private IStockRepo stockRepo;
    private IStoreRepoDB storeJpaRepo;
    private UserSuspensionJpaRepository suspensionJpaRepo;

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    public ReviewService(IReviewRepo reviewRepo, IAuthRepo authRepo, IStoreRepo storeRepo, IStockRepo stockRepo,
            IStoreRepoDB storeJpaRepo , UserSuspensionJpaRepository suspensionJpaRepo) {
        this.authRepo = authRepo;
        this.reviewRepo = reviewRepo;
        this.storeRepo = storeRepo;
        // this.userRepo = userRepo;
        this.stockRepo = stockRepo;
        this.storeJpaRepo = storeJpaRepo;
        this.suspensionJpaRepo = suspensionJpaRepo;
        logger.info("created review service");
    }

    private UIException storeNotFound() {
        return new UIException(" store does not exist.", ErrorCodes.STORE_NOT_FOUND);
    }

    public boolean AddReviewToProduct(String token, int storeId, int productId, String review)
            throws UIException, DevException {
        logger.info("About to add review to product: {} in store: {}", productId, storeId);

        // Check user and store
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        int userId = authRepo.getUserId(token);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        ItemStoreDTO[] items = stockRepo.getProductsInStore(storeId);
        boolean found = Arrays.stream(items)
                .anyMatch(item -> item.getProductId() == productId);

        if (!found) {
            throw new UIException("Product with ID " + productId + " not found in store " + storeId,
                    ErrorCodes.PRODUCT_NOT_FOUND);
        }
        String username = authRepo.getUserName(token);
        reviewRepo.AddReviewToProduct(storeId, productId, userId, username, review);

        logger.info("Added review successfully!");
        return true;
    }

    public boolean AddReviewToStore(String token, int storeId, String review) throws UIException {
        logger.info("about to add review to store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        int userId = authRepo.getUserId(token);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        reviewRepo.AddReviewToStore(storeId, authRepo.getUserId(token), authRepo.getUserName(token), review);
        logger.info("added review successfully!");
        return true;
    }

    public List<ReviewDTO> getReviewsForStore(int storeId) throws UIException {
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        return reviewRepo.getReviewsForStore(storeId);
    }

    public List<ReviewDTO> getReviewsForProduct(int storeId, int productId) throws UIException {
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        return reviewRepo.getReviewsForProduct(storeId, productId);
    }
}
