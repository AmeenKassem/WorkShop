package workshop.demo.Controllers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.ActivePurchasesService;
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

    @Autowired
    private StockService stockService;
    @Autowired
    private ActivePurchasesService activeService;

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
    public ResponseEntity<ApiResponse<ItemStoreDTO[]>> getProductsInStore(@RequestParam int storeId) {

        try {
            ItemStoreDTO[] products = stockService.getProductsInStore(storeId);
            return ResponseEntity.ok(new ApiResponse<>(products, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @PostMapping("/addItem")
    public ResponseEntity<?> addItem(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId,
            @RequestParam int quantity,
            @RequestParam int price,
            @RequestParam workshop.demo.DTOs.Category category) {

        try {
            stockService.addItem(storeId, token, productId, quantity, price, category);
            return ResponseEntity.ok(new ApiResponse<>("Item added successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @PostMapping("/addProduct")
    public ResponseEntity<?> addProduct(@RequestParam String token,
            @RequestParam String name,
            @RequestParam workshop.demo.DTOs.Category category,
            @RequestParam String description,
            @RequestParam String[] keywords) {
        try {
            String decodedName = URLDecoder.decode(name, StandardCharsets.UTF_8);
            String decodedDescription = URLDecoder.decode(description, StandardCharsets.UTF_8);
            int productId = stockService.addProduct(token, decodedName, category, decodedDescription, keywords);
            return ResponseEntity.ok(new ApiResponse<>(productId, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @DeleteMapping("/removeItem")
    public ResponseEntity<?> removeItem(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId) {
        try {
            stockService.removeItem(storeId, token, productId);
            return ResponseEntity.ok(new ApiResponse<>("Item removed successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/updateQuantity")
    public ResponseEntity<?> updateQuantity(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId,
            @RequestParam int newQuantity) {

        try {
            stockService.updateQuantity(storeId, token, productId, newQuantity);
            return ResponseEntity.ok(new ApiResponse<>("Quantity updated successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @PostMapping("/updatePrice")
    public ResponseEntity<?> updatePrice(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId,
            @RequestParam int newPrice) {

        try {
            stockService.updatePrice(storeId, token, productId, newPrice);
            return ResponseEntity.ok(new ApiResponse<>("Price updated successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @PostMapping("/rankProduct")
    public ResponseEntity<?> rankProduct(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int productId,
            @RequestParam int newRank) {

        try {
            stockService.rankProduct(storeId, token, productId, newRank);
            return ResponseEntity.ok(new ApiResponse<>("Product ranked successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/searchProducts")
    public ResponseEntity<?> searchProducts(@RequestParam String token,
            @RequestParam(required = false) String productNameFilter,
            @RequestParam(required = false) Category categoryFilter,
            @RequestParam(required = false) String keywordFilter,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minProductRating,
            @RequestParam(required = false) Double maxProductRating) {
        try {
            ItemStoreDTO[] items = stockService.searchProductsOnAllSystem(token,
                    new ProductSearchCriteria(productNameFilter, categoryFilter, keywordFilter, storeId, minPrice,
                            maxPrice, minProductRating, maxProductRating));
            return ResponseEntity.ok(new ApiResponse<>(items, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<?> getAllProducts(@RequestParam String token) {
        try {
            ProductDTO[] products = stockService.getAllProducts(token);
            return ResponseEntity.ok(new ApiResponse<>(products, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    // ===============================================================================================================RANDOMS================================================================================================================
    @GetMapping("/searchRandoms")
    public ResponseEntity<?> searceRandoms(@RequestParam String token,
            @RequestParam(required = false) String productNameFilter,
            @RequestParam(required = false) Category categoryFilter,
            @RequestParam(required = false) String keywordFilter,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minProductRating,
            @RequestParam(required = false) Double maxProductRating) {
        try {
            ProductSearchCriteria criteria = new ProductSearchCriteria(productNameFilter, categoryFilter, keywordFilter,
                    storeId, minPrice, maxPrice, minProductRating, maxProductRating);
            RandomDTO[] results = activeService.searchActiveRandoms(token, criteria);
            return ResponseEntity.ok(new ApiResponse<>(results, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllRandomInStore")
    public ResponseEntity<?> getAllRandomInStore(@RequestParam String token, @RequestParam int storeId) {
        try {
            RandomDTO[] randoms = activeService.getAllRandoms(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(randoms, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllActiveRandomsInStore")
    public ResponseEntity<?> getAllActiveRandomInStore_user(@RequestParam String token, @RequestParam int storeId) {
        try {
            RandomDTO[] randoms = activeService.getAllActiveRandoms_user(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(randoms, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/setProductToRandom")
    public ResponseEntity<?> setProductToRandom(@RequestParam String token, @RequestParam int productId,
            @RequestParam int quantity,
            @RequestParam double productPrice, @RequestParam int storeId, @RequestParam long randomTime) {
        try {
            int result = activeService.setProductToRandom(token, productId, quantity, productPrice, storeId,
                    randomTime);
            return ResponseEntity.ok(new ApiResponse<>(result, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    // ===============================================================================================================BIDS================================================================================================================
    @GetMapping("/searchBids")
    public ResponseEntity<?> searchBids(@RequestParam String token,
            @RequestParam(required = false) String productNameFilter,
            @RequestParam(required = false) Category categoryFilter,
            @RequestParam(required = false) String keywordFilter,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minProductRating,
            @RequestParam(required = false) Double maxProductRating) {
        try {
            ProductSearchCriteria criteria = new ProductSearchCriteria(productNameFilter, categoryFilter, keywordFilter,
                    storeId, minPrice, maxPrice, minProductRating, maxProductRating);
            BidDTO[] results = activeService.searchActiveBids(token, criteria);
            return ResponseEntity.ok(new ApiResponse<>(results, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/acceptBid")
    public ResponseEntity<?> acceptBid(@RequestParam String token,
            @RequestParam int storeId, @RequestParam int bidId, @RequestParam int userToAcceptForId) {
        try {
            //System.out.println("fuck1");
            activeService.acceptBid(token, storeId, bidId, userToAcceptForId);
            return ResponseEntity.ok(new ApiResponse<>("Bid accepted", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/addRegularBid")
    public ResponseEntity<?> addRegularBid(@RequestParam String token,
            @RequestParam int bidId,
            @RequestParam int storeId,
            @RequestParam double offer) {
        try {
            activeService.addUserBidToBid(token, bidId, storeId, offer);
            return ResponseEntity.ok(new ApiResponse<>("Bid placed successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/setProductToBid")
    public ResponseEntity<?> setProductToBid(@RequestParam String token, @RequestParam int storeId,
            @RequestParam int productId, @RequestParam int quantity) {
        try {
            int id = activeService.setProductToBid(token, storeId, productId, quantity);
            return ResponseEntity.ok(new ApiResponse<>(id, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllBidsStatus")
    public ResponseEntity<?> getAllBidsStatus(@RequestParam String token, @RequestParam int storeId) {
        try {
            BidDTO[] bids = activeService.getAllBids(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(bids, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllBidsInStore")
    public ResponseEntity<?> getAllBids(@RequestParam String token, @RequestParam int storeId) {
        try {
            BidDTO[] bids = activeService.getAllBids(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(bids, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllActiveBidsInStore")
    public ResponseEntity<?> getAllActiveBids_user(@RequestParam String token, @RequestParam int storeId) {
        try {
            BidDTO[] bids = activeService.getAllActiveBids_user(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(bids, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/rejectBid")
    public ResponseEntity<?> rejectBid(@RequestParam String token, @RequestParam int storeId, @RequestParam int bidId,
            @RequestParam int userToRejectForId, @RequestParam(required = false) Double ownerOffer) {
        try {
            activeService.rejectBid(token, storeId, bidId, userToRejectForId, ownerOffer);
            return ResponseEntity.ok(new ApiResponse<>("Bid rejected successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    // ===============================================================================================================AUCTIONS================================================================================================================
    @GetMapping("/searchAuctions")
    public ResponseEntity<?> searchAuctions(@RequestParam String token,
            @RequestParam(required = false) String productNameFilter,
            @RequestParam(required = false) Category categoryFilter,
            @RequestParam(required = false) String keywordFilter,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minProductRating,
            @RequestParam(required = false) Double maxProductRating) {
        try {
            ProductSearchCriteria criteria = new ProductSearchCriteria(productNameFilter, categoryFilter, keywordFilter,
                    storeId, minPrice, maxPrice, minProductRating, maxProductRating);
            AuctionDTO[] results = activeService.searchActiveAuctions(token, criteria);
            return ResponseEntity.ok(new ApiResponse<>(results, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllAuctions")
    public ResponseEntity<?> getAllAuctions(@RequestParam String token, @RequestParam int storeId) {
        try {
            AuctionDTO[] auctions = activeService.getAllAuctions(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(auctions, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getAllActiveAuctionsInStore")
    public ResponseEntity<?> getAllActiveAuctions_user(@RequestParam String token, @RequestParam int storeId) {
        try {
            AuctionDTO[] auctions = activeService.getAllActiveAuctions_user(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(auctions, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/addBidOnAuction")
    public ResponseEntity<?> addBidOnAuction(@RequestParam String token, @RequestParam int auctionId,
            @RequestParam int storeId, @RequestParam double price) {
        try {
            activeService.addBidOnAucction(token, auctionId, storeId, price);
            return ResponseEntity.ok(new ApiResponse<>("Bid added successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/setProductToAuction")
    public ResponseEntity<?> setProductToAuction(@RequestParam String token, @RequestParam int storeId,
            @RequestParam int productId, @RequestParam int quantity,
            @RequestParam long time, @RequestParam double startPrice) {
        try {
            int id = activeService.setProductToAuction(token, storeId, productId, quantity, time, startPrice);
            return ResponseEntity.ok(new ApiResponse<>(id, null));

        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

}
