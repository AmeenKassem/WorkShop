package workshop.demo.AcceptanceTest.Tests;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import workshop.demo.AcceptanceTest.Utill.Bridge;
import workshop.demo.AcceptanceTest.Utill.Proxy;
import workshop.demo.DTOs.Category;

public class ConcurrencyAcceptanceTests {

    static Bridge bridge = new Proxy();
    static AtomicInteger idCounter = new AtomicInteger(100);

    private int generateId() {
        return idCounter.getAndIncrement();
    }

    /////////////////////////////
    @Test
    public void testTwoUsersBuyingLastProductConcurrently() throws Exception {
        String token1 = bridge.testGuest_Enter();
        String token2 = bridge.testGuest_Enter();

        bridge.testGuest_Register(token1, "A", "0000", 23);
        bridge.testGuest_Register(token2, "B", "1111", 18);

        bridge.testUser_LogIn(token1, "A", "0000");
        bridge.testUser_LogIn(token2, "B", "1111");

        bridge.testUser_OpenStore(token1, "SHOP1", "ANIMALS");
        int storeId = 1;
        int productId = generateId();

        bridge.testOwner_ManageInventory_AddProduct(storeId, token1, productId, 1, 20, Category.ELECTRONICS);

        Thread buyer1 = new Thread(() -> {
            try {
                bridge.testGuest_AddProductToCart(token1, storeId, productId, 1);
                bridge.testGuest_BuyCart(token1, 1);
            } catch (Exception e) {
                System.out.println("UserA failed: " + e.getMessage());
            }
        });

        Thread buyer2 = new Thread(() -> {
            try {
                bridge.testGuest_AddProductToCart(token2, storeId, productId, 1);
                bridge.testGuest_BuyCart(token2, 2);
            } catch (Exception e) {
                System.out.println("UserB failed: " + e.getMessage());
            }
        });

        buyer1.start();
        buyer2.start();
        buyer1.join();
        buyer2.join();
    }

    /////////////////////////////
    @Test
    public void testDeleteWhileBuying() throws Exception {
        String buyerToken = bridge.testGuest_Enter();
        String ownerToken = bridge.testGuest_Enter();

        bridge.testGuest_Register(buyerToken, "A", "pass", 21);
        bridge.testGuest_Register(ownerToken, "O", "pass", 22);

        bridge.testUser_LogIn(buyerToken, "A", "pass");
        bridge.testUser_LogIn(ownerToken, "O", "pass");

        bridge.testUser_OpenStore(ownerToken, "SHOP2", "CANDY");
        int storeId = 1;
        int productId = generateId();

        bridge.testOwner_ManageInventory_AddProduct(storeId, ownerToken, productId, 5, 15, Category.ELECTRONICS);

        Thread buyer = new Thread(() -> {
            try {
                bridge.testGuest_AddProductToCart(buyerToken, storeId, productId, 1);
                bridge.testGuest_BuyCart(buyerToken, 1);
            } catch (Exception e) {
                System.out.println("Buyer failed: " + e.getMessage());
            }
        });

        Thread owner = new Thread(() -> {
            try {
                Thread.sleep(50);
                bridge.testOwner_ManageInventory_RemoveProduct(storeId, ownerToken, productId);
            } catch (Exception e) {
                System.out.println("Owner failed: " + e.getMessage());
            }
        });

        buyer.start();
        owner.start();
        buyer.join();
        owner.join();
    }

    /////////////////////////////
    @Test
    public void testConcurrentAssignSameManager() throws Exception {
        String owner1 = bridge.testGuest_Enter();
        String owner2 = bridge.testGuest_Enter();
        String manager = bridge.testGuest_Enter();

        bridge.testGuest_Register(owner1, "o1", "pass", 25);
        bridge.testGuest_Register(owner2, "o2", "pass", 25);
        bridge.testGuest_Register(manager, "manager", "pass", 25);

        bridge.testUser_LogIn(owner1, "o1", "pass");
        bridge.testUser_LogIn(owner2, "o2", "pass");
        bridge.testUser_LogIn(manager, "manager", "pass");

        bridge.testUser_OpenStore(owner1, "Owner1Shop", "Books");
        int storeId = 1;
        int managerId = 3; // assuming 3rd user

        Thread assign1 = new Thread(() -> {
            try {
                bridge.testOwner_AssignManager(owner1, storeId, managerId);
            } catch (Exception e) {
                System.out.println("Owner1 failed: " + e.getMessage());
            }
        });

        Thread assign2 = new Thread(() -> {
            try {
                bridge.testOwner_AssignManager(owner2, storeId, managerId);
            } catch (Exception e) {
                System.out.println("Owner2 failed: " + e.getMessage());
            }
        });

        assign1.start();
        assign2.start();
        assign1.join();
        assign2.join();
    }
}
