package workshop.demo.UnitTests.StoreTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Auction;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Store.*;
import workshop.demo.DomainLayer.StoreUserConnection.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class RandomTests {

    private Auction auction;
    private Random random;
    private Store store;
    Node owner;
    Node manager;
    private SuperDataStructure superDS;

    private List<ItemStoreDTO> items;
    private DiscountScope scope;

    @BeforeEach
    void setUp() {
        auction = new Auction(1, 5, 2000, 100, 10);
        random = new Random(1, 5, 100.0, 10, 200, 2000);
        store = new Store(1, "TechStore", "ELECTRONICS");
        superDS = new SuperDataStructure();
        owner = new Node(1, false, -1); // root owner
        manager = new Node(2, true, 1); // manager added by owner
        items = new ArrayList<>();
        items.add(new ItemStoreDTO(1, 1, 1000, Category.Electronics, 0, 1, "Laptop", "test"));
        items.add(new ItemStoreDTO(1, 1, 50, Category.Electronics, 0, 1, "Laptop1", "test"));
        scope = new DiscountScope(items);
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

}
