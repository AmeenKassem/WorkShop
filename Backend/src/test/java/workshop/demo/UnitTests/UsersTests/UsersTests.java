// package workshop.demo.UnitTests.UsersTests;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;

// import workshop.demo.DTOs.Category;
// import workshop.demo.DTOs.ItemCartDTO;
// import workshop.demo.DomainLayer.Authentication.IAuthRepo;
// import workshop.demo.DomainLayer.Exceptions.UIException;
// import workshop.demo.DomainLayer.User.AdminInitilizer;
// import workshop.demo.DomainLayer.User.CartItem;
// import workshop.demo.DomainLayer.User.Guest;
// import workshop.demo.DomainLayer.User.IUserRepo;
// import workshop.demo.DomainLayer.User.ShoppingCart;
// import workshop.demo.InfrastructureLayer.AuthenticationRepo;
// import workshop.demo.InfrastructureLayer.Encoder;
// import workshop.demo.InfrastructureLayer.UserRepository;

// @SpringBootTest
// @ActiveProfiles("test")
// public class UsersTests {

//     private IAuthRepo auth = new AuthenticationRepo();
//     // private Encoder enc = new Encoder();
//     private String adminKey = "123321";

//     @Autowired
//     private AdminInitilizer a;

//     private IUserRepo userRepo = new UserRepository(enc, a);
//     private Guest guest;

//     private int goodLogin(String username, String password) throws UIException {
//         int userIdFromRegister = userRepo.registerUser(username, password, 22);

//         int userIdFromLogIn = userRepo.login(username, password);
//         return userIdFromLogIn;
//     }

//     @BeforeEach
//     void setUp() {
//         guest = new Guest(42);
//     }

//     @Test
//     void testGetId() {
//         assertEquals(42, guest.getId());
//     }

//     @Test
//     void testAddToCart_WithExplicitStore() {
//         CartItem item = new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics));
//         guest.addToCart(1, item);

//         List<CartItem> items = guest.getCart();
//         assertEquals(1, items.size());
//         assertEquals(item, items.get(0));
//     }

//     @Test
//     void testAddToCart_ImplicitStore() {
//         CartItem item = new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics)); // storeId = 2 inside item
//         guest.addToCart(item);

//         List<CartItem> items = guest.getCart();
//         assertEquals(1, items.size());
//         assertEquals(item, items.get(0));
//     }

//     @Test
//     void testGetCartItemsList() {
//         guest.addToCart(new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics)));
//         List<CartItem> items = guest.getCart();
//         assertEquals(1, items.size());
//     }

//     @Test
//     void testGetCartObject() {
//         assertNotNull(guest.getCart());
//         assertFalse(guest.getCart() instanceof ShoppingCart);
//     }

//     @Test
//     void testClearCart() {
//         guest.addToCart(1, new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics)));
//         assertFalse(guest.getCart().isEmpty());

//         guest.clearCart();
//         assertTrue(guest.getCart().isEmpty());
//     }

//     @Test
//     void testModifyCartAddToBuy() {
//         CartItem item = new CartItem(new ItemCartDTO(1, 1, 1, 1, "phone", "store", Category.Electronics));
//         guest.addToCart(item);

//         guest.ModifyCartAddQToBuy(500, 10); // should call inner logic
//         // No assert here because inner method does not expose anything
//         // JaCoCo will register coverage on the condition
//     }

//     @Test
//     void testRemoveItem() {
//         CartItem item = new CartItem(new ItemCartDTO(1, 600, 1, 1, "phone", "store", Category.Electronics));
//         guest.addToCart(item);

//         guest.removeItem(600);

//         assertTrue(guest.getCart().isEmpty());
//     }

//     @Test
//     void testToUserDTO() {
//         assertNotNull(guest.getUserDTO());
//         assertEquals(42, guest.getUserDTO().id);
//     }

//     @Test
//     public void test_register_and_login() throws UIException {
//         int guestId = userRepo.generateGuest();
//         int userIdFromRegister = userRepo.registerUser("bhaa", "123123", 22);

//         int userIdFromLogIn = userRepo.login("bhaa", "123123");

//         assertEquals(userIdFromRegister, userIdFromLogIn);

//         int id2 = userRepo.generateGuest();
//         try {
//             userRepo.login("bhaa", "11111");
//             assertTrue(false);
//         } catch (UIException ex) {
//             assertTrue(true);
//         } catch (Exception ex) {
//             assertTrue(false);
//         }

//     }

//     @Test
//     public void testOnlineAafterLogin() throws UIException {
//         int registeredId = userRepo.registerUser("layan", "123", 22);
//         int loggedInId = userRepo.login("layan", "123");
//         assertEquals(registeredId, loggedInId);
//         boolean isOnline = userRepo.isOnline(loggedInId);
//         assertTrue(isOnline, "should be online after login");
//     }

//     // @Test
//     // public void adminTest(){
//     //     int userId = goodLogin("bhaa2", "123321");
//     //     int userId2 = goodLogin("ghanem", "123321");
//     //     //user1 are not admin
//     //     Assertions.assertFalse(userRepo.isAdmin(userId));
//     //     // System.out.println(a.getPassword());
//     //     //wrong admin key
//     //     Assertions.assertFalse(userRepo.setUserAsAdmin(userId, adminKey+"2222"));
//     //     Assertions.assertFalse(userRepo.isAdmin(userId));
//     //     //good admin key
//     //     Assertions.assertTrue(userRepo.setUserAsAdmin(userId, adminKey));
//     //     Assertions.assertTrue(userRepo.isAdmin(userId));
//     //     //user2 still not admin
//     //     Assertions.assertFalse((userRepo.isAdmin(userId2)));
//     // }
//     @Test
//     public void onlineTest() throws UIException {
//         userRepo.registerUser("ghanem2", "123321", 22);

//     }

//     @Test
//     public void registeredTest() {

//     }

// }
