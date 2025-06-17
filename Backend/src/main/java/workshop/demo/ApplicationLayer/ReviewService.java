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
import workshop.demo.DataAccessLayer.ReviewJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Review.Review;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.Store.Store;

@Service
public class ReviewService {

    // @Autowired
    // private IReviewRepo reviewRepo;
    @Autowired
    private IAuthRepo authRepo;
    @Autowired
    private IStoreRepo storeRepo;
    @Autowired
    private IStockRepo stockRepo;
    @Autowired
    private IStoreRepoDB storeJpaRepo;
    @Autowired
    private ReviewJpaRepository reviewJpaRepo;

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    // @Autowired
    // public ReviewService(IReviewRepo reviewRepo, IAuthRepo authRepo, IStoreRepo storeRepo, IStockRepo stockRepo,
    //         IStoreRepoDB storeJpaRepo) {
    //     this.authRepo = authRepo;
    //     this.reviewRepo = reviewRepo;
    //     this.storeRepo = storeRepo;
    //     // this.userRepo = userRepo;
    //     this.stockRepo = stockRepo;
    //     this.storeJpaRepo = storeJpaRepo;
    //     logger.info("created review service");
    // }
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

        ItemStoreDTO[] items = stockRepo.getProductsInStore(storeId);
        boolean found = Arrays.stream(items)
                .anyMatch(item -> item.getProductId() == productId);

        if (!found) {
            throw new UIException("Product with ID " + productId + " not found in store " + storeId,
                    ErrorCodes.PRODUCT_NOT_FOUND);
        }

        int userId = authRepo.getUserId(token);
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
