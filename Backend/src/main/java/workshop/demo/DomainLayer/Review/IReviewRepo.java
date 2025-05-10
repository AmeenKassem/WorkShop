package workshop.demo.DomainLayer.Review;

import java.util.List;

// import workshop.demo.DTOs.MessageDTO;

public interface IReviewRepo {

    public void AddReviewToProduct(int storeId, int productId, String review);

    public void AddReviewToStore(int storeId, String review);

    //must add gettt
    public List<String> getReviewsForProduct(int storeId, int productId);

    public List<String> getReviewsForStore(int storeId);
}
