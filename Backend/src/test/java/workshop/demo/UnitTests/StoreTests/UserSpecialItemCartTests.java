package workshop.demo.UnitTests.StoreTests;

import org.junit.jupiter.api.Test;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DTOs.SpecialType;

import static org.junit.jupiter.api.Assertions.*;

public class UserSpecialItemCartTests {

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
        assertFalse(c1.equals(c2)); // Line 24-27
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
}
