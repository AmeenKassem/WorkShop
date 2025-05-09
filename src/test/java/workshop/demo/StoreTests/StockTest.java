package workshop.demo.StoreTests;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import workshop.demo.DTOs.Category;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;

public class StockTest {

    private Store store;

    @BeforeEach
    public void setUp() {
        store = new Store(1, "aa", "bb");
    }
    //test remove not threaded:

    @Test
    public void testRemoveItem() throws Exception {
        item newItem = new item(1, 10, 100, Category.ELECTRONICS);
        store.addItem(newItem);
        System.out.println("addedddd");
        System.out.println(store.getStock().size());
        try {
            System.out.println(store.getItemByProductId(1).getCategory());
            store.removeItem(1);

            // Retrieve the item after removal
            item removedItem = store.getItemByProductId(1);

            // Check that the item's quantity has been set to 0
            assertNotNull(removedItem, "The item should still exist after removal.");
            assertEquals(0, removedItem.getQuantity(), "The quantity of the item should be 0 after removal.");

        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

    }

    //Test "functionname" when "something " then "Succes/faliure"
    @Test
    public void testRankProductWithoutConcurrency() throws UIException {
        item testItem = new item(1, 10, 100, Category.ELECTRONICS);
        testItem.setRank(new AtomicInteger[5]);
        for (int i = 0; i < 5; i++) {
            testItem.getRank()[i] = new AtomicInteger(0);
        }
        store.addItem(testItem);
        store.rankProduct(1, 1);
        store.rankProduct(1, 2);
        store.rankProduct(1, 3);

        item rankedItem = store.getItemByProductId(1);
        assertNotNull(rankedItem, "Item should not be null");
        AtomicInteger[] ranks = rankedItem.getRank();
        assertEquals(1, ranks[0].get(), "The rank count for rank 1 should be 1.");
        assertEquals(1, ranks[1].get(), "The rank count for rank 2 should be 1.");
        assertEquals(1, ranks[2].get(), "The rank count for rank 3 should be 1.");

        // // Now, check the final rank of the product -> i GHANGE IT 
        // int finalRank = rankedItem.getFinalRank();
        // assertEquals(6, finalRank, "The final rank should be calculated correctly as 6.");
    }

}
