package workshop.demo.DomainLayer.Purchase;

import workshop.demo.DTOs.ItemCartDTO;

public interface IPurchaseRepo {

    public List<ReceiptProduct> pay(List<ItemCartDTO> items );

}
