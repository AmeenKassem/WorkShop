package workshop.demo.DomainLayer.Stock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IActivePurchasesRepo extends JpaRepository<ActivePurcheses,Integer> {
    
}
