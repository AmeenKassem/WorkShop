// package workshop.demo.InfrastructureLayer;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.function.Function;
// import java.util.logging.Level;
// import java.util.logging.Logger;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Example;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
// import org.springframework.stereotype.Repository;

// import workshop.demo.DTOs.ItemCartDTO;
// import workshop.demo.DTOs.ParticipationInRandomDTO;
// import workshop.demo.DTOs.SpecialType;
// import workshop.demo.DTOs.UserDTO;
// import workshop.demo.DTOs.UserSpecialItemCart;
// import workshop.demo.DomainLayer.Exceptions.DevException;
// import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
// import workshop.demo.DomainLayer.Exceptions.UIException;
// import workshop.demo.DomainLayer.Stock.SingleBid;
// import workshop.demo.DomainLayer.User.AdminInitilizer;
// import workshop.demo.DomainLayer.User.CartItem;
// import workshop.demo.DomainLayer.User.Guest;
// import workshop.demo.DomainLayer.User.IUserRepo;
// import workshop.demo.DomainLayer.User.Registered;
// import workshop.demo.DomainLayer.User.ShoppingCart;
// import workshop.demo.DomainLayer.User.SpecialCart;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;
// @Repository
// public class UserRepository  {

//     private AtomicInteger idGen;
//     private ConcurrentHashMap<Integer, Guest> guests; // id -> Guest
//     private ConcurrentHashMap<String, Registered> users; // username -> Registered
//     private ConcurrentHashMap<Integer, String> idToUsername; // id -> username
//     private Encoder encoder;
//     private AdminInitilizer adminInit;
//     private IUserRepoDB guestDb;
//     private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

//     // @Autowired
//     public UserRepository(Encoder encoder, AdminInitilizer adminInit,IUserRepoDB db) {
//         this.encoder = encoder;
//         this.idGen = new AtomicInteger(1);
//         this.adminInit = adminInit;
//         users = new ConcurrentHashMap<>();
//         guests = new ConcurrentHashMap<>();
//         idToUsername = new ConcurrentHashMap<>();
//         guestDb = db;
//     }

//     public List<String> getAllUsernames() {
//         return new ArrayList<>(users.keySet());
//     }

//     @Override
//     public int logoutUser(String username) throws UIException {
//         if (userExist(username)) {
//             Registered user = users.get(username);
//             user.logout();
//             logger.log(Level.INFO, "User logged out: {0}", username);
//             return generateGuest();
//         } else {
//             logger.log(Level.WARNING, "User not found: {0}", username);
//             throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
//         }
//     }

//     @Override
//     public int registerUser(String username, String password, int age) throws UIException {
//         if (userExist(username)) {

//             throw new UIException("another user try to register with used username", ErrorCodes.USERNAME_USED);
//         }
//         //user<-
//         String encPass = encoder.encodePassword(password);
//         int id = idGen.getAndIncrement();
//         //user<-
//         Registered userToAdd = new Registered(id, username, encPass, age);
//         users.put(username, userToAdd);
//         idToUsername.put(id, username);
//         logger.log(Level.INFO, "User {0} registered successfully", username);
//         System.out.println(guestDb.count()+"  count on db.");
//         return id;

//     }

//     @Override
//     public int generateGuest() {
//         int id = idGen.getAndIncrement();
//         Guest newGuest = new Guest(id);
//         guests.put(id, newGuest);
//         logger.log(Level.INFO, "INIT DATAAAAAA "+ (guestDb==null));
//         logger.log(Level.INFO,"geust generated!");
//         guestDb.save(newGuest);
//         logger.log(Level.INFO,"geust persisted!");
//         return id;
//     }

//     @Override
//     public int login(String username, String password) throws UIException {
//         if (userExist(username)) {
//             Registered user = users.get(username);
//             if (user.check(encoder, username, password)) {
//                 logger.log(Level.INFO, "User logged in: {0}", username);
//                 return user.getId();
//             } else {
//                 logger.log(Level.WARNING, "Invalid password for user: {0}", username);
//                 throw new UIException("Incorrect username or password.", ErrorCodes.WRONG_PASSWORD);
//             }
//         } else {
//             throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
//         }
//     }

