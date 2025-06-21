package workshop.demo.DomainLayer.Stock;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IStoreStockRepo extends JpaRepository<StoreStock , Integer> {
    @Query("SELECT i FROM StoreStock s JOIN s.items i WHERE i.productId = :productId")
    List<item> findItemsByProductId(@Param("productId") int productId);
}

