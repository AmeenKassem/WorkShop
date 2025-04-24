package workshop.demo.DomainLayer.Stock;

import java.util.List;

public interface IStockRepo {

    public List<ProductDTO> viewProductsInStore(int storeID);

}
