package workshop.demo.DomainLayer.Stock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IStoreStockRepo extends JpaRepository<StoreStock , Integer> {
    
}
