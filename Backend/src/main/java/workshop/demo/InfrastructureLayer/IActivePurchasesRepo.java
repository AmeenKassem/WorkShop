package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;

import workshop.demo.DomainLayer.Stock.ActivePurcheses;

public interface IActivePurchasesRepo extends JpaRepository<ActivePurcheses, Integer> {

}