//     private boolean userExist(String username) {
//         return users.containsKey(username);
//     }

//     private boolean userExist(int userid) {
//         return idToUsername.containsKey(userid);
//     }
// // changed from private to public

//     public boolean guestExist(int id) {
//         return guests.containsKey(id);
//     }
// // changed it to handle users as well
// // added fucntion userexists that takes userid and returns name

//     @Override
//     public void addItemToGeustCart(int guestId, ItemCartDTO item) throws UIException {
//         CartItem itemCart = new CartItem(item);
//         if (guestExist(guestId)) {
//             Guest geust = guests.get(guestId);
//             geust.addToCart(itemCart);
//             logger.log(Level.INFO, "Item added to guest cart: {0} for guest id: {1}", new Object[]{item.getProductId(), guestId});
//         } else if (userExist(guestId)) {
//             getRegisteredUser(guestId).addToCart(itemCart);
//             logger.log(Level.INFO, "Item added to guest cart: {0} for guest id: {1}", new Object[]{item.getProductId(), guestId});
//         } else {
//             throw new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND);

//         }
//     }

//     @Override
//     public void ModifyCartAddQToBuy(int guestId, int productId, int quantity) throws UIException {
//         if (guestExist(guestId)) {
//             Guest geust = guests.get(guestId);
//             geust.ModifyCartAddQToBuy(productId, quantity);
//             logger.log(Level.INFO, "Item modified in guest cart: {0} for guest id: {1}", new Object[]{productId, guestId});
//         } else if (userExist(guestId)) {
//             getRegisteredUser(guestId).ModifyCartAddQToBuy(productId, quantity);
//             logger.log(Level.INFO, "Item modified in guest cart: {0} for guest id: {1}", new Object[]{productId, guestId});
//         } else {
//             throw new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND);

//         }
//     }

//     @Override
//     public void destroyGuest(int id) {
//         guests.remove(id);
//         logger.log(Level.INFO, "guest destroyed: {0}", id);
//     }

//     @Override
//     public boolean isAdmin(int id) throws UIException {
//         Registered registered = getRegisteredUser(id);
//         return registered != null && registered.isAdmin();
//     }

//     @Override
//     public boolean isRegistered(int id) throws UIException {
//         return getRegisteredUser(id) != null;
//     }

//     @Override
//     public boolean isOnline(int id) throws UIException {
//         Registered registered = getRegisteredUser(id);
//         return registered != null && registered.isOnline();
//     }

//     @Override
//     public Registered getRegisteredUser(int id) throws UIException {
//         if (idToUsername.containsKey(id)) {
//             String username = idToUsername.get(id);
//             if (users.containsKey(username)) {
//                 return users.get(username);
//             } else {
//                 throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
//             }
//         }
//         return null;
//     }

//     @Override
//     public boolean setUserAsAdmin(int id, String adminKey) throws UIException {
//         Registered registered = getRegisteredUser(id);
//         if (registered != null) {
//             if (adminInit.matchPassword(adminKey)) {
//                 registered.setAdmin();
//                 logger.log(Level.INFO, "User {0} is now an admin.", registered.getUsername());
//                 return true;
//             }
//         }
//         return false;
//     }

//     @Override
//     public void removeItemFromGeustCart(int guestId, int productId) throws UIException {
//         if (guestExist(guestId)) {
//             Guest geust = guests.get(guestId);
//             geust.removeItem(productId);
//             logger.log(Level.INFO, "Item removed from guest cart: {0} for guest id: {1}", new Object[]{productId, guestId});
//         } else if (userExist(guestId)) {
//             getRegisteredUser(guestId).removeItem(productId);
//             logger.log(Level.INFO, "Item removed from guest cart: {0} for guest id: {1}", new Object[]{productId, guestId});
//         } else {
//             throw new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND);

//         }
//     }

//     @Override
//     public ShoppingCart getUserCart(int userId) throws UIException {
//         if (guests.containsKey(userId)) {
//             return guests.get(userId).geCart();
//         }
//         Registered registered = getRegisteredUser(userId);
//         if (registered != null) {
//             return registered.geCart();
//         }
//         throw new UIException("User with ID " + userId + " not found", ErrorCodes.USER_NOT_FOUND);
//     }

