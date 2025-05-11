package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import workshop.demo.ApplicationLayer.Response;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;

import java.util.List;

@RestController
@RequestMapping("/stock")
public class StockController {

    private  StockService stockService;

    @Autowired
    public StockController(Repos repos) {
        this.stockService = new StockService(repos.stockrepo,repos.storeRepo,repos.auth,repos.userRepo,repos.sUConnectionRepo,repos.UserSuspensionRepo);
    }

    @GetMapping("/getProductInfo")
    public String getProductInfo(@RequestParam String token,
                                 @RequestParam int productId) {
        Response<ProductDTO> res;
        try {
            ProductDTO product = stockService.getProductInfo(token, productId);
            res = new Response<>(product, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/getProductsInStore")
    public String getProductsInStore(@RequestParam int storeId) {
        Response<List<ItemStoreDTO>> res;
        try {
            List<ItemStoreDTO> products = stockService.getProductsInStore(storeId);
            res = new Response<>(products, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addItem")
    public String addItem(@RequestParam int storeId,
                          @RequestParam String token,
                          @RequestParam int productId,
                          @RequestParam int quantity,
                          @RequestParam int price,
                          @RequestParam workshop.demo.DTOs.Category category) {
        Response<String> res;
        try {
            stockService.addItem(storeId, token, productId, quantity, price, category);
            res = new Response<>("Item added successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addProduct")
    public String addProduct(@RequestParam String token,
                             @RequestParam String name,
                             @RequestParam workshop.demo.DTOs.Category category,
                             @RequestParam String description,
                             @RequestParam String[] keywords) {
        Response<Integer> res;
        try {
            int productId = stockService.addProduct(token, name, category, description, keywords);
            res = new Response<>(productId, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @DeleteMapping("/removeItem")
    public String removeItem(@RequestParam int storeId,
                             @RequestParam String token,
                             @RequestParam int productId) {
        Response<String> res;
        try {
            stockService.removeItem(storeId, token, productId);
            res = new Response<>("Item removed successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/updateQuantity")
    public String updateQuantity(@RequestParam int storeId,
                                 @RequestParam String token,
                                 @RequestParam int productId,
                                 @RequestParam int newQuantity) {
        Response<String> res;
        try {
            stockService.updateQuantity(storeId, token, productId, newQuantity);
            res = new Response<>("Quantity updated successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/updatePrice")
    public String updatePrice(@RequestParam int storeId,
                              @RequestParam String token,
                              @RequestParam int productId,
                              @RequestParam int newPrice) {
        Response<String> res;
        try {
            stockService.updatePrice(storeId, token, productId, newPrice);
            res = new Response<>("Price updated successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/rankProduct")
    public String rankProduct(@RequestParam int storeId,
                              @RequestParam String token,
                              @RequestParam int productId,
                              @RequestParam int newRank) {
        Response<String> res;
        try {
            stockService.rankProduct(storeId, token, productId, newRank);
            res = new Response<>("Product ranked successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/searchProducts")
    public String searchProducts(@RequestParam String token,
                                 @RequestBody ProductSearchCriteria criteria) {
        Response<ItemStoreDTO[]> res;
        try {
            ItemStoreDTO[] items = stockService.searchProducts(token, criteria);
            res = new Response<>(items, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }
}
