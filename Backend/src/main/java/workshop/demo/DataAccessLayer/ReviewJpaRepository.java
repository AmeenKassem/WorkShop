package workshop.demo.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Review.Review;

@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    List<Review> findByStoreId(int storeId);

    List<Review> findByStoreIdAndProductId(int storeId, int productId);
}
