
package workshop.demo.UnitTests.StoreTests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.Status;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ActivePurcheses;
import workshop.demo.DomainLayer.Stock.Auction;
import workshop.demo.DomainLayer.Stock.BID;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.AndDiscount;
import workshop.demo.DomainLayer.Store.CompositeDiscount;
import workshop.demo.DomainLayer.Store.Discount;
import workshop.demo.DomainLayer.Store.DiscountConditions;
import workshop.demo.DomainLayer.Store.DiscountFactory;
import workshop.demo.DomainLayer.Store.DiscountScope;
import workshop.demo.DomainLayer.Store.InvisibleDiscount;
import workshop.demo.DomainLayer.Store.MaxDiscount;
import workshop.demo.DomainLayer.Store.OrDiscount;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.Store.VisibleDiscount;
import workshop.demo.DomainLayer.Store.XorDiscount;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Offer;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.StoreUserConnection.SuperDataStructure;
import workshop.demo.DomainLayer.StoreUserConnection.Tree;
import workshop.demo.DomainLayer.User.UserSpecialItemCart;

@SpringBootTest
@ActiveProfiles("test")
public class RandomTests {

    private SingleBid auctionBid;
    private SingleBid standardBid;
    private Auction auction;
    private Store store;
    Node owner;
    Node manager;
    private SuperDataStructure superDS;
    private BID bid;
    private Product product;

    private List<ItemStoreDTO> items;
    private DiscountScope scope;
    private StoreStock storeStock;
    private item testItem;
    private ActivePurcheses active;
    private final int storeId = 1;

    @BeforeEach
    void setUp() {
        active = new ActivePurcheses(storeId);

        bid = new BID(123, 2, 10); // productId=123, quantity=2, bidId=1, storeId=10
        storeStock = new StoreStock(1);
        testItem = new item(1, 10, 1000, Category.Electronics);
        storeStock.addItem(testItem);
        auction = new Auction(1, 5, 2000, 100, 10, 0);
        // random = new Random(1, 5, 100.0, 10, 200, 2000);
        store = new Store("TechStore", "ELECTRONICS");
        superDS = new SuperDataStructure();
        owner = new Node(1, -1, false, null); // root owner
        manager = new Node(2, 1, false, owner); // manager added by owner
        items = new ArrayList<>();
        items.add(new ItemStoreDTO(1, 1, 1000, Category.Electronics, 0, 1, "Laptop", "test"));
        items.add(new ItemStoreDTO(1, 1, 50, Category.Electronics, 0, 1, "Laptop1", "test"));
        scope = new DiscountScope(items);
        String[] keywords = { "gaming", "laptop", "performance" };
        product = new Product("Laptop", Category.Electronics, "High-end laptop", keywords);
        auctionBid = new SingleBid(1, 2, 100, 999.99, SpecialType.Auction, 10, 1);
        standardBid = new SingleBid(2, 3, 200, 499.49, SpecialType.BID, 20, 2);
        // standardBid.ownersNum = 2;
    }

    @Test
    void testVisibleDiscount_applicable() {
        Discount d = new VisibleDiscount(
                "10% Electronics",
                0.1,
                s -> s.containsCategory("ELECTRONICS"),
                "category == ELECTRONICS");
        assertTrue(d.isApplicable(scope));
        assertEquals(105, d.apply(scope), 0.01);
    }

    @Test
    void testInvisibleDiscount_notApplicable() {
        Discount d = new InvisibleDiscount(
                "10% Books",
                0.1,
                s -> s.containsCategory("BOOKS"),
                "category == BOOKS");
        assertFalse(d.isApplicable(scope));
        assertEquals(0.0, d.apply(scope), 0.01);
    }

    @Test
    void testAndDiscount() {
        AndDiscount and = new AndDiscount("AND Combo");
        and.addDiscount(new VisibleDiscount(
                "10% Electronics",
                0.1,
                s -> s.containsCategory("ELECTRONICS"),
                "category == ELECTRONICS"));
        and.addDiscount(new VisibleDiscount(
                "5% Electronics",
                0.05,
                s -> s.containsCategory("ELECTRONICS"),
                "category == ELECTRONICS"));
        assertTrue(and.isApplicable(scope));
        assertEquals(157.5, and.apply(scope), 0.01);
    }

    @Test
    void testOrDiscount() {
        OrDiscount or = new OrDiscount("OR Combo");
        or.addDiscount(new VisibleDiscount(
                "10% Electronics",
                0.1,
                s -> s.containsCategory("ELECTRONICS"),
                "category == ELECTRONICS"));
        or.addDiscount(new VisibleDiscount(
                "5% Books",
                0.05,
                s -> s.containsCategory("BOOKS"),
                "category == BOOKS"));
        assertTrue(or.isApplicable(scope));
        assertEquals(105, or.apply(scope), 0.01);
    }

    @Test
    void testMaxDiscount() {
        MaxDiscount max = new MaxDiscount("MAX Combo");
        max.addDiscount(new VisibleDiscount(
                "10% Electronics",
                0.1,
                s -> s.containsCategory("ELECTRONICS"),
                "category == ELECTRONICS"));
        max.addDiscount(new VisibleDiscount(
                "20% Accessories",
                0.2,
                s -> s.containsCategory("ACCESSORIES"),
                "category == ACCESSORIES"));
        assertTrue(max.isApplicable(scope));
        assertEquals(105, max.apply(scope), 0.01); // 20% of 50
    }

    @Test
    void testXorDiscount_singleValid() {
        XorDiscount xor = new XorDiscount("XOR Combo");
        xor.addDiscount(new VisibleDiscount(
                "10% Electronics",
                0.1,
                s -> s.containsCategory("ELECTRONICS"),
                "category == ELECTRONICS"));
        xor.addDiscount(new VisibleDiscount(
                "5% Books",
                0.05,
                s -> s.containsCategory("BOOKS"),
                "category == BOOKS"));
        assertTrue(xor.isApplicable(scope));
        assertEquals(105, xor.apply(scope), 0.01);
    }

    @Test
    void testXorDiscount_multipleValid() {
        XorDiscount xor = new XorDiscount("XOR Combo");
        xor.addDiscount(new VisibleDiscount(
                "10% Electronics",
                0.1,
                s -> s.containsCategory("ELECTRONICS"),
                "category == ELECTRONICS"));
        xor.addDiscount(new VisibleDiscount(
                "5% Accessories",
                0.05,
                s -> s.containsCategory("ACCESSORIES"),
                "category == ACCESSORIES"));
        assertTrue(xor.isApplicable(scope));
        assertEquals(105, xor.apply(scope), 0.01);
    }

    @Test
    void testRemoveDiscountByName_nested() {
        OrDiscount root = new OrDiscount("root");
        AndDiscount child = new AndDiscount("child");
        VisibleDiscount leaf = new VisibleDiscount(
                "leaf",
                0.1,
                s -> true,
                "always true");
        child.addDiscount(leaf);
        root.addDiscount(child);

        assertTrue(root.removeDiscountByName("leaf"));
        assertTrue(root.getDiscounts().isEmpty());
    }

