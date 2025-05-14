package workshop.demo.Contrrollers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;

@RestController
@RequestMapping("/stock")
public class StockController {

    private StockService stockService;

    @Autowired
    public StockController(Repos repos) {
        this.stockService = new StockService(repos.stockrepo, repos.storeRepo, repos.auth, repos.userRepo, repos.sUConnectionRepo, repos.UserSuspensionRepo);
    }

    @GetMapping("/getProductInfo")
    public String getProductInfo(@RequestParam String token,
            @RequestParam int productId) {
        ApiResponse<ProductDTO> res;
        try {
            ProductDTO product = stockService.getProductInfo(token, productId);
            res = new ApiResponse<>(product, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/getProductsInStore")
    public String getProductsInStore(@RequestParam int storeId) {
        ApiResponse<List<ItemStoreDTO>> res;
        try {
            List<ItemStoreDTO> products = stockService.getProductsInStore(storeId);
            res = new ApiResponse<>(products, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
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
        ApiResponse<String> res;
        try {
            stockService.addItem(storeId, token, productId, quantity, price, category);
            res = new ApiResponse<>("Item added successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addProduct")
    public String addProduct(@RequestParam String token,
            @RequestParam String name,
            @RequestParam workshop.demo.DTOs.Category category,
            @RequestParam String description,
            @RequestParam String[] keywords) {
        ApiResponse<Integer> res;
        try {
            int productId = stockService.addProduct(token, name, category, description, keywords);
            res = new ApiResponse<>(productId, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @DeleteMapping("/removeItem")
    public String removeItem(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId) {
        ApiResponse<String> res;
        try {
            stockService.removeItem(storeId, token, productId);
            res = new ApiResponse<>("Item removed successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/updateQuantity")
    public String updateQuantity(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId,
            @RequestParam int newQuantity) {
        ApiResponse<String> res;
        try {
            stockService.updateQuantity(storeId, token, productId, newQuantity);
            res = new ApiResponse<>("Quantity updated successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/updatePrice")
    public String updatePrice(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId,
            @RequestParam int newPrice) {
        ApiResponse<String> res;
        try {
            stockService.updatePrice(storeId, token, productId, newPrice);
            res = new ApiResponse<>("Price updated successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/rankProduct")
    public String rankProduct(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId,
            @RequestParam int newRank) {
        ApiResponse<String> res;
        try {
            stockService.rankProduct(storeId, token, productId, newRank);
            res = new ApiResponse<>("Product ranked successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/searchProducts")
    public String searchProducts(@RequestParam String token,
            @RequestBody ProductSearchCriteria criteria) {
        ApiResponse<ItemStoreDTO[]> res;
        try {
            ItemStoreDTO[] items = stockService.searchProducts(token, criteria);
            res = new ApiResponse<>(items, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }
}
