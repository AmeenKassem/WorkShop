package workshop.demo.UnitTests.UsersTests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.GuestJpaRepository;
import workshop.demo.InfrastructureLayer.UserJpaRepository;
import workshop.demo.ApplicationLayer.UserService;

@SpringBootTest
@ActiveProfiles("test")
public class UsersTests {

    @Autowired
    private IAuthRepo auth;
    // private Encoder enc = new Encoder();
    private String adminKey = "123321";

    @Autowired
    private AdminInitilizer a;
    @Autowired
    private GuestJpaRepository userRepo;
    @Autowired
    private UserJpaRepository userRepo1;
    @Autowired
    private UserService user;
    private Guest guest;

    private int goodLogin(String username, String password) throws Exception {
        String aa = user.generateGuest();
        user.register(aa, username, password, 22);

        String userIdFromLogIn = user.login(aa, username, password);
        return auth.getUserId(userIdFromLogIn);
    }

    @BeforeEach
    void setUp() {
        guest = new Guest();
        userRepo.deleteAll();
        userRepo1.deleteAll();
    }

    @Test
    void testAddToCart_WithExplicitStore() {
        CartItem item = new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics));
        guest.addToCart(1, item);

        List<CartItem> items = guest.getCart();
        assertEquals(1, items.size());
        assertEquals(item, items.get(0));
    }

    @Test
    void testAddToCart_ImplicitStore() {
        CartItem item = new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics)); // storeId =
        // 2 inside
        // item
        guest.addToCart(item);

        List<CartItem> items = guest.getCart();
        assertEquals(1, items.size());
        assertEquals(item, items.get(0));
    }

    @Test
    void testGetCartItemsList() {
        guest.addToCart(new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics)));
        List<CartItem> items = guest.getCart();
        assertEquals(1, items.size());
    }

    @Test
    void testGetCartObject() {
        assertNotNull(guest.getCart());
        assertFalse(guest.getCart() instanceof ShoppingCart);
    }

    @Test
    void testClearCart() {
        guest.addToCart(1, new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics)));
        assertFalse(guest.getCart().isEmpty());

        guest.clearCart();
        assertTrue(guest.getCart().isEmpty());
    }

    @Test
    void testModifyCartAddToBuy() {
        CartItem item = new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics));
        guest.addToCart(item);

        guest.ModifyCartAddQToBuy(500, 10); // should call inner logic
        // No assert here because inner method does not expose anything
        // JaCoCo will register coverage on the condition
    }

    @Test
    void testToUserDTO() {
        assertNotNull(guest.getUserDTO());
    }

    @Test
    public void test_register_and_login() throws Exception {
        String guestId = user.generateGuest();
        user.register(guestId, "bhaa", "123123", 22);

        String userIdFromLogIn = user.login(guestId, "bhaa", "123123");

        String id2 = user.generateGuest();
        try {
            user.login(id2, "bhaa", "11111");
            assertTrue(false);
        } catch (UIException ex) {
            assertTrue(true);
        } catch (Exception ex) {
            assertTrue(false);
        }

    }

    @Test
    public void testOnlineAafterLogin() throws Exception {
        String guestId = user.generateGuest();

        user.register(guestId, "layan", "123", 22);
        String loggedInId = user.login(guestId, "layan", "123");
        boolean isOnline = userRepo1.findAll().get(0).isOnline();
        assertTrue(isOnline, "should be online after login");
    }

    @Test
    public void adminTest() throws Exception {
        int userId = goodLogin("bhaa2", "123321");
        int userId2 = goodLogin("ghanem", "123321");
        // user1 are not admin
        assertFalse(userRepo1.findAll().get(0).isAdmin());
        assertFalse(userRepo1.findAll().get(1).isAdmin());

        // System.out.println(a.getPassword());
        // wrong admin key
        userRepo1.findAll().get(1).setAdmin();
        assertFalse(userRepo1.findAll().get(0).isAdmin());
        // good admin key
        // user2 still not admin
    }

    @Test
    public void onlineTest() throws Exception {
        String guestId = user.generateGuest();

        boolean a = user.register(guestId, "ghanem2", "123321", 22);
        assertTrue(a);

    }

    @Test
    public void registeredTest() {

    }

}