//   //  @Override
// //    public List<ItemCartDTO> getCartForUser(int ownerId) {
// //      //  throw new UnsupportedOperationException("Unimplemented method 'getCartForUser'");
// //    }

//     @Override
//     public void checkUserRegisterOnline_ThrowException(int userId) throws UIException {
//         if (!(isRegistered(userId) && isOnline(userId))) {
//             // logger.error("User not logged in for setProductToBid: {}", userId);
//             throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
//         }
//     }

//     @Override
//     public void checkAdmin_ThrowException(int userId) throws UIException {
//         checkUserRegisterOnline_ThrowException(userId);
//         if (!isAdmin(userId)) {
//             throw new UIException("User is not an admin", ErrorCodes.NO_PERMISSION);
//         }
//     }

//     @Override
//     public void checkUserRegister_ThrowException(int userId) throws UIException {
//         if (!(isRegistered(userId))) {
//             // logger.error("User not logged in for setProductToBid: {}", userId);
//             throw new UIException("You are not regestered user!", ErrorCodes.USER_NOT_LOGGED_IN);
//         }
//     }

//     @Override
//     public void addSpecialItemToCart(UserSpecialItemCart item, int userId) throws DevException, UIException {
//         getRegisteredUser(userId).addSpecialItemToCart(item);
//     }

//     @Override
//     public List<UserSpecialItemCart> getAllSpecialItems(int userId) throws UIException {
//         return getRegisteredUser(userId).getSpecialCart();
//     }

//     @Override
//     public UserDTO getUserDTO(int userId) throws UIException {
//         if (isRegistered(userId)) {
//             logger.log(Level.INFO, "getUserDTO for registered user ID={}", userId);
//             return getRegisteredUser(userId).getUserDTO();
//         } else if (guests.containsKey(userId)) {
//             logger.log(Level.INFO, "getUserDTO for guests user ID={}", userId);
//             return guests.get(userId).getUserDTO();
//         } else {
//             throw new UIException("User not found with ID: " + userId, ErrorCodes.USER_NOT_FOUND);
//         }
//     }

//     @Override
//     public List<UserDTO> getAllUserDTOs() {
//         List<UserDTO> result = new ArrayList<>();
//         for (String username : users.keySet()) {
//             Registered user = users.get(username);
//             result.add(user.getUserDTO());
//         }
//         return result;
//     }

//     public void clear() {
//             guests.clear();

//             users.clear();

//             idToUsername.clear();

//             idGen.set(1); // Reset to starting ID

//     }

//     @Override
//     public Registered getRegisteredUserByName(String name) throws UIException {
//         Registered user = users.get(name);
//         if (user == null) {
//             throw new UIException("No user found with username: " + name, ErrorCodes.USER_NOT_FOUND);
//         }
//         return user;
//     }

//     public void removeSpecialItem(int userId, UserSpecialItemCart itemToRemove) throws UIException {
//         Registered user = getRegisteredUser(userId);

//         user.getSpecialCart().removeIf(item
//                 -> item.storeId == itemToRemove.storeId
//                 && item.specialId == itemToRemove.specialId
//                 && item.bidId == itemToRemove.bidId
//                 && item.type == itemToRemove.type
//         );
//     }

//     public void removeBoughtSpecialItems(int userId, List<SingleBid> winningBids, List<ParticipationInRandomDTO> winningRandoms) throws UIException {
//         Registered user = getRegisteredUser(userId);
//         List<UserSpecialItemCart> cart = user.getSpecialCart();

//         for (SingleBid bid : winningBids) {
//             cart.removeIf(item
//                     -> item.storeId == bid.getStoreId()
//                     && item.specialId == bid.getSpecialId()
//                     && item.bidId == bid.getId()
//                     && item.type == bid.getType()
//             );
//         }

//         for (ParticipationInRandomDTO card : winningRandoms) {
//             cart.removeIf(item
//                     -> item.storeId == card.storeId
//                     && item.specialId == card.randomId
//                     && item.type == SpecialType.Random
//             );
//         }

//         logger.log(Level.INFO, "Removed bought special items from user {}'s cart", userId);
//     }

  

// }
