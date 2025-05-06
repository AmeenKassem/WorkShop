package workshop.demo.DomainLayer.Purchase;
import workshop.demo.DTOs.SupplyDetails;

public interface ISupplyService {
    boolean processSupply(SupplyDetails supplyDetails) throws Exception;
}