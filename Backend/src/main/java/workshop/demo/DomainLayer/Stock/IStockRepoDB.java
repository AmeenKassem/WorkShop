package workshop.demo.DomainLayer.Stock;

import org.springframework.data.jpa.repository.JpaRepository;


public interface IStockRepoDB extends JpaRepository<Product,Integer> {
    
}
