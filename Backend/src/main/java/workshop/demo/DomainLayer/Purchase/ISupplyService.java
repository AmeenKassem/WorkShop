package workshop.demo.DomainLayer.Purchase;
import workshop.demo.DTOs.SupplyDetails;

public interface ISupplyService {
    int processSupply(SupplyDetails supplyDetails) throws Exception;
}