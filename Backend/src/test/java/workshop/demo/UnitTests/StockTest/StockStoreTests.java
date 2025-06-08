package workshop.demo.UnitTests.StockTest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StockStoreTests {



    @Test
    void test_item_changeQuantity_shouldUpdateValue() {
        item i = new item(1, 10, 150, Category.ELECTRONICS);
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
        item i = new item(1, 10, 150, Category.ELECTRONICS);
        i.rankItem(4);
        i.rankItem(5);
        assertEquals(5, i.getFinalRank(), 0.01);
    }

    @Test
    void test_item_containsCategory_trueAndFalseCases() {
        item i = new item(1, 10, 150, Category.Sports);
        assertEquals(i.getCategory().toString(),"Sports");
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
        assertNull(ap.getRandomCardIfWinner(777, 20)); // ID 777 does not exist
    }




}
