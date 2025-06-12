package workshop.demo.UnitTests.StockTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DTOs.Category;
import workshop.demo.DomainLayer.Stock.ActivePurcheses;
import workshop.demo.DomainLayer.Stock.item;

@SpringBootTest
@ActiveProfiles("test")
public class StockStoreTests {

    @Test
    void test_item_changeQuantity_shouldUpdateValue() {
        item i = new item(1, 10, 150, Category.Electronics);
        i.changeQuantity(5);
        assertEquals(5, i.getQuantity());
    }

//    @Test
//    void test_item_updatePrice_shouldUpdateValue() {
//        item i = new item(1, 10, 150, Category.ELECTRONICS);
//        i.updatePrice(250);
//        assertEquals(250, i.getPrice());
//    }
    @Test
    void test_item_rankProduct_shouldUpdateFinalRank() {
        item i = new item(1, 10, 150, Category.Electronics);
        i.rankItem(4);
        i.rankItem(5);
        assertEquals(5, i.getFinalRank(), 0.01);
    }

    @Test
    void test_item_containsCategory_trueAndFalseCases() {
        item i = new item(1, 10, 150, Category.Sports);
        assertEquals(i.getCategory().toString(), "Sports");
        assertFalse(i.getCategory().toString().equals("FOOD"));
    }

    @Test
    void test_getProductPrice_randomNotFound_shouldThrowDevException() {
        ActivePurcheses ap = new ActivePurcheses(1);

        Exception ex = assertThrows(Exception.class, () -> {
            ap.getProductPrice(999);
        });

        assertTrue(ex.getMessage().contains("not found in active randoms"));
    }

    @Test
    void test_getRandomCardIfWinner_notExists_shouldReturnNull() {
        ActivePurcheses ap = new ActivePurcheses(1);
        assertNull(ap.getRandomCardforuser(777, 20)); // ID 777 does not exist
    }

}