    @Test
    void testDiscountFactory_createsComposite() {
        CreateDiscountDTO d1 = new CreateDiscountDTO("D1", 0.1, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("D2", 0.05, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ACCESSORIES",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO dto = new CreateDiscountDTO("FactoryTest", 0.0, CreateDiscountDTO.Type.VISIBLE, null,
                CreateDiscountDTO.Logic.OR, List.of(d1, d2));

        Discount d = DiscountFactory.fromDTO(dto);
        assertEquals("FactoryTest", d.getName());
        assertTrue(d.isApplicable(scope));
        assertTrue(d instanceof CompositeDiscount);
    }

    @Test
    void testDiscountConditions_totalPrice() {
        Predicate<DiscountScope> cond = DiscountConditions.fromString("TOTAL>900");
        assertTrue(cond.test(scope));
    }

    @Test
    void testDiscountConditions_category() {
        Predicate<DiscountScope> cond = DiscountConditions.fromString("CATEGORY:ELECTRONICS");
        assertTrue(cond.test(scope));
    }

    @Test
    void testDiscountConditions_quantity() {
        Predicate<DiscountScope> cond = DiscountConditions.fromString("QUANTITY>2");
        assertTrue(cond.test(scope));
    }

    @Test
    void testDiscountConditions_item() {
        Predicate<DiscountScope> cond = DiscountConditions.fromString("ITEM:1");
        assertTrue(cond.test(scope));
    }

    @Test
    void testDiscountConditions_store() {
        Predicate<DiscountScope> cond = DiscountConditions.fromString("STORE:1");
        assertTrue(cond.test(scope));
    }

    @Test
    void testDiscountConditions_invalid() {
        Predicate<DiscountScope> cond = DiscountConditions.fromString("BAD:VALUE");
        assertFalse(cond.test(scope));
    }

    @Test
    void testBidSuccess() throws UIException {
        var bid = auction.bid(101, 50.0);
        assertNotNull(bid);
        assertEquals(50.0, bid.getBidPrice());
    }

    @Test
    void testBidTooLow() throws UIException {
        auction.bid(101, 50.0);
        UIException ex = assertThrows(UIException.class, () -> auction.bid(102, 30.0));
        assertTrue(ex.getMessage().contains("must be higher"));
    }

    @Test
    void testBidAfterAuctionEndsThrows() throws Exception {
        auction.bid(101, 100.0);
        Thread.sleep(2100); // allow it to finish
        UIException ex = assertThrows(UIException.class, () -> auction.bid(102, 120.0));
        assertTrue(ex.getMessage().contains("ended"));
    }

    @Test
    void testBidIsWinnerFunction() throws Exception {
        var b1 = auction.bid(201, 300.0);
        Thread.sleep(2100); // wait for auction to end
        assertTrue(!auction.bidIsWinner(b1.getId()));
    }

    @Test
    void testGetBid() throws UIException {
        var b1 = auction.bid(123, 250.0);
        assertEquals(b1, auction.getBid(b1.getId()));
        assertNull(auction.getBid(999)); // not exist
    }
    
    @Test
    void testConstructorAndGetters() {
        assertEquals("TechStore", store.getStoreName());
        assertEquals("ELECTRONICS", store.getCategory());
        assertTrue(store.isActive());
    }

    @Test
    void testSetActive() {
        store.setActive(false);
        assertFalse(store.isActive());
    }

    @Test
    void testValidRanking() {
        assertTrue(store.rankStore(1));
        assertTrue(store.rankStore(5));
    }

    @Test
    void testInvalidRanking() {
        assertFalse(store.rankStore(0));
        assertFalse(store.rankStore(6));
    }

    @Test
    void testFinalRank_Default() {
        assertEquals(3, store.getFinalRateInStore()); // no votes yet
    }

    @Test
    void testFinalRank_WithVotes() {
        store.rankStore(4); // vote once for 4
        store.rankStore(5); // vote once for 5
        assertEquals(5, store.getFinalRateInStore());
    }

    @Test
    void testGetStoreDTO() {
        StoreDTO dto = store.getStoreDTO();
        assertEquals("TechStore", dto.getStoreName());
        assertEquals("ELECTRONICS", dto.getCategory());
        assertTrue(dto.isActive());
    }

    @Test
    void testSetDiscountAndGet() {
        Discount d = new VisibleDiscount("10%", 0.1, s -> true, "always true");
        store.setDiscount(d);
        assertEquals(d, store.getDiscount());
    }

    @Test
    void testAddDiscountToEmpty() {
        Discount d = new VisibleDiscount("10%", 0.1, s -> true, "always true");
        store.addDiscount(d);
        assertEquals(d, store.getDiscount());
    }

    @Test
    void testAddDiscountToExisting() {
        Discount d1 = new VisibleDiscount("D1", 0.1, s -> true, "always true");
        Discount d2 = new VisibleDiscount("D2", 0.2, s -> true, "always true");

        store.setDiscount(d1);
        store.addDiscount(d2);

        Discount result = store.getDiscount();
        assertTrue(result instanceof MaxDiscount);
        assertEquals("Auto-wrapped discounts", result.getName());
    }

    @Test
    void testAddDiscountToComposite() {
        MaxDiscount composite = new MaxDiscount("Combo");
        Discount d1 = new VisibleDiscount("D1", 0.1, s -> true, "always true");
        Discount d2 = new VisibleDiscount("D2", 0.2, s -> true, "always true");

        composite.addDiscount(d1);
        store.setDiscount(composite);
        store.addDiscount(d2);

        Discount result = store.getDiscount();
        assertTrue(result instanceof MaxDiscount);
        assertEquals(2, ((MaxDiscount) result).getDiscounts().size());
    }

    @Test
    void testRemoveDiscount_SimpleMatch() {
        Discount d = new VisibleDiscount("10%", 0.1, s -> true, "always true");
        store.setDiscount(d);
        assertTrue(store.removeDiscountByName("10%"));
        assertNull(store.getDiscount());
    }

    @Test
    void testRemoveDiscount_NestedMatch() {
        MaxDiscount composite = new MaxDiscount("Combo");
        Discount d = new VisibleDiscount("D1", 0.1, s -> true, "always true");
        composite.addDiscount(d);
        store.setDiscount(composite);
        assertTrue(store.removeDiscountByName("D1"));
    }

    @Test
    void testRemoveDiscount_NotFound() {
        Discount d = new VisibleDiscount("D1", 0.1, s -> true, "always true");
        store.setDiscount(d);
        assertFalse(store.removeDiscountByName("NoMatch"));
    }

    @Test
    void testAddChildAndGetNode_NotFound() {
        Node found = owner.getNode(99);
        assertNull(found);
    }

    @Test
    void testAddAuthorization_NotManager() {
        assertThrows(UIException.class, () -> owner.addAuthrization(List.of(), -1));
    }

    @Test
    void testUpdateAuthorization_NotManager() {
        assertThrows(UIException.class, () -> owner.updateAuthorization(List.of(), -1));
    }

    @Test
    void testDeleteNode_NotFound() {
        assertFalse(owner.deleteNode(999));
    }

    @Test
    void testGetChildNotFound() {
        Node result = owner.getChild(99);
        assertNull(result);
    }

    @Test
    void testGetChildren() {
        owner.addChild(manager);
        List<Node> children = owner.getChildren();
        assertEquals(1, children.size());
    }

    @Test
    void testAddNewOwner_StoreDoesNotExist_ThrowsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            superDS.addNewOwner(999, 1, 2); // store 999 doesn't exist
        });
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testDeleteOwnership_StoreDoesNotExist() {
        DevException ex = assertThrows(DevException.class, () -> {
            superDS.DeleteOwnershipFromStore(999, 1, 2); // store 999 doesn't exist
        });
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testAddNewManager_StoreDoesNotExist_ThrowsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            superDS.addNewManager(999, 1, 2);
        });
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testChangeAutho_StoreNotExist_ThrowsDevException() {
        DevException ex = assertThrows(DevException.class, () -> {
            superDS.changeAuthoToManager(999, 1, 2, List.of(Permission.AddToStock));
        });
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    public void testMatchesForStore_AllFiltersMatch() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[] { new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(1),
                new AtomicInteger(0), new AtomicInteger(0) };
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_CategoryMismatch() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        item testItem = new item(1, 1, 3, Category.Clothing);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MinPriceFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        item testItem = new item(1, 1, 3, Category.Sports);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MaxPriceFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        item testItem = new item(1, 1, 3, Category.Sports);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MinRatingFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[] { new AtomicInteger(1), new AtomicInteger(0), new AtomicInteger(0),
                new AtomicInteger(0), new AtomicInteger(0) };
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MaxRatingFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[] { new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0),
                new AtomicInteger(0), new AtomicInteger(1) };
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testProductIsMatch_AllFiltersMatch() {
        Product product = new Product("Fresh Milk", Category.Sports, "Healthy and Cold",
                new String[] { "cold", "fresh" });
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_NameFilterFails() {
        Product product = new Product("Bread", Category.Sports, "Healthy and Cold", new String[] { "cold", "fresh" });
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_CategoryMismatch() {
        Product product = new Product("Fresh Milk", Category.Clothing, "Healthy and Cold",
                new String[] { "cold", "fresh" });
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        assertFalse(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_KeywordFails() {
        Product product = new Product("Fresh Milk", Category.Sports, "Healthy and Cold",
                new String[] { "sweet", "soft" });
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.Sports, // categoryFilter
                null, // keywordFilter
                Integer.valueOf(1), // storeId
                Double.valueOf(10.0), // minPrice
                Double.valueOf(50.0), // maxPrice
                Double.valueOf(4.0), // minStoreRating
                Double.valueOf(5.0) // maxStoreRating
        );
        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testSpecificStore() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null, 10, null, null, null, null);
        assertTrue(criteria.specificStore());
    }

    @Test
    public void testSpecificCategory() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, Category.Clothing, null, null, null, null,
                null, null);
        assertTrue(criteria.specificCategory());
    }

    @Test
    void testEquals_SameReference() {
        UserSpecialItemCart cart = new UserSpecialItemCart(1, 2, 3, SpecialType.BID, 1);
        assertTrue(cart.equals(cart)); // Line 20
    }

    @Test
    void testEquals_NullObject() {
        UserSpecialItemCart cart = new UserSpecialItemCart(1, 2, 3, SpecialType.BID, 1);
        assertFalse(cart.equals(null)); // Line 21
    }

    @Test
    void testEquals_DifferentClass() {
        UserSpecialItemCart cart = new UserSpecialItemCart(1, 2, 3, SpecialType.BID, 1);
        assertFalse(cart.equals("not a cart")); // Line 21
    }

    @Test
    void testEquals_DifferentStoreId() {
        UserSpecialItemCart c1 = new UserSpecialItemCart(1, 2, 3, SpecialType.BID, 1);
        UserSpecialItemCart c2 = new UserSpecialItemCart(99, 2, 3, SpecialType.BID, 1);
        assertFalse(c1.equals(c2));
    }

    @Test
    void testEquals_DifferentSpecialId() {
        UserSpecialItemCart c1 = new UserSpecialItemCart(1, 2, 3, SpecialType.BID, 1);
        UserSpecialItemCart c2 = new UserSpecialItemCart(1, 99, 3, SpecialType.BID, 1);
        assertFalse(c1.equals(c2));
    }

    @Test
    void testEquals_DifferentType() {
        UserSpecialItemCart c1 = new UserSpecialItemCart(1, 2, 3, SpecialType.BID, 1);
        UserSpecialItemCart c2 = new UserSpecialItemCart(1, 2, 3, SpecialType.Auction, 1);
        assertFalse(c1.equals(c2));
    }

    @Test
    void testEquals_ExactMatch() {
        UserSpecialItemCart c1 = new UserSpecialItemCart(1, 2, 3, SpecialType.Random, 1);
        UserSpecialItemCart c2 = new UserSpecialItemCart(1, 2, 999, SpecialType.Random, 1); // bidId ignored
        assertTrue(c1.equals(c2));
    }

    @Test
    void testBidSuccess1() throws UIException {
        SingleBid b = bid.bid(5, 99.99);
        assertNotNull(b);
        assertEquals(5, b.getUserId());
    }

    @Test
    void testRejectBidWithInvalidIdThrows() {
        DevException ex = assertThrows(DevException.class, () -> bid.rejectBid(404));
        assertEquals("Trying to reject bid with non-existent ID.", ex.getMessage());
    }
   
    @Test
    void testGetDTOIncludesAllBids() throws Exception {
        bid.bid(1, 10.0);
        bid.bid(2, 12.0);
        BidDTO dto = bid.getDTO();

        assertEquals(2, dto.bids.length);
        assertEquals(123, dto.productId);
        assertFalse(dto.isAccepted);
    }

    @Test
    void testGetProductId() {
        assertEquals(123, bid.getProductId());
    }

    @Test
    void testAddItem_NewItem() {
        item newItem = new item(2, 5, 500, Category.Books);
        storeStock.addItem(newItem);
        assertEquals(2, storeStock.getAllItemsInStock().size());
    }

    @Test
    void testAddItem_ExistingItemIncrementsQuantity() {
        int originalQty = testItem.getQuantity();
        storeStock.addItem(testItem);
        assertTrue(testItem.getQuantity() > originalQty);
    }

    @Test
    void testRemoveItem_Success() throws Exception {
        storeStock.removeItem(1);
        assertEquals(0, storeStock.getItemByProductId(1).getQuantity());
    }

    @Test
    void testRemoveItem_NotFound_Throws() {
        UIException ex = assertThrows(UIException.class, () -> storeStock.removeItem(999));
        assertTrue(ex.getMessage().contains("Item not found"));
    }

    @Test
    void testChangeQuantity_Success() throws Exception {
        storeStock.changeQuantity(1, 5);
        assertEquals(5, storeStock.getItemByProductId(1).getQuantity());
    }

    @Test
    void testChangeQuantity_NotFound_Throws() {
        UIException ex = assertThrows(UIException.class, () -> storeStock.changeQuantity(999, 3));
        assertTrue(ex.getMessage().contains("Item not found"));
    }

    @Test
    void testDecreaseQuantityToBuy_Success() throws Exception {
        storeStock.decreaseQuantitytoBuy(1, 2);
        assertEquals(8, storeStock.getItemByProductId(1).getQuantity());
    }

    @Test
    void testDecreaseQuantityToBuy_InsufficientStock_Throws() {
        UIException ex = assertThrows(UIException.class, () -> storeStock.decreaseQuantitytoBuy(0, 100));
    }

    @Test
    void testDecreaseQuantityToBuy_NotFound_Throws() {
        UIException ex = assertThrows(UIException.class, () -> storeStock.decreaseQuantitytoBuy(999, 1));
        assertTrue(ex.getMessage().contains("Item not found"));
    }

    @Test
    void testUpdatePrice_Success() throws Exception {
        storeStock.updatePrice(1, 1500);
        assertEquals(1500, storeStock.getItemByProductId(1).getPrice());
    }

    @Test
    void testUpdatePrice_NotFound_Throws() {
        UIException ex = assertThrows(UIException.class, () -> storeStock.updatePrice(999, 300));
        assertTrue(ex.getMessage().contains("Item not found"));
    }

    @Test
    void testRankProduct_Success() throws Exception {
        storeStock.rankProduct(1, 3);
        assertEquals(1, storeStock.getItemByProductId(1).getRank()[2].get());
    }

    @Test
    void testRankProduct_NotFound_Throws() {
        UIException ex = assertThrows(UIException.class, () -> storeStock.rankProduct(999, 2));
        assertTrue(ex.getMessage().contains("Product ID not found"));
    }

    @Test
    void testGetItemsByCategoryObject() {
        item another = new item(2, 5, 2000, Category.Electronics);
        storeStock.addItem(another);
        List<item> electronics = storeStock.getItemsByCategoryObject(Category.Electronics);
        assertEquals(2, electronics.size());
    }

    @Test
    void testChangeQuantityBatch_GuestFailsOnInsufficient() {
        ItemCartDTO dto = new ItemCartDTO(1, 1, 9999, 1000, "Phone", "Store", Category.Electronics);
        assertThrows(UIException.class,
                () -> storeStock.changequantity(List.of(dto), true, "StoreName"));
    }

    @Test
    void testChangeQuantityBatch_RegisteredSkipsInvalid() throws Exception {
        ItemCartDTO valid = new ItemCartDTO(1, 1, 1, 1000, "Phone", "Store", Category.Electronics);
        ItemCartDTO invalid = new ItemCartDTO(999, 1, 5, 1000, "X", "Y", Category.Books);

        // Should not throw exception for registered users
        storeStock.changequantity(List.of(invalid, valid), false, "StoreName");

        assertEquals(4, storeStock.getItemByProductId(1).getQuantity()); // 10 - 1
    }

    @Test
    void testGetStoreStockId() {
        assertEquals(1, storeStock.getStoreStockId());
    }

    @Test
    void testGetStock() {
        assertTrue(storeStock.getStock().containsKey(1));
    }

    @Test
    void testGetName() {
        assertEquals("Laptop", product.getName());
    }

    @Test
    void testSetName() {
        product.setName("New Laptop");
        assertEquals("New Laptop", product.getName());
    }

    @Test
    void testGetProductId1() {
        assertEquals(0, product.getProductId());
    }

    @Test
    void testSetProductId() {
        product.setProductId(99);
        assertEquals(99, product.getProductId());
    }

    @Test
    void testGetDescription() {
        assertEquals("High-end laptop", product.getDescription());
    }

    @Test
    void testSetDescription() {
        product.setDescription("Gaming Laptop");
        assertEquals("Gaming Laptop", product.getDescription());
    }

    @Test
    void testGetCategory() {
        assertEquals(Category.Electronics, product.getCategory());
    }

    @Test
    void testSetCategory() {
        product.setCategory(Category.Books);
        assertEquals(Category.Books, product.getCategory());
    }

    @Test
    void testGetKeywords() {
        String[] keywords = product.getKeywords();
        assertEquals(3, keywords.length);
        assertEquals("gaming", keywords[0]);
    }

    @Test
    void testSetKeywords() {
        String[] newKeywords = { "tech", "fast", "new" };
        product.setKeywords(newKeywords);
        String[] actual = product.getKeywords();
        assertArrayEquals(newKeywords, actual);
    }

    @Test
    void testGetProductPrice_NotFound_Throws() {
        assertThrows(DevException.class, () -> active.getProductPrice(999));
    }

    @Test
    void testGetRandom_NotFound_Throws() {
        assertThrows(DevException.class, () -> active.getRandom(888));
    }

    @Test
    void testgetRandomCardforuser_ReturnsNull_IfNotWinnerOrNotExist() throws Exception {
        assertNull(active.getRandomCardforuser(777, 5));
    }

    @Test
    void testGetBidIfWinner_ReturnsNull_NotFoundOrNotWinner() {
        assertNull(active.getBidIfWinner(999, 1, SpecialType.BID));
    }

    @Test
    void testGetBidWithId_ReturnsNull_IfMissing() {
        assertNull(active.getBidWithId(1234, 1, SpecialType.BID));
        assertNull(active.getBidWithId(1234, 1, SpecialType.Auction));
    }

    @Test
    void testGetCardWithId_NotFound() {
        assertNull(active.getCardWithId(5555, 1));
    }

    @Test
    void testGetProductIdForSpecial_Auction() throws Exception {
        var id = active.addProductToAuction(42, 1, 99999, 0);
        assertEquals(42, active.getProductIdForSpecial(id.getId(), SpecialType.Auction));
    }

    @Test
    void testGetProductIdForSpecial_BID() throws Exception {
        int id = active.addProductToBid(44, 1);
        assertEquals(44, active.getProductIdForSpecial(id, SpecialType.BID));
    }

    @Test
    void testGetWinner_WhenNoWinner_ReturnsNull() {
        Auction auction = new Auction(1, 1, 5000, 1, 10, 0);
        assertNull(auction.getWinner()); // Timer hasn't ended, so no winner
    }

    @Test
    void testBidIsWinner_WhenNoWinner_ReturnsFalse() {
        Auction auction = new Auction(1, 1, 5000, 1, 10, 0);
        assertFalse(auction.bidIsWinner(999));
    }

    @Test
    void testGetBidPrice() {
        assertEquals(999.99, auctionBid.getBidPrice());
    }

    @Test
    void testGetId() {
        assertEquals(0, auctionBid.getId());
        assertEquals(0, standardBid.getId());
    }

    @Test
    void testGetUserId() {
        assertEquals(100, auctionBid.getUserId());
    }

    @Test
    void testMarkAsWinner_Auction() {
        auctionBid.markAsWinner();
        assertEquals(Status.AUCTION_WON, auctionBid.getStatus());
        assertTrue(auctionBid.isWinner());
        assertTrue(auctionBid.isWon());
        assertTrue(auctionBid.isEnded());
    }

    @Test
    void testMarkAsLosed_Auction() {
        auctionBid.markAsLosed();
        assertEquals(Status.AUCTION_LOSED, auctionBid.getStatus());
        assertFalse(auctionBid.isWinner());
        assertTrue(auctionBid.isEnded());
    }

    @Test
    void testRejectBid() {
        standardBid.rejectBid();
        assertEquals(Status.BID_REJECTED, standardBid.getStatus());
        assertFalse(standardBid.isAccepted());
        assertFalse(standardBid.isWinner());
        assertTrue(standardBid.isEnded());
    }

    @Test
    void testConvertToDTO_AuctionPending() {
        SingleBidDTO dto = auctionBid.convertToDTO();
        assertEquals(auctionBid.getId(), dto.id);
        assertEquals(SpecialType.Auction, dto.type);
        assertFalse(dto.isWinner);
        assertFalse(dto.isAccepted);
        assertFalse(dto.isEnded);
    }

    @Test
    void testConvertToDTO_AuctionWon() {
        auctionBid.markAsWinner();
        SingleBidDTO dto = auctionBid.convertToDTO();
        assertTrue(dto.isWinner);
        assertTrue(dto.isEnded);
    }

    @Test
    void testGetters1() {
        assertEquals(3, standardBid.getAmount());
        assertEquals(1, auctionBid.getSpecialId());
        assertEquals(10, auctionBid.getStoreId());
        assertEquals(SpecialType.Auction, auctionBid.getType());
        assertEquals(Status.AUCTION_PENDING, auctionBid.getStatus());
    }

    @Test
    void testProductIdGetter() {
        assertEquals(1, auctionBid.getProductId());
    }

    @Test
    void testIsEnded_FalseInitially() {
        assertFalse(auctionBid.isEnded());
        assertFalse(standardBid.isEnded());
    }

    @Test
    void testRankItem_ValidRanks() {
        for (int i = 1; i <= 5; i++) {
            boolean result = testItem.rankItem(i);
            assertTrue(result, "Rank " + i + " should return true");
            assertEquals(1, testItem.getRank()[i - 1].get(), "Rank index " + (i - 1) + " should be incremented");
        }
    }

    @Test
    void testRankItem_InvalidLowRank() {
        boolean result = testItem.rankItem(0);
        assertFalse(result);
        for (int i = 0; i < 5; i++) {
            assertEquals(0, testItem.getRank()[i].get());
        }
    }

    @Test
    void testRankItem_InvalidHighRank() {
        boolean result = testItem.rankItem(6);
        assertFalse(result);
        for (int i = 0; i < 5; i++) {
            assertEquals(0, testItem.getRank()[i].get());
        }
    }

    @Test
    void testMultipleRankIncrements() {
        testItem.rankItem(2);
        testItem.rankItem(2);
        assertEquals(2, testItem.getRank()[1].get());
    }
}
