package workshop.demo.DomainLayer.Review;

import java.util.List;

import workshop.demo.DTOs.ReviewDTO;

// import workshop.demo.DTOs.MessageDTO;
public interface IReviewRepo {

    public void AddReviewToProduct(int storeId, int productId, int reviewerId, String name, String review);

    public void AddReviewToStore(int storeId, int reviewerId, String name, String review);

    //must add gettt
    public List<ReviewDTO> getReviewsForProduct(int storeId, int productId);

    public List<ReviewDTO> getReviewsForStore(int storeId);
}
