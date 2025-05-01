package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Store.item;

public class StoreStock {
    private HashMap<Category,List<item>> categoryToitems;
    private int storeId;




    public List<ItemStoreDTO> getProducts(Filters filter){
        Set<Category> categories = new HashSet<>();
        List<ItemStoreDTO> products = new ArrayList<>();
        if(filter.specificCategory()){
            categories.add(filter.getCategory());
        }else{
            categories = categoryToitems.keySet();
        }
        for (Category category : categories) {
            if(categoryToitems.containsKey(category)){
                for (item itemOnCategory : categoryToitems.get(category)) {
                    if(filter.matches(itemOnCategory)) {
                        // ItemStoreDTO toAdd = new ItemStoreDTO(storeId, , storeId, null, storeId, storeId)
                        //add to returned list
                        
                    }
                }
            }
        }
        return products;
    }


    
}
