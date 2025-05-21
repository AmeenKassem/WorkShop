package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;

@RestController
@RequestMapping("/stock")
public class StockController {

    private StockService stockService;

    @Autowired
    public StockController(Repos repos) {
        this.stockService = new StockService(repos.stockrepo, repos.storeRepo, repos.auth, repos.userRepo,
                repos.sUConnectionRepo, repos.UserSuspensionRepo);
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
        ApiResponse<ItemStoreDTO[]> res;
        try {
            ItemStoreDTO[] products = stockService.getProductsInStore(storeId);
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
            @RequestParam(required = false) String productNameFilter,
            @RequestParam(required = false) Category categoryFilter,
            @RequestParam(required = false) String keywordFilter,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minProductRating,
            @RequestParam(required = false) Double maxProductRating) {
        ApiResponse<ItemStoreDTO[]> res;
        try {
            ItemStoreDTO[] items = stockService.searchProducts(token, new ProductSearchCriteria(productNameFilter, categoryFilter, keywordFilter, storeId, minPrice, maxPrice, minProductRating, maxProductRating));
            res = new ApiResponse<>(items, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }


    @PostMapping("/acceptBid")
    public ResponseEntity<?> acceptBid(@RequestParam String token,
            @RequestBody int storeId,@RequestBody int bidId,@RequestBody int bidToAcceptId ) {
        try {
            stockService.acceptBid(token,storeId,bidId,bidToAcceptId);
            return ResponseEntity.ok(new ApiResponse<>("Store closed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllAuctions")
    public ResponseEntity<?> getAllAuctions(@RequestParam String token,@RequestParam int storeId) {
        try {
            AuctionDTO[] auctions = stockService.getAllAuctions(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(auctions, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/addBidOnAuction")
    public ResponseEntity<?> addBidOnAuction(@RequestParam String token,@RequestParam int auctionId,
                                             @RequestParam int storeId,@RequestParam double price) {
        try {
            stockService.addBidOnAucction(token, auctionId, storeId, price);
            return ResponseEntity.ok(new ApiResponse<>("Bid added successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

     @PostMapping("/addRegularBid")
    public ResponseEntity<?> addRegularBid(@RequestParam String token,
                                           @RequestParam int bitId,
                                           @RequestParam int storeId,
                                           @RequestParam double price) {
        try {
            stockService.addRegularBid(token, bitId, storeId, price);
            return ResponseEntity.ok(new ApiResponse<>("Bid placed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/setProductToAuction")
    public ResponseEntity<?> setProductToAuction(@RequestParam String token,@RequestParam int storeId,
                                                 @RequestParam int productId,@RequestParam int quantity,
                                                 @RequestParam long time,@RequestParam double startPrice) {
        try {
            int id = stockService.setProductToAuction(token, storeId, productId, quantity, time, startPrice);
            return ResponseEntity.ok(new ApiResponse<>(id, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/setProductToBid")
    public ResponseEntity<?> setProductToBid(@RequestParam String token,@RequestParam int storeId,@RequestParam int productId,@RequestParam int quantity) {
        try {
            int id = stockService.setProductToBid(token, storeId, productId, quantity);
            return ResponseEntity.ok(new ApiResponse<>(id, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

        @GetMapping("/getAllBidsStatus")
    public ResponseEntity<?> getAllBidsStatus(@RequestParam String token,@RequestParam int storeId) {
        try {
            BidDTO[] bids = stockService.getAllBidsStatus(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(bids, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/endRandom")
    public ResponseEntity<?> endRandom(@RequestParam String token,@RequestParam int storeId,@RequestParam int randomId) {
        try {
            ParticipationInRandomDTO result = stockService.endBid(token, storeId, randomId);
            return ResponseEntity.ok(new ApiResponse<>(result, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllRandomInStore")
    public ResponseEntity<?> getAllRandomInStore(@RequestParam String token,@RequestParam int storeId) {
        try {
            RandomDTO[] randoms = stockService.getAllRandomInStore(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(randoms, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/setProductToRandom")
    public ResponseEntity<?> setProductToRandom(@RequestParam String token,@RequestParam int productId,@RequestParam int quantity,
                                                @RequestParam double productPrice,@RequestParam int storeId,@RequestParam long randomTime) {
        try {
            int result = stockService.setProductToRandom(token, productId, quantity, productPrice, storeId, randomTime);
            return ResponseEntity.ok(new ApiResponse<>(result, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllProducts")
    public String getAllProducts(@RequestParam String token) {
        ApiResponse<ProductDTO[]> res;
        try {
            ProductDTO[] products = stockService.getAllProducts(token);
            res = new ApiResponse<>(products, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

}

