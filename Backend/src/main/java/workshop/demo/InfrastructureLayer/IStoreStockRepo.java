package workshop.demo.InfrastructureLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.item;

public interface IStoreStockRepo extends JpaRepository<StoreStock, Integer> {

    @Query("SELECT i FROM StoreStock s JOIN s.items i WHERE i.productId = :productId")
    List<item> findItemsByProductId(@Param("productId") int productId);

    @Query("SELECT i FROM StoreStock s JOIN s.items i WHERE s.storeID = :storeId")
    List<item> findItemsByStoreId(@Param("storeId") int storeId);
}
