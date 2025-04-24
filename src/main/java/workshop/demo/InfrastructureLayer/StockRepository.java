package workshop.demo.InfrastructureLayer;

import java.util.List;

import workshop.demo.DomainLayer.Stock.IStockRepo;

import workshop.demo.DomainLayer.Stock.ProductDTO;
public class StockRepository implements IStockRepo {

    @Override
    public List<ProductDTO> viewProductsInStore(int storeID){
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'viewAllStores'");
    }
}
