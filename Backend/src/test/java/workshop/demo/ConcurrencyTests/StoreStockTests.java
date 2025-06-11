package workshop.demo.ConcurrencyTests;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.DTOs.Category;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.item;

@SpringBootTest
public class StoreStockTests {

    private StoreStock store;

    @BeforeEach
    public void setUp() {
        store = new StoreStock(1);
    }

    @Test
    public void testAddItemConcurrency_addingSameItemOnceThenIncreaseQuantity_Sucess() throws InterruptedException, Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // Create an item to add
        item newItem = new item(1, 10, 100, Category.Electronics);  // Set category
        store.addItem(newItem);  // Add the item to the store initially
        int numberOfThreads = 100;
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> store.addItem(newItem));
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        // Verify that the quantity has been correctly updated
        List<item> items = store.getItemsByCategoryObject(Category.Electronics);
        assertNotNull(items, "Items list should not be null");
        assertEquals(1, items.size(), "There should be exactly one item with productId 1");
        item storedItem = items.get(0);
        assertEquals(10 + numberOfThreads, storedItem.getQuantity(), "The quantity should match the expected value after concurrent additions.");
    }

    //here to adding the quantity:(same item)
    @Test
    public void testUpdateQuantityConcurrency_Success() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        item newItem = new item(1, 10, 100, Category.Electronics);
        store.addItem(newItem);  // Add initial item to the store

        int numberOfThreads = 100;

        // Submit tasks to executor service
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> store.addItem(newItem));
        }

        // Shutdown executor service and wait for tasks to complete (after submitting all tasks)
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Retrieve items and check the quantity
        List<item> items = store.getItemsByCategoryObject(Category.Electronics);
        assertNotNull(items, "Items list should not be null");
        assertEquals(1, items.size(), "There should be exactly one item with productId 1");

        item storedItem = items.get(0);
        // Check that the quantity is correctly updated (initial quantity + number of threads)
        assertEquals(10 + numberOfThreads, storedItem.getQuantity(), "The quantity of the item should match the expected value after concurrent updates.");
    }

    @Test
    public void testRemoveItemSuccessfullyInStockInStoreX() throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        item newItem = new item(1, 10, 100, Category.Electronics);
        store.addItem(newItem);
        int numberOfThreads = 2;
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    store.removeItem(1);
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }

            });
        }
        // Shutdown executor service and wait for tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // After remove, quantity should be 0
        assertEquals(0, newItem.getQuantity(), "Quantity should be 0 after concurrent removeItem calls");
    }

    //I think this is a stuipd test:
    @Test
    public void testUpdatePriceForProductInStoreX_Concurrency() throws InterruptedException {
        item newItem = new item(1, 10, 100, Category.Electronics);
        store.addItem(newItem);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int numberOfThreads = 100;

        // Submit tasks to the executor to update the price of the same item concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            int newPrice = 200;  // New price to set
            executorService.submit(() -> {
                try {
                    store.updatePrice(1, newPrice);  // Update price for the item with productId 1
                } catch (Exception e) {
                    fail("Exception occurred during price update: " + e.getMessage());
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        item updatedItem = store.getItemByProductId(1);
        assertNotNull(updatedItem, "Item should not be null");
        assertEquals(200, updatedItem.getPrice(), "The price should be updated to the expected value.");
    }

    @Test
    public void testRankProductInStoreX_Concurrency() throws InterruptedException {
        // Create a sample item with rank array of length 5 (for ranks 1 to 5)
        item testItem = new item(1, 10, 100, workshop.demo.DTOs.Category.Electronics);
        testItem.setRank(new AtomicInteger[5]);  // Assuming ranks from 1 to 5
        for (int i = 0; i < 5; i++) {
            testItem.getRank()[i] = new AtomicInteger(0);
        }

        store.addItem(testItem);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int numberOfThreads = 100;

        for (int i = 0; i < numberOfThreads; i++) {
            int rank = 3;
            executorService.submit(() -> {
                try {
                    store.rankProduct(1, rank);  // Rank product with productId 1
                } catch (IllegalArgumentException | UIException e) {
                    fail("Exception occurred during ranking: " + e.getMessage());
                }
            });
        }

        // Shutdown executor service and wait for tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // After all threads have completed, check if the rank is correctly updated
        item rankedItem = store.getItemByProductId(1);
        assertNotNull(rankedItem, "Item should not be null");

        // The rank at index 2 (rank 3) should have been incremented 100 times (since 100 threads tried to rank it 3)
        AtomicInteger[] ranks = rankedItem.getRank();
        assertEquals(100, ranks[2].get(), "The rank count for rank 3 should be 100.");
    }

    @Test
    public void testConcurrentPurchaseOfLastItem_onlyOneSucceeds() throws InterruptedException {
        item newItem = new item(1, 1, 100, Category.Electronics); // Quantity = 1
        store.addItem(newItem);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                try {
                    store.decreaseQuantitytoBuy(1, 1);
                    successCount.incrementAndGet();
                } catch (UIException e) {
                    failureCount.incrementAndGet();
                    System.out.println("Failed to buy: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Only one thread should succeed in buying the last item");
        assertEquals(1, failureCount.get(), "One thread should fail due to insufficient stock");

        item finalItem = store.getItemByProductId(1);
        assertNotNull(finalItem);
        assertEquals(0, finalItem.getQuantity(), "Final item quantity should be zero");
    }

}
