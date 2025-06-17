// package workshop.demo.InfrastructureLayer;

// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// import org.apache.commons.lang3.tuple.ImmutablePair;
// import org.apache.commons.lang3.tuple.Pair;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Repository;

// import workshop.demo.DTOs.ReviewDTO;
// import workshop.demo.DomainLayer.Review.IReviewRepo;
// import workshop.demo.DomainLayer.Review.Review;

// @Repository
// public class ReviewRepository implements IReviewRepo {

//     private Map<Integer, List<Review>> storeReviews;
//     private Map<Pair<Integer, Integer>, List<Review>> productReviews; //1.storeId 2.productId -> List of reviews

//     @Autowired
//     public ReviewRepository() {
//         this.storeReviews = new ConcurrentHashMap<>();
//         this.productReviews = new ConcurrentHashMap<>();
//     }

//     @Override
//     public void AddReviewToProduct(int storeId, int productId, int reviewerId, String name, String review) {
//         ImmutablePair<Integer, Integer> key = new ImmutablePair<>(storeId, productId);
//         Review toAdd = new Review(reviewerId, name, review, storeId, productId);
//         productReviews
//                 .computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
//                 .add(toAdd);//it's thread safe
//     }

//     @Override
//     public void AddReviewToStore(int storeId, int reviewerId, String name, String review) {
//         Review toAdd = new Review(reviewerId, name, review, storeId, -1);
//         storeReviews
//                 .computeIfAbsent(storeId, k -> Collections.synchronizedList(new ArrayList<>()))
//                 .add(toAdd); //it's thread safe
//     }

//     @Override
//     public List<ReviewDTO> getReviewsForProduct(int storeId, int productId) {
//         ImmutablePair<Integer, Integer> key = new ImmutablePair<>(storeId, productId);
//         List<Review> reviews = productReviews.getOrDefault(key, Collections.emptyList());
//         return reviews.stream().map(ReviewDTO::new).toList();
//     }

//     @Override
//     public List<ReviewDTO> getReviewsForStore(int storeId) {
//         List<Review> reviews = storeReviews.getOrDefault(storeId, Collections.emptyList());
//         return reviews.stream().map(ReviewDTO::new).toList();
//     }

//     public void clear() {
//         if (storeReviews != null) {
//             storeReviews.clear();
//         }
//         if (productReviews != null) {
//             productReviews.clear();
//         }
//     }

// }
