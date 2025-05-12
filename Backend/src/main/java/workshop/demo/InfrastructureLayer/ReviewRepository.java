package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Review.IReviewRepo;

@Repository
public class ReviewRepository implements IReviewRepo {

    private Map<Integer, List<String>> storeReviews;
    private Map<Pair<Integer, Integer>, List<String>> productReviews; //1.storeId 2.productId -> List of reviews

    @Autowired
    public ReviewRepository() {
        this.storeReviews = new ConcurrentHashMap<>();
        this.productReviews = new ConcurrentHashMap<>();
    }

    @Override
    public void AddReviewToProduct(int storeId, int productId, String review) {
        ImmutablePair<Integer, Integer> key = new ImmutablePair<>(storeId, productId);
        productReviews
                .computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(review);//it's thread safe
    }

    @Override
    public void AddReviewToStore(int storeId, String review) {
        storeReviews
                .computeIfAbsent(storeId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(review); //it's thread safe
    }

    @Override
    public List<String> getReviewsForProduct(int storeId, int productId) {
        ImmutablePair<Integer, Integer> key = new ImmutablePair<>(storeId, productId);
        return productReviews.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public List<String> getReviewsForStore(int storeId) {
        return storeReviews.getOrDefault(storeId, Collections.emptyList());
    }

}
