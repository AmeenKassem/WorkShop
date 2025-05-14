package workshop.demo.ApplicationLayer;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import workshop.demo.DTOs.MessageDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
@Service
public class ReviewService {
    private IReviewRepo reviewRepo;
    private IAuthRepo authRepo;
    private IUserRepo userRepo;
    private IStoreRepo storeRepo;

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    public ReviewService(IReviewRepo reviewRepo, IAuthRepo authRepo, IUserRepo userRepo, IStoreRepo storeRepo) {
        this.authRepo = authRepo;
        this.reviewRepo = reviewRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        logger.info("created review service");
    }

    public boolean AddReviewToProduct(String token, int storeId, int productId, String review) throws UIException {
        logger.info("about to add review to product: {} in store: {}", productId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        storeRepo.checkStoreExistance(storeId);
        reviewRepo.AddReviewToProduct(storeId, productId, review);
        logger.info("added review successfully!");
        return true;
    }

    public boolean AddReviewToStore(String token, int storeId, String review) throws UIException {
        logger.info("about to add review to store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        storeRepo.checkStoreExistance(storeId);
        reviewRepo.AddReviewToStore(storeId, review);
        logger.info("added review successfully!");
        return true;
    }

    public List<String> getReviewsForStore(int storeId) throws UIException {
        storeRepo.checkStoreExistance(storeId);
        return reviewRepo.getReviewsForStore(storeId);
    }

    public List<String> getReviewsForProduct(int storeId, int productId) throws UIException {
        storeRepo.checkStoreExistance(storeId);
        return reviewRepo.getReviewsForProduct(storeId, productId);
    }
}
