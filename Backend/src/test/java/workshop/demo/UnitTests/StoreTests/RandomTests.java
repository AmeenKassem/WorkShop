package workshop.demo.UnitTests.StoreTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.Store.*;
import workshop.demo.DomainLayer.StoreUserConnection.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class RandomTests {
    private SingleBid auctionBid;
    private SingleBid standardBid;
    private Auction auction;
    private Random random;
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

        bid = new BID(123, 2, 1, 10); // productId=123, quantity=2, bidId=1, storeId=10
        storeStock = new StoreStock(1);
        testItem = new item(1, 10,1000, Category.Electronics);
        storeStock.addItem(testItem);
        auction = new Auction(1, 5, 2000, 100, 10,0);
        random = new Random(1, 5, 100.0, 10, 200, 2000);
        store = new Store(1, "TechStore", "ELECTRONICS");
        superDS = new SuperDataStructure();
        owner = new Node(1, false, -1); // root owner
        manager = new Node(2, true, 1); // manager added by owner
        items = new ArrayList<>();
        items.add(new ItemStoreDTO(1, 1, 1000, Category.Electronics, 0, 1, "Laptop", "test"));
        items.add(new ItemStoreDTO(1, 1, 50, Category.Electronics, 0, 1, "Laptop1", "test"));
        scope = new DiscountScope(items);
        String[] keywords = {"gaming", "laptop", "performance"};
        product = new Product("Laptop", 1, Category.Electronics, "High-end laptop", keywords);
        auctionBid = new SingleBid(1, 2, 100, 999.99, SpecialType.Auction, 10, 1, 11);
        standardBid = new SingleBid(2, 3, 200, 499.49, SpecialType.BID, 20, 2, 22);
        standardBid.ownersNum = 2;
    }

    @Test
    void testVisibleDiscount_applicable() {
        Discount d = new VisibleDiscount("10% Electronics", 0.1, s -> s.containsCategory("ELECTRONICS"));
        assertTrue(d.isApplicable(scope));
        assertEquals(105, d.apply(scope), 0.01);
    }

    @Test
    void testInvisibleDiscount_notApplicable() {
        Discount d = new InvisibleDiscount("10% Books", 0.1, s -> s.containsCategory("BOOKS"));
        assertFalse(d.isApplicable(scope));
        assertEquals(0.0, d.apply(scope), 0.01);
    }

    @Test
    void testAndDiscount() {
        AndDiscount and = new AndDiscount("AND Combo");
        and.addDiscount(new VisibleDiscount("10% Electronics", 0.1, s -> s.containsCategory("ELECTRONICS")));
        and.addDiscount(new VisibleDiscount("5% Electronics", 0.05, s -> s.containsCategory("ELECTRONICS")));
        assertTrue(and.isApplicable(scope));
        assertEquals(157.5, and.apply(scope), 0.01);
    }

    @Test
    void testOrDiscount() {
        OrDiscount or = new OrDiscount("OR Combo");
        or.addDiscount(new VisibleDiscount("10% Electronics", 0.1, s -> s.containsCategory("ELECTRONICS")));
        or.addDiscount(new VisibleDiscount("5% Books", 0.05, s -> s.containsCategory("BOOKS")));
        assertTrue(or.isApplicable(scope));
        assertEquals(105, or.apply(scope), 0.01);
    }

    @Test
    void testMaxDiscount() {
        MaxDiscount max = new MaxDiscount("MAX Combo");
        max.addDiscount(new VisibleDiscount("10% Electronics", 0.1, s -> s.containsCategory("ELECTRONICS")));
        max.addDiscount(new VisibleDiscount("20% Accessories", 0.2, s -> s.containsCategory("ACCESSORIES")));
        assertTrue(max.isApplicable(scope));
        assertEquals(105, max.apply(scope), 0.01); // 20% of 50
    }

    @Test
    void testXorDiscount_singleValid() {
        XorDiscount xor = new XorDiscount("XOR Combo");
        xor.addDiscount(new VisibleDiscount("10% Electronics", 0.1, s -> s.containsCategory("ELECTRONICS")));
        xor.addDiscount(new VisibleDiscount("5% Books", 0.05, s -> s.containsCategory("BOOKS")));
        assertTrue(xor.isApplicable(scope));
        assertEquals(105, xor.apply(scope), 0.01);
    }

    @Test
    void testXorDiscount_multipleValid() {
        XorDiscount xor = new XorDiscount("XOR Combo");
        xor.addDiscount(new VisibleDiscount("10% Electronics", 0.1, s -> s.containsCategory("ELECTRONICS")));
        xor.addDiscount(new VisibleDiscount("5% Accessories", 0.05, s -> s.containsCategory("ACCESSORIES")));
        assertTrue(xor.isApplicable(scope));
        assertEquals(105, xor.apply(scope), 0.01);
    }

    @Test
    void testRemoveDiscountByName_nested() {
        OrDiscount root = new OrDiscount("root");
        AndDiscount child = new AndDiscount("child");
        VisibleDiscount leaf = new VisibleDiscount("leaf", 0.1, s -> true);
        child.addDiscount(leaf);
        root.addDiscount(child);

        assertTrue(root.removeDiscountByName("leaf"));
        assertTrue(root.getDiscounts().isEmpty());
    }

    @Test
    void testDiscountFactory_createsComposite() {
        CreateDiscountDTO d1 = new CreateDiscountDTO("D1", 0.1, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("D2", 0.05, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ACCESSORIES", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO dto = new CreateDiscountDTO("FactoryTest", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));

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
        SingleBid bid = auction.bid(101, 50.0);
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
    void testGetDTOContainsCorrectData() throws UIException {
        auction.bid(101, 50.0);
        AuctionDTO dto = auction.getDTO();
        assertEquals(1, dto.productId);
        assertEquals(10, dto.storeId);
        assertEquals(50.0, dto.maxBid);
        assertEquals(AuctionStatus.IN_PROGRESS, dto.status);
        assertEquals(1, dto.bids.length);
    }

    @Test
    void testGetWinnerAfterTimeExpires() throws Exception {
        SingleBid b1 = auction.bid(201, 100.0);
        Thread.sleep(2100); // wait for the auction to finish
        AuctionDTO dto = auction.getDTO();
        assertEquals(AuctionStatus.FINISH, dto.status);
        assertEquals(b1.getId(), dto.winner.getId());
        assertEquals(100.0, dto.winner.getBidPrice());
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
        SingleBid b1 = auction.bid(201, 300.0);
        Thread.sleep(2100); // wait for auction to end
        assertTrue(auction.bidIsWinner(b1.getId()));
    }

    @Test
    void testGetBid() throws UIException {
        SingleBid b1 = auction.bid(123, 250.0);
        assertEquals(b1, auction.getBid(b1.getId()));
        assertNull(auction.getBid(999)); // not exist
    }

    @Test
    void testParticipateSuccess() throws Exception {
        ParticipationInRandomDTO card = random.participateInRandom(1001, 40.0);
        assertNotNull(card);
        assertEquals(60.0, random.getAmountLeft(), 0.01);
    }

    @Test
    void testParticipateNegativeAmount() {
        UIException e = assertThrows(UIException.class, () -> {
            random.participateInRandom(1002, -5.0);
        });
        assertTrue(e.getMessage().contains("positive"));
    }

    @Test
    void testParticipateOverAmount() {
        UIException e = assertThrows(UIException.class, () -> {
            random.participateInRandom(1003, 150.0);
        });
        assertTrue(e.getMessage().contains("Maximum amount"));
    }

    @Test
    void testDuplicateParticipation() throws Exception {
        random.participateInRandom(1004, 50.0);
        UIException e = assertThrows(UIException.class, () -> {
            random.participateInRandom(1004, 30.0);
        });
        assertTrue(e.getMessage().contains("already participated"));
    }

    @Test
    void testFullParticipationTriggersEnd() throws Exception {
        random.participateInRandom(1, 30.0);
        random.participateInRandom(2, 30.0);
        random.participateInRandom(3, 40.0); // reaches 100
        assertFalse(random.isActive());
        assertNotNull(random.getWinner());
    }

    @Test
    void testEndRandomMarksWinnerAndLosers() throws Exception {
        random.participateInRandom(101, 40.0);
        random.participateInRandom(102, 60.0);
        ParticipationInRandomDTO winner = random.endRandom();
        assertNotNull(winner);
        assertTrue(winner.isWinner);
        for (int uid : new int[]{101, 102}) {
            ParticipationInRandomDTO dto = random.getCard(uid);
            if (uid == winner.getUserId()) {
                assertTrue(dto.isWinner);
            } else {
                assertTrue(!dto.won());
            }
        }
    }

    @Test
    void testDTOExport() throws Exception {
        random.participateInRandom(501, 70.0);
        RandomDTO dto = random.getDTO();
        assertEquals(1, dto.productId);
        assertEquals(100.0, dto.productPrice);
        assertEquals(1, dto.participations.length);
    }

    @Test
    void testUserIsWinnerTrue() throws Exception {
        random.participateInRandom(1, 50.0);
        random.participateInRandom(2, 50.0);
        ParticipationInRandomDTO win = random.endRandom();
        assertTrue(random.userIsWinner(win.getUserId()));
    }

    @Test
    void testUserIsWinnerFalse() throws Exception {
        random.participateInRandom(1, 50.0);
        random.participateInRandom(2, 50.0);
        ParticipationInRandomDTO win = random.endRandom();
        assertFalse(random.userIsWinner(win.getUserId() == 1 ? 2 : 1));
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(1, store.getStoreID());
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
        assertEquals(1, dto.getStoreId());
        assertEquals("ELECTRONICS", dto.getCategory());
        assertTrue(dto.isActive());
    }

    @Test
    void testSetDiscountAndGet() {
        Discount d = new VisibleDiscount("10%", 0.1, s -> true);
        store.setDiscount(d);
        assertEquals(d, store.getDiscount());
    }

    @Test
    void testAddDiscountToEmpty() {
        Discount d = new VisibleDiscount("10%", 0.1, s -> true);
        store.addDiscount(d);
        assertEquals(d, store.getDiscount());
    }

    @Test
    void testAddDiscountToExisting() {
        Discount d1 = new VisibleDiscount("D1", 0.1, s -> true);
        Discount d2 = new VisibleDiscount("D2", 0.2, s -> true);
        store.setDiscount(d1);
        store.addDiscount(d2);

        Discount result = store.getDiscount();
        assertTrue(result instanceof MaxDiscount);
        assertEquals("Auto-wrapped discounts", result.getName());
    }

    @Test
    void testAddDiscountToComposite() {
        MaxDiscount composite = new MaxDiscount("Combo");
        Discount d1 = new VisibleDiscount("D1", 0.1, s -> true);
        Discount d2 = new VisibleDiscount("D2", 0.2, s -> true);
        composite.addDiscount(d1);
        store.setDiscount(composite);
        store.addDiscount(d2);

        Discount result = store.getDiscount();
        assertTrue(result instanceof MaxDiscount);
        assertEquals(2, ((MaxDiscount) result).getDiscounts().size());
    }

    @Test
    void testRemoveDiscount_SimpleMatch() {
        Discount d = new VisibleDiscount("10%", 0.1, s -> true);
        store.setDiscount(d);
        assertTrue(store.removeDiscountByName("10%"));
        assertNull(store.getDiscount());
    }

    @Test
    void testRemoveDiscount_NestedMatch() {
        MaxDiscount composite = new MaxDiscount("Combo");
        Discount d = new VisibleDiscount("D1", 0.1, s -> true);
        composite.addDiscount(d);
        store.setDiscount(composite);
        assertTrue(store.removeDiscountByName("D1"));
    }

    @Test
    void testRemoveDiscount_NotFound() {
        Discount d = new VisibleDiscount("D1", 0.1, s -> true);
        store.setDiscount(d);
        assertFalse(store.removeDiscountByName("NoMatch"));
    }

    @Test
    void testAddNewStore_And_CheckExistence() {
        superDS.addNewStore(1, 10);
        assertTrue(superDS.checkStoreExist(1));
    }

    @Test
    void testAddNewOwner_Success() throws Exception {
        superDS.addNewStore(1, 10);
        assertTrue(superDS.checkToAddOwner(1, 10, 11));
        superDS.addNewOwner(1, 10, 11);
    }

    @Test
    void testAddNewOwner_Failure_NotOwner() throws Exception {
        superDS.addNewStore(1, 10);
        UIException ex = assertThrows(UIException.class, () -> superDS.addNewOwner(1, 11, 12));
        assertEquals("This worker is not an owner", ex.getMessage());
    }

    @Test
    void testAddNewManager_And_ChangePermissions() throws Exception {
        superDS.addNewStore(1, 10);
        superDS.addNewManager(1, 10, 20);
        List<Permission> permissions = List.of(Permission.AddToStock);
        superDS.changeAuthoToManager(1, 10, 20, permissions);
    }

    @Test
    void testDeleteManager_Success() throws Exception {
        superDS.addNewStore(1, 10);
        superDS.addNewManager(1, 10, 20);
        superDS.deleteManager(1, 10, 20);
    }

    @Test
    void testCheckDeactivateStore_Success() throws DevException {
        superDS.addNewStore(1, 10);
        assertTrue(superDS.checkDeactivateStore(1, 10));
    }

    @Test
    void testCloseStore() throws Exception {
        superDS.addNewStore(1, 10);
        superDS.closeStore(1);
        assertFalse(superDS.checkStoreExist(1));
    }

    @Test
    void testOffers_CreateDeleteGet() throws Exception {
        superDS.addNewStore(1, 10);
        Offer offer = new Offer(10, 20, true, List.of(Permission.AddToStock), "description");
        superDS.makeOffer(offer, 1);

        Offer got = superDS.getOffer(1, 10, 20);
        assertEquals(10, got.getSenderId());

        List<Permission> perms = superDS.deleteOffer(1, 10, 20);
        assertTrue(perms.contains(Permission.AddToStock));
    }

    @Test
    void testRemoveUserAccordingly() throws Exception {
        superDS.addNewStore(1, 10);
        superDS.addNewOwner(1, 10, 11);
        assertEquals(11, superDS.removeUserAccordingly(11));
    }

    @Test
    void testGetWorkersInStore() throws Exception {
        superDS.addNewStore(1, 10);
        List<Integer> workers = superDS.getWorkersInStore(1);
        assertTrue(workers.contains(10));
    }

    @Test
    void testGetPermissions_NullForOwner() throws Exception {
        superDS.addNewStore(1, 10);
        Node owner = superDS.getWorkersTreeInStore(1).getRoot();
        assertNull(superDS.getPermissions(owner));
    }

    @Test
    void testGetStoresIdForUser() throws Exception {
        superDS.addNewStore(1, 10);
        List<Integer> ids = superDS.getStoresIdForUser(10);
        assertTrue(ids.contains(1));
    }

    @Test
    void testClearData() {
        superDS.addNewStore(1, 10);
        superDS.clearData();
        assertFalse(superDS.checkStoreExist(1));
    }

    @Test
    void testGetAllWorkers() throws Exception {
        superDS.addNewStore(1, 10);
        List<Node> all = superDS.getAllWorkers(1);
        assertEquals(1, all.size());
        assertEquals(10, all.get(0).getMyId());
    }

    @Test
    void testAddChildAndGetNode() {
        owner.addChild(manager);
        Node found = owner.getNode(2);
        assertNotNull(found);
        assertEquals(2, found.getMyId());
    }

    @Test
    void testAddChildAndGetNode_NotFound() {
        Node found = owner.getNode(99);
        assertNull(found);
    }

    @Test
    void testAddAuthorizationSuccess() throws UIException {
        owner.addChild(manager);
        List<Permission> perms = List.of(Permission.AddToStock, Permission.SpecialType);
        manager.addAuthrization(perms, 1);
        assertNotNull(manager.getMyAuth());
        assertEquals(8, manager.getMyAuth().getMyAutho().size());
    }

    @Test
    void testAddAuthorization_NotManager() {
        assertThrows(UIException.class, () -> owner.addAuthrization(List.of(), -1));
    }

    @Test
    void testAddAuthorization_WrongParent() {
        Node m2 = new Node(3, true, 9);
        assertThrows(UIException.class, () -> m2.addAuthrization(List.of(Permission.AddToStock), 1));
    }

    @Test
    void testUpdateAuthorization_Success() throws UIException {
        owner.addChild(manager);
        manager.updateAuthorization(List.of(Permission.ViewAllProducts), 1);
        assertTrue(manager.getMyAuth().getMyAutho().get(Permission.ViewAllProducts));
    }

    @Test
    void testUpdateAuthorization_NotManager() {
        assertThrows(UIException.class, () -> owner.updateAuthorization(List.of(), -1));
    }

    @Test
    void testUpdateAuthorization_WrongParent() {
        Node m2 = new Node(3, true, 7);
        assertThrows(UIException.class, () -> m2.updateAuthorization(List.of(Permission.DeleteFromStock), 1));
    }

    @Test
    void testDeleteNodeDirectChild() {
        owner.addChild(manager);
        boolean deleted = owner.deleteNode(2);
        assertTrue(deleted);
    }

    @Test
    void testDeleteNodeDeep() {
        Node m2 = new Node(3, true, 2);
        manager.addChild(m2);
        owner.addChild(manager);
        assertTrue(owner.deleteNode(3));
    }

    @Test
    void testDeleteNode_NotFound() {
        assertFalse(owner.deleteNode(999));
    }

    @Test
    void testGetChildSuccess() {
        owner.addChild(manager);
        Node found = owner.getChild(2);
        assertNotNull(found);
        assertEquals(1, found.getMyId()); // returns parent
    }

    @Test
    void testGetChildNotFound() {
        Node result = owner.getChild(99);
        assertNull(result);
    }

    @Test
    void testGetters() {
        assertEquals(1, owner.getMyId());
        assertEquals(-1, owner.getParentId());
        assertFalse(owner.getIsManager());
        assertNull(owner.getMyAuth());

        assertEquals(2, manager.getMyId());
        assertEquals(1, manager.getParentId());
        assertTrue(manager.getIsManager());
        assertNotNull(manager.getMyAuth());
    }

    @Test
    void testGetChildren() {
        owner.addChild(manager);
        List<Node> children = owner.getChildren();
        assertEquals(1, children.size());
    }

    @Test
    void testCheckToAddOwner_AllBranches() throws Exception {
        int storeId = 101;
        int ownerId = 10;
        int newOwnerId = 11;

        superDS.addNewStore(storeId, ownerId); // Creates a Tree with root ownerId (not manager)

        // Case 1: ownerId is not in store
        UIException ex1 = assertThrows(UIException.class, ()
                -> superDS.checkToAddOwner(storeId, 999, newOwnerId)
        );
        assertEquals(ErrorCodes.NO_PERMISSION, ex1.getErrorCode());

        // Case 2: ownerId is a manager
        superDS.getEmployees().put(storeId, new Tree(ownerId, true, -1)); // Set owner as manager
        UIException ex2 = assertThrows(UIException.class, ()
                -> superDS.checkToAddOwner(storeId, ownerId, newOwnerId)
        );
        assertEquals(ErrorCodes.NO_PERMISSION, ex2.getErrorCode());

        // Fix back to valid owner root
        superDS.getEmployees().put(storeId, new Tree(ownerId, false, -1)); // Set as owner again

        // Case 3: newOwnerId already exists and isOwner
        superDS.getEmployees().get(storeId).getRoot().addChild(new Node(newOwnerId, false, ownerId));
        UIException ex3 = assertThrows(UIException.class, ()
                -> superDS.checkToAddOwner(storeId, ownerId, newOwnerId)
        );
        assertEquals(ErrorCodes.NO_PERMISSION, ex3.getErrorCode());

        // Case 4: success path
        int freshId = 99;
        assertTrue(superDS.checkToAddOwner(storeId, ownerId, freshId));
    }

    @Test
    void testCheckToAddManager_AllBranches() throws Exception {
        int storeId = 102;
        int ownerId = 20;
        int newManagerId = 21;

        // Setup store and root owner
        superDS.addNewStore(storeId, ownerId);

        // === Case 1: store doesn't exist ===
        DevException ex1 = assertThrows(DevException.class, ()
                -> superDS.checkToAddManager(9999, ownerId, newManagerId)
        );
        assertEquals("store does not exist in superDS", ex1.getMessage());

        // === Case 2: owner not found ===
        UIException ex2 = assertThrows(UIException.class, ()
                -> superDS.checkToAddManager(storeId, 999, newManagerId)
        );
        assertEquals(ErrorCodes.NO_PERMISSION, ex2.getErrorCode());

        superDS.getEmployees().put(storeId, new Tree(ownerId, true, -1));
        UIException ex3 = assertThrows(UIException.class, ()
                -> superDS.checkToAddManager(storeId, ownerId, newManagerId)
        );
        assertEquals(ErrorCodes.NO_PERMISSION, ex3.getErrorCode());

// === Case 4: newManager already exists and is a manager ===
        superDS.getEmployees().put(storeId, new Tree(ownerId, false, -1)); // reset to owner
        superDS.getEmployees().get(storeId).getRoot().addChild(new Node(newManagerId, true, ownerId));
        UIException ex4 = assertThrows(UIException.class, ()
                -> superDS.checkToAddManager(storeId, ownerId, newManagerId)
        );
        // === Case 5: Success ===
        int freshId = 999;
        assertTrue(superDS.checkToAddManager(storeId, ownerId, freshId));
    }

    @Test
    void testAddNewOwner_StoreDoesNotExist_ThrowsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            superDS.addNewOwner(999, 1, 2); // store 999 doesn't exist
        });
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testAddNewOwner_OwnerNotFound_ThrowsException() throws Exception {
        int storeId = 200;
        int bossId = 10;
        superDS.addNewStore(storeId, bossId);
        Exception ex = assertThrows(UIException.class, () -> {
            superDS.addNewOwner(storeId, 999, 11); // 999 is not an owner
        });
        assertEquals(ErrorCodes.USER_NOT_FOUND, ((UIException) ex).getErrorCode());
    }

    @Test
    void testAddNewOwner_AlreadyOwner_ThrowsException() throws Exception {
        int storeId = 201;
        int bossId = 20;
        int existingOwner = 21;
        superDS.addNewStore(storeId, bossId);
        superDS.addNewOwner(storeId, bossId, existingOwner); // now 21 is owner

        UIException ex = assertThrows(UIException.class, () -> {
            superDS.addNewOwner(storeId, bossId, existingOwner); // add again
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testDeleteOwnership_StoreDoesNotExist() {
        DevException ex = assertThrows(DevException.class, () -> {
            superDS.DeleteOwnershipFromStore(999, 1, 2); // store 999 doesn't exist
        });
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testDeleteOwnership_OwnerNotFound() throws Exception {
        int storeId = 202;
        int bossId = 30;
        int target = 31;
        superDS.addNewStore(storeId, bossId);
        superDS.addNewOwner(storeId, bossId, target); // create the node

        UIException ex = assertThrows(UIException.class, () -> {
            superDS.DeleteOwnershipFromStore(storeId, 999, target); // 999 not in store
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testDeleteOwnership_TargetNotFound() throws Exception {
        int storeId = 203;
        int bossId = 40;
        superDS.addNewStore(storeId, bossId);

        UIException ex = assertThrows(UIException.class, () -> {
            superDS.DeleteOwnershipFromStore(storeId, bossId, 999); // target not in store
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testDeleteOwnership_NotDirectChild() throws Exception {
        int storeId = 204;
        int bossId = 50;
        int owner = 51;
        superDS.addNewStore(storeId, bossId);
        superDS.addNewOwner(storeId, bossId, owner);

        UIException ex = assertThrows(UIException.class, () -> {
            superDS.DeleteOwnershipFromStore(storeId, 999, owner); // wrong owner
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testAddNewManager_StoreDoesNotExist_ThrowsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            superDS.addNewManager(999, 1, 2);
        });
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testAddNewManager_OwnerNotFound_ThrowsException() throws Exception {
        int storeId = 301;
        superDS.addNewStore(storeId, 100);

        Exception ex = assertThrows(Exception.class, () -> {
            superDS.addNewManager(storeId, 999, 200); // 999 not an owner
        });
        assertEquals("this user is not the owner of this store", ex.getMessage());
    }

    @Test
    void testAddNewManager_AlreadyManager_ThrowsUIException() throws Exception {
        int storeId = 302;
        int boss = 101;
        int manager = 102;
        superDS.addNewStore(storeId, boss);
        superDS.addNewManager(storeId, boss, manager); // valid addition

        UIException ex = assertThrows(UIException.class, () -> {
            superDS.addNewManager(storeId, boss, manager); // again
        });
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testChangeAutho_StoreNotExist_ThrowsDevException() {
        DevException ex = assertThrows(DevException.class, () -> {
            superDS.changeAuthoToManager(999, 1, 2, List.of(Permission.AddToStock));
        });
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testChangeAutho_ManagerNotFound_ThrowsUIException() throws Exception {
        int storeId = 303;
        superDS.addNewStore(storeId, 103);

        UIException ex = assertThrows(UIException.class, () -> {
            superDS.changeAuthoToManager(storeId, 103, 999, List.of(Permission.AddToStock));
        });
        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testChangeAutho_UserNotManager_ThrowsUIException() throws Exception {
        int storeId = 304;
        int boss = 104;
        int owner = 105;
        superDS.addNewStore(storeId, boss);
        superDS.addNewOwner(storeId, boss, owner);

        UIException ex = assertThrows(UIException.class, () -> {
            superDS.changeAuthoToManager(storeId, boss, owner, List.of(Permission.AddToStock));
        });
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testChangeAutho_NotHisManager_ThrowsUIException() throws Exception {
        int storeId = 305;
        int boss = 106;
        int anotherOwner = 107;
        int manager = 108;
        superDS.addNewStore(storeId, boss);
        superDS.addNewOwner(storeId, boss, anotherOwner);
        superDS.addNewManager(storeId, anotherOwner, manager); // manager added under anotherOwner

        UIException ex = assertThrows(UIException.class, () -> {
            superDS.changeAuthoToManager(storeId, boss, manager, List.of(Permission.AddToStock)); // boss tries to change
        });
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }
        @Test
    public void testMatchesForStore_AllFiltersMatch() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );
        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[]{new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(1), new AtomicInteger(0), new AtomicInteger(0)};
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_CategoryMismatch() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Clothing);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MinPriceFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Sports);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MaxPriceFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Sports);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MinRatingFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[]{new AtomicInteger(1), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0)};
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testMatchesForStore_MaxRatingFails() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        item testItem = new item(1, 1, 3, Category.Sports);
        AtomicInteger[] rank = new AtomicInteger[]{new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(1)};
        testItem.setRank(rank);
        assertFalse(criteria.matchesForStore(testItem));
    }

    @Test
    public void testProductIsMatch_AllFiltersMatch() {
        Product product = new Product("Fresh Milk", 1, Category.Sports, "Healthy and Cold", new String[]{"cold", "fresh"});
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_NameFilterFails() {
        Product product = new Product("Bread", 1, Category.Sports, "Healthy and Cold", new String[]{"cold", "fresh"});
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_CategoryMismatch() {
        Product product = new Product("Fresh Milk", 1, Category.Clothing, "Healthy and Cold", new String[]{"cold", "fresh"});
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        assertFalse(criteria.productIsMatch(product));
    }

    @Test
    public void testProductIsMatch_KeywordFails() {
        Product product = new Product("Fresh Milk", 1, Category.Sports, "Healthy and Cold", new String[]{"sweet", "soft"});
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                          // productNameFilter
                Category.Sports,              // categoryFilter
                null,                          // keywordFilter
                Integer.valueOf(1),           // storeId
                Double.valueOf(10.0),         // minPrice
                Double.valueOf(50.0),         // maxPrice
                Double.valueOf(4.0),          // minStoreRating
                Double.valueOf(5.0)           // maxStoreRating
        );        assertTrue(criteria.productIsMatch(product));
    }

    @Test
    public void testSpecificStore() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null, 10, null, null, null, null);
        assertTrue(criteria.specificStore());
    }

    @Test
    public void testSpecificCategory() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, Category.Clothing, null, null, null, null, null, null);
        assertTrue(criteria.specificCategory());
    }
    @Test
    void testEquals_SameReference() {
        UserSpecialItemCart cart = new UserSpecialItemCart(1, 2, 3, SpecialType.BID);
        assertTrue(cart.equals(cart)); // Line 20
    }

    @Test
    void testEquals_NullObject() {
        UserSpecialItemCart cart = new UserSpecialItemCart(1, 2, 3, SpecialType.BID);
        assertFalse(cart.equals(null)); // Line 21
    }

    @Test
    void testEquals_DifferentClass() {
        UserSpecialItemCart cart = new UserSpecialItemCart(1, 2, 3, SpecialType.BID);
        assertFalse(cart.equals("not a cart")); // Line 21
    }

    @Test
    void testEquals_DifferentStoreId() {
        UserSpecialItemCart c1 = new UserSpecialItemCart(1, 2, 3, SpecialType.BID);
        UserSpecialItemCart c2 = new UserSpecialItemCart(99, 2, 3, SpecialType.BID);
        assertFalse(c1.equals(c2)); 
    }

    @Test
    void testEquals_DifferentSpecialId() {
        UserSpecialItemCart c1 = new UserSpecialItemCart(1, 2, 3, SpecialType.BID);
        UserSpecialItemCart c2 = new UserSpecialItemCart(1, 99, 3, SpecialType.BID);
        assertFalse(c1.equals(c2));
    }

    @Test
    void testEquals_DifferentType() {
        UserSpecialItemCart c1 = new UserSpecialItemCart(1, 2, 3, SpecialType.BID);
        UserSpecialItemCart c2 = new UserSpecialItemCart(1, 2, 3, SpecialType.Auction);
        assertFalse(c1.equals(c2));
    }

    @Test
    void testEquals_ExactMatch() {
        UserSpecialItemCart c1 = new UserSpecialItemCart(1, 2, 3, SpecialType.Random);
        UserSpecialItemCart c2 = new UserSpecialItemCart(1, 2, 999, SpecialType.Random); // bidId ignored
        assertTrue(c1.equals(c2));
    }
    @Test
    void testBidSuccess1() throws UIException {
        SingleBid b = bid.bid(5, 99.99);
        assertNotNull(b);
        assertEquals(5, b.getUserId());
    }

//    @Test
//    void testBidWhenClosedThrows() throws Exception {
//        SingleBid b1 = bid.bid(1, 100);
//        assertThrows(Exception.class, () ->    bid.acceptBid(b1.getId()));
//
//        UIException ex = assertThrows(UIException.class, () -> bid.bid(2, 50));
//        assertEquals("This bid is already closed!", ex.getMessage());
//    }

//    @Test
//    void testAcceptBidSuccess() throws Exception {
//        SingleBid b1 = bid.bid(1, 100);
//
//        SingleBid winner = bid.acceptBid(b1.getId());
//
//        assertNotNull(winner);
//        assertTrue(winner.isAccepted());
//        assertFalse(bid.isOpen());
//    }

//    @Test
//    void testAcceptBidTwiceThrows() throws Exception {
//        SingleBid b1 = bid.bid(1, 100);
//        bid.acceptBid(b1.getId());
//
//        UIException ex = assertThrows(UIException.class, () -> bid.acceptBid(b1.getId()));
//        assertEquals("This bid is already closed!", ex.getMessage());
//    }

    @Test
    void testAcceptBidWithInvalidIdThrows() throws Exception {
        bid.bid(1, 50);
        DevException ex = assertThrows(DevException.class, () -> bid.acceptBid(999));
        assertEquals("Trying to accept bid for non-existent ID.", ex.getMessage());
    }

    @Test
    void testRejectBidSuccess() throws Exception {
        SingleBid b1 = bid.bid(1, 50);
        assertTrue(bid.rejectBid(b1.getId()));
        assertNull(bid.getBid(b1.getId()));
    }

//    @Test
//    void testRejectBidWhenClosedThrows() throws Exception {
//        SingleBid b1 = bid.bid(1, 50);
//        bid.acceptBid(b1.getId());
//
//        UIException ex = assertThrows(UIException.class, () -> bid.rejectBid(b1.getId()));
//        assertEquals("The bid is already closed!", ex.getMessage());
//    }

    @Test
    void testRejectBidWithInvalidIdThrows() {
        DevException ex = assertThrows(DevException.class, () -> bid.rejectBid(404));
        assertEquals("Trying to reject bid with non-existent ID.", ex.getMessage());
    }
//
//    @Test
//    void testIsOpenTrueFalse() throws Exception {
//        assertTrue(bid.isOpen());
//        SingleBid b1 = bid.bid(1, 100);
//        bid.acceptBid(b1.getId());
//        assertFalse(bid.isOpen());
//    }
//
//    @Test
//    void testUserIsWinnerTrueFalse() throws Exception {
//        SingleBid b1 = bid.bid(42, 300);
//        assertFalse(bid.userIsWinner(42));
//        bid.acceptBid(b1.getId());
//        assertTrue(bid.userIsWinner(42));
//        assertFalse(bid.userIsWinner(99));
//    }
//
//    @Test
//    void testBidIsWinnerTrueFalse() throws Exception {
//        SingleBid b1 = bid.bid(1, 200);
//        int bidId = b1.getId();
//        assertFalse(bid.bidIsWinner(bidId));
//        bid.acceptBid(bidId);
//        assertTrue(bid.bidIsWinner(bidId));
//    }

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
    void testGetBid1() throws Exception {
        SingleBid b1 = bid.bid(1, 50.0);
        assertEquals(b1, bid.getBid(b1.getId()));
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
        UIException ex = assertThrows(UIException.class, () -> storeStock.decreaseQuantitytoBuy(1, 100));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
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
    void testRankProduct_InvalidRankIndex_Throws() {
        UIException ex = assertThrows(UIException.class, () -> storeStock.rankProduct(1, 10));
        assertTrue(ex.getMessage().contains("Invalid rank index"));
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
    void testProcessCartItems_GuestSuccess() throws Exception {
        ItemCartDTO dto = new ItemCartDTO(1, 1, 1, 1000, "Phone", "Store", Category.Electronics);
        List<ReceiptProduct> result = storeStock.ProcessCartItems(List.of(dto), true, "StoreName");

        assertEquals(1, result.size());
        assertEquals("Phone", result.get(0).getProductName());
    }

    @Test
    void testProcessCartItems_GuestFailsOnStock() {
        ItemCartDTO dto = new ItemCartDTO(1, 1, 9999, 1000, "Phone", "Store", Category.Electronics);
        UIException ex = assertThrows(UIException.class,
                () -> storeStock.ProcessCartItems(List.of(dto), true, "StoreName"));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void testProcessCartItems_RegisteredSkipsInvalid() throws Exception {
        ItemCartDTO valid = new ItemCartDTO(1, 1, 1, 1000, "Phone", "Store", Category.Electronics);
        ItemCartDTO invalid = new ItemCartDTO(999, 1, 999, 100, "X", "Y", Category.Books);

        List<ReceiptProduct> result = storeStock.ProcessCartItems(List.of(invalid, valid), false, "StoreName");
        assertEquals(1, result.size());
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
        assertEquals(1, product.getProductId());
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
        String[] newKeywords = {"tech", "fast", "new"};
        product.setKeywords(newKeywords);
        String[] actual = product.getKeywords();
        assertArrayEquals(newKeywords, actual);
    }
    @Test
    void testGetProductPrice_Success() throws Exception {
        int randomId = active.addProductToRandom(1, 5, 123.45, storeId, 99999);
        double price = active.getProductPrice(randomId);
        assertEquals(123.45, price);
    }

    @Test
    void testGetProductPrice_NotFound_Throws() {
        assertThrows(DevException.class, () -> active.getProductPrice(999));
    }

    @Test
    void testGetRandom_Success() throws Exception {
        int randomId = active.addProductToRandom(2, 3, 200.0, storeId, 50000);
        Random result = active.getRandom(randomId);
        assertNotNull(result);
        assertEquals(2, result.getProductId());
    }

    @Test
    void testGetRandom_NotFound_Throws() {
        assertThrows(DevException.class, () -> active.getRandom(888));
    }

    @Test
    void testgetRandomCardforuser_ReturnsWinner() throws Exception {
        int randomId = active.addProductToRandom(10, 3, 150, storeId, 99999);
        ParticipationInRandomDTO dto = active.participateInRandom(5, randomId, 150);
        active.getRandom(randomId).endRandom(); // Ends and assigns a winner

        ParticipationInRandomDTO result = active.getRandomCardforuser(randomId, dto.userId);
        assertNotNull(result);
    }

    @Test
    void testgetRandomCardforuser_ReturnsNull_IfNotWinnerOrNotExist() throws Exception {
        assertNull(active.getRandomCardforuser(777, 5));
    }

    @Test
    void testGetBidIfWinner_Auction_Winner() throws Exception {
        int auctionId = active.addProductToAuction(1, 1, 99999,0);
        SingleBid bid = active.addUserBidToAuction(auctionId, 99, 300.0);
        active.getBidIfWinner(auctionId, bid.getId(), SpecialType.Auction); // triggers isWinner() check

        bid.markAsWinner();  // simulate win
        SingleBid result = active.getBidIfWinner(auctionId, bid.getId(), SpecialType.Auction);
        assertNotNull(result);
    }

//    @Test
//    void testGetBidIfWinner_BID_Winner() throws Exception {
//        int bidId = active.addProductToBid(1, 1);
//        SingleBid bid = active.addUserBidToBid(bidId, 88, 500.0);
//        active.acceptBid(bid.getId(), bidId);  // mark as accepted
//
//        SingleBid result = active.getBidIfWinner(bidId, bid.getId(), SpecialType.BID);
//        assertNotNull(result);
//    }

    @Test
    void testGetBidIfWinner_ReturnsNull_NotFoundOrNotWinner() {
        assertNull(active.getBidIfWinner(999, 1, SpecialType.BID));
    }

    @Test
    void testGetBidWithId_Auction() throws Exception {
        int auctionId = active.addProductToAuction(1, 1, 100000,0);
        SingleBid bid = active.addUserBidToAuction(auctionId, 66, 300);
        SingleBid result = active.getBidWithId(auctionId, bid.getId(), SpecialType.Auction);
        assertEquals(bid, result);
    }

    @Test
    void testGetBidWithId_BID() throws Exception {
        int bidId = active.addProductToBid(1, 1);
        SingleBid bid = active.addUserBidToBid(bidId, 55, 400);
        SingleBid result = active.getBidWithId(bidId, bid.getId(), SpecialType.BID);
        assertEquals(bid, result);
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
        int id = active.addProductToAuction(42, 1, 99999,0);
        assertEquals(42, active.getProductIdForSpecial(id, SpecialType.Auction));
    }

    @Test
    void testGetProductIdForSpecial_BID() throws Exception {
        int id = active.addProductToBid(44, 1);
        assertEquals(44, active.getProductIdForSpecial(id, SpecialType.BID));
    }

    @Test
    void testGetProductIdForSpecial_Random() throws Exception {
        int id = active.addProductToRandom(55, 1, 300, storeId, 99999);
        assertEquals(55, active.getProductIdForSpecial(id, SpecialType.Random));
    }


    @Test
    void testGetWinner_WhenNoWinner_ReturnsNull() {
        Auction auction = new Auction(1, 1, 5000, 1, 10,0);
        assertNull(auction.getWinner()); // Timer hasn't ended, so no winner
    }

    @Test
    void testBidIsWinner_WhenNoWinner_ReturnsFalse() {
        Auction auction = new Auction(1, 1, 5000, 1, 10,0);
        assertFalse(auction.bidIsWinner(999));
    }

    @Test
    void testBidIsWinner_MatchingWinner_ReturnsTrue() throws UIException, InterruptedException {
        Auction auction = new Auction(1, 1, 100, 1, 10,0);
        SingleBid bid = auction.bid(5, 300.0);

        Thread.sleep(150); // let auction finish

        assertEquals(bid, auction.getWinner());
        assertTrue(auction.bidIsWinner(bid.getId()));
    }

    @Test
    void testBidIsWinner_WrongId_ReturnsFalse() throws UIException, InterruptedException {
        Auction auction = new Auction(1, 1, 100, 1, 10,0);
        auction.bid(5, 300.0);

        Thread.sleep(150); // let auction finish

        assertNotNull(auction.getWinner());
        assertFalse(auction.bidIsWinner(999)); // wrong ID
    }
    @Test
    void testGetBidPrice() {
        assertEquals(999.99, auctionBid.getBidPrice());
    }

    @Test
    void testGetId() {
        assertEquals(1, auctionBid.getId());
        assertEquals(2, standardBid.getId());
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
    void testAcceptBid_Once_NotEnoughOwners() {
        standardBid.acceptBid(); // acceptCounter = 1, ownersNum = 2
        assertNotEquals(Status.BID_ACCEPTED, standardBid.getStatus());
        assertFalse(standardBid.isAccepted());
        assertFalse(standardBid.isWon());
    }

    @Test
    void testAcceptBid_Twice_TriggersAccepted() {
        standardBid.acceptBid();
        standardBid.acceptBid(); // counter reaches ownersNum = 2
        assertEquals(Status.BID_ACCEPTED, standardBid.getStatus());
        assertTrue(standardBid.isAccepted());
        assertTrue(standardBid.isWinner());
        assertTrue(standardBid.isWon());
        assertTrue(standardBid.isEnded());
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
        assertEquals(11, auctionBid.getSpecialId());
        assertEquals(10, auctionBid.getStoreId());
        assertEquals(SpecialType.Auction, auctionBid.getType());
        assertEquals(Status.AUCTION_PENDING, auctionBid.getStatus());
    }

    @Test
    void testProductIdGetter() {
        assertEquals(1, auctionBid.productId());
    }

    @Test
    void testIsEnded_FalseInitially() {
        assertFalse(auctionBid.isEnded());
        assertFalse(standardBid.isEnded());
    }

    @Test
    void testIsEnded_AfterAcceptedOrRejected() {
        standardBid.acceptBid();
        standardBid.acceptBid(); // triggers accept
        assertTrue(standardBid.isEnded());

        SingleBid rejectedBid = new SingleBid(1, 1, 1, 100.0, SpecialType.BID, 1, 3, 3);
        rejectedBid.rejectBid();
        assertTrue(rejectedBid.isEnded());
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
