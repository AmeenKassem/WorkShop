package workshop.demo.DomainLayer.Review;

import java.util.List;

import workshop.demo.DTOs.MessageDTO;

public interface IReviewRepo {

    public void AddReviewToProduct(int storeId, int productId, MessageDTO review);

    public void AddReviewToStore(int storeId, MessageDTO review);

    //must add gettt
    public List<MessageDTO> getReviewsForProduct(int storeId, int productId);

    public List<MessageDTO> getReviewsForStore(int storeId);
}
