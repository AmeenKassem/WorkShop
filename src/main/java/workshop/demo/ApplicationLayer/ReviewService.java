package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.MessageDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

public class ReviewService {
    private IReviewRepo reviewRepo;
    private IAuthRepo authRepo;
    private IUserRepo userRepo;
    private IStoreRepo storeRepo;

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    public ReviewService(IReviewRepo reviewRepo, IAuthRepo authRepo, IUserRepo userRepo, IStoreRepo storeRepo) {
        this.authRepo = authRepo;
        this.reviewRepo = reviewRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        logger.info("created review service");
    }

    public void AddReviewToProduct(String token, int storeId, int productId, MessageDTO review) throws UIException {
        logger.info("about to add review to product: {} in store: {}", productId, storeId);
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        if (this.storeRepo.findStoreByID(storeId) == null) {
            throw new UIException("Store not found!", ErrorCodes.STORE_NOT_FOUND);
        }
        this.reviewRepo.AddReviewToProduct(storeId, productId, review);
        logger.info("added review successfully!");
    }

    public void AddReviewToStore(String token, int storeId, MessageDTO review) throws UIException {
        logger.info("about to add review to store: {}", storeId);
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        if (this.storeRepo.findStoreByID(storeId) == null) {
            throw new UIException("Store not found!", ErrorCodes.STORE_NOT_FOUND);
        }
        this.reviewRepo.AddReviewToStore(storeId, review);
        logger.info("added review successfully!");
    }

    public List<MessageDTO> getReviewsForStore(int storeId) throws UIException {
        if (this.storeRepo.findStoreByID(storeId) == null) {
            throw new UIException("Store not found!", ErrorCodes.STORE_NOT_FOUND);
        }
        return this.reviewRepo.getReviewsForStore(storeId);
    }

    public List<MessageDTO> getReviewsForProduct(int storeId, int productId) throws UIException {
        if (this.storeRepo.findStoreByID(storeId) == null) {
            throw new UIException("Store not found!", ErrorCodes.STORE_NOT_FOUND);
        }
        return this.reviewRepo.getReviewsForProduct(storeId, productId);
    }
}
