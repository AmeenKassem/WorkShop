package workshop.demo.InfrastructureLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Review.Review;

@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    List<Review> findByStoreId(int storeId);

    @Query("SELECT r FROM Review r WHERE r.storeId = :storeId AND r.productId = :productId")
    List<Review> findByStoreIdAndProductId(int storeId, int productId);
}
