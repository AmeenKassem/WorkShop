package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.MessageDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

public class ReviewService {

    //public void AddReviewToProduct(String token, int storeId, int productId, String review);
    //to store also 
    private IReviewRepo reviewRepo;
    private IAuthRepo authRepo;
    private IUserRepo userRepo;
    private IStoreRepo storeRepo;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public ReviewService(IReviewRepo reviewRepo, IAuthRepo authRepo, IUserRepo userRepo, IStoreRepo storeRepo) {
        this.authRepo = authRepo;
        this.reviewRepo = reviewRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        logger.info("created review service");
    }

    public void AddReviewToProduct(String token, int storeId, int productId, MessageDTO review) throws Exception {
        logger.info("about to add review to product: {} in store: {}", productId, storeId);
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            //who can make the review, the guest or user or both?????
            if (this.storeRepo.findStoreByID(storeId) == null) {
                throw new Exception("store not found!");
            }
            //also must check if this item exists in this store -> tomorrow!
            this.reviewRepo.AddReviewToProduct(storeId, productId, review);
            logger.info("added review succesfully!");
        } catch (Exception e) {
            logger.error(e.getMessage());

        }
    }

    public void AddReviewToStore(String token, int storeId, MessageDTO review) throws Exception {
        logger.info("about to add review to store: {}", storeId);
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            //who can make the review, the guest or user or both?????
            if (this.storeRepo.findStoreByID(storeId) == null) {
                throw new Exception("store not found!");
            }
            //also must check if this item exists in this store -> tomorrow!
            this.reviewRepo.AddReviewToStore(storeId, review);
            logger.info("added review succesfully!");
        } catch (Exception e) {
            logger.error(e.getMessage());

        }

    }

    public List<MessageDTO> getReviewsForStore(int storeId) throws Exception {
        if (this.storeRepo.findStoreByID(storeId) == null) {
            throw new Exception("store not found!");
        }
        return this.reviewRepo.getReviewsForStore(storeId);
    }

    public List<MessageDTO> getReviewsForProduct(int storeId, int productId) throws Exception {
        if (this.storeRepo.findStoreByID(storeId) == null) {
            throw new Exception("store not found!");
        }
        return this.reviewRepo.getReviewsForProduct(storeId, productId);

    }
}
