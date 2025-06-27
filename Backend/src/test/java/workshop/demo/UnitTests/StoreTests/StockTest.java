package workshop.demo.UnitTests.StoreTests;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DTOs.Category;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.item;

//@SpringBootTest
@ActiveProfiles("test")
public class StockTest {

    private StoreStock store;

    @BeforeEach
    public void setUp() {
        store = new StoreStock(1);
    }
    //test remove not threaded:

    @Test
    public void testRemoveItem() throws Exception {
        item newItem = new item(1, 10, 100, Category.Electronics);
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
    // @Test
    // public void testRankProductWithoutConcurrency() throws UIException {
    //     item testItem = new item(1, 10, 100, Category.Electronics);
    //     testItem.setRank(new AtomicInteger[5]);
    //     for (int i = 0; i < 5; i++) {
    //         testItem.getRank()[i] = new AtomicInteger(0);
    //     }
    //     store.addItem(testItem);
    //     store.rankProduct(1, 1);
    //     store.rankProduct(1, 2);
    //     store.rankProduct(1, 3);
    //     item rankedItem = store.getItemByProductId(1);
    //     assertNotNull(rankedItem, "Item should not be null");
    //     AtomicInteger[] ranks = rankedItem.getRank();
    //     assertEquals(1, ranks[0].get(), "The rank count for rank 1 should be 1.");
    //     assertEquals(1, ranks[1].get(), "The rank count for rank 2 should be 1.");
    //     assertEquals(1, ranks[2].get(), "The rank count for rank 3 should be 1.");
    //     // // Now, check the final rank of the product -> i GHANGE IT 
    //     // int finalRank = rankedItem.getFinalRank();
    //     // assertEquals(6, finalRank, "The final rank should be calculated correctly as 6.");
    // }
    @Test
    void testRankingUpdatesAndFinalRank() {
        item itemUnderTest = new item(1, 10, 100, Category.Beauty);
        // Give 3 votes for rank 5
        itemUnderTest.rankItem(5);
        itemUnderTest.rankItem(5);
        itemUnderTest.rankItem(5);
        // Give 2 votes for rank 3
        itemUnderTest.rankItem(3);
        itemUnderTest.rankItem(3);
        // Give 1 vote for rank 1
        itemUnderTest.rankItem(1);
        AtomicInteger[] ranks = itemUnderTest.getRank();
        // Check individual counts
        assertEquals(1, ranks[0].get(), "Rank 1 count should be 1");
        assertEquals(0, ranks[1].get(), "Rank 2 count should be 0");
        assertEquals(2, ranks[2].get(), "Rank 3 count should be 2");
        assertEquals(0, ranks[3].get(), "Rank 4 count should be 0");
        assertEquals(3, ranks[4].get(), "Rank 5 count should be 3");
        // Calculate expected final rank:
        // Total votes: 1+2+3=6
        // Weighted sum: 1*1 + 2*3 + 3*5 = 1 + 6 + 15 = 22
        // avg = round(22/6)= 4
        assertEquals(4, itemUnderTest.getFinalRank(), "Final rank should be 4");
    }
}
