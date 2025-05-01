package workshop.demo.InfrastructureLayer;

import java.util.HashMap;
import java.util.List;

import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Stock.Filters;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.StoreStock;

public class StockRepo implements IStockRepo {
    private HashMap<Integer, StoreStock> stockForStore;
    
    public List<ProductDTO> getProducts(Filters filter) {
        if (filter.specificStore())
            return stockForStore.get(filter.getStoreId()).getProducts(filter);
        for (StoreStock stock : stockForStore.) {
            
        }
    }

}
