package workshop.demo.DomainLayer.Stock;

import java.util.Collection;
import java.util.List;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Store.item;

public class Filters {
    private double minRank;
    private double maxRank;
    //...
    public boolean specificCategory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'specificCategory'");
    }
    public Category getCategory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCategory'");
    }
    public List<ProductDTO> filteredProducts(List<item> list) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filteredProducts'");
    }
    public boolean specificStore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'specificStore'");
    }
    public Object getStoreId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStoreId'");
    }
}
