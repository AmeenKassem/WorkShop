package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Stock.ActivePurcheses;

@Repository
public interface IActivePurchasesRepo extends JpaRepository<ActivePurcheses, Integer> {

}
