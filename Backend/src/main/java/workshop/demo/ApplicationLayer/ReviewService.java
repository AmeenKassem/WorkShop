package workshop.demo.ApplicationLayer;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Review.Review;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.IStockRepoDB;
import workshop.demo.DomainLayer.Stock.IStoreStockRepo;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.InfrastructureLayer.ReviewJpaRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionJpaRepository;

@Service
public class ReviewService {

    @Autowired
    private IAuthRepo authRepo;
    // @Autowired
    // private IStockRepo stockRepo;
    @Autowired
    private IStoreStockRepo storeStockRepo;
    @Autowired
    private IStoreRepoDB storeJpaRepo;
    @Autowired
    private UserSuspensionJpaRepository suspensionJpaRepo;
    @Autowired
    private ReviewJpaRepository reviewJpaRepo;

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private UIException storeNotFound() {
        return new UIException(" store does not exist.", ErrorCodes.STORE_NOT_FOUND);
    }

    @CacheEvict(value = "productReviews", key = "T(org.apache.commons.lang3.tuple.ImmutablePair).of(#storeId, #productId)")
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
        List<item> items = storeStockRepo.findItemsByStoreId(storeId);
        if (items == null || items.size() == 0) {
            logger.debug("[AddReviewToProduct] No items found for StoreId={}. Check the database or stock service!", storeId);
            throw new UIException("No products found for Store ID " + storeId, ErrorCodes.PRODUCT_NOT_FOUND);
        }

        List<Integer> availableProductIds = items.stream()
                .map(item -> item.getProductId())
                .toList();

        if (!availableProductIds.contains(productId)) {
            logger.error("[AddReviewToProduct] ProductId={} NOT found in StoreId={}. Available ids: {}",
                    productId, storeId, availableProductIds);
            throw new UIException("Product with ID " + productId + " not found in Store " + storeId,
                    ErrorCodes.PRODUCT_NOT_FOUND);
        }
        String username = authRepo.getUserName(token);
        //reviewRepo.AddReviewToProduct(storeId, productId, userId, username, review);
        Review newReview = new Review(authRepo.getUserId(token),
                authRepo.getUserName(token),
                review,
                storeId,
                productId);
        reviewJpaRepo.save(newReview);
        logger.info("Added review successfully!");
        return true;
    }

    @CacheEvict(value = "storeReviews", key = "#storeId")
    public boolean AddReviewToStore(String token, int storeId, String review) throws UIException {
        logger.info("about to add review to store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        //reviewRepo.AddReviewToStore(storeId, authRepo.getUserId(token), authRepo.getUserName(token), review);
        Review newReview = new Review(authRepo.getUserId(token), authRepo.getUserName(token), review, storeId, -1); // -1 productId → store review
        reviewJpaRepo.save(newReview);
        logger.info("added review successfully!");
        return true;
    }

    @Cacheable("storeReviews")
    public List<ReviewDTO> getReviewsForStore(int storeId) throws UIException {
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        List<Review> reviews = reviewJpaRepo.findByStoreId(storeId)
                .stream()
                .filter(r -> r.getProductId() == -1) // Only store reviews
                .toList();

        return reviews.stream()
                .map(ReviewDTO::new)
                .toList();
    }

    @Cacheable("productReviews")
    public List<ReviewDTO> getReviewsForProduct(int storeId, int productId) throws UIException {
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        List<Review> reviews = reviewJpaRepo.findByStoreIdAndProductId(storeId, productId);

        return reviews.stream()
                .map(ReviewDTO::new)
                .toList();
    }
}
