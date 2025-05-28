package workshop.demo.ApplicationLayer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.SystemAnalyticsDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

@Service
public class AdminHandler {

    private final IOrderRepo orderRepo;
    private final IStoreRepo storeRepo;
    private final IUserRepo userRepo;
    private final IAuthRepo authRepo;
    private final List<LocalDateTime> loginEvents = new ArrayList<>();
    private final List<LocalDateTime> logoutEvents = new ArrayList<>();
    private final List<LocalDateTime> registerEvents = new ArrayList<>();

    @Autowired
    public AdminHandler(IOrderRepo orderRepo, IStoreRepo storeRepo, IUserRepo userRepo, IAuthRepo authRepo) {
        this.orderRepo = orderRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        //you can inialize the admin here:
        try {
            initAdmin();
        } catch (UIException e) {
            // TODO Auto-generated catch block
            //here must do something
            e.printStackTrace();
        }

    }

    private void initAdmin() throws UIException {

        this.userRepo.registerUser("admin", "Admin123", 23);
    }

    public List<PurchaseHistoryDTO> viewPurchaseHistory(String adminToken) throws Exception {
        if (!authRepo.validToken(adminToken)) {
            throw new UIException("Invalid Token!", ErrorCodes.INVALID_TOKEN);
        }
        int adminUserId = authRepo.getUserId(adminToken);
        userRepo.checkAdmin_ThrowException(adminUserId);
        List<OrderDTO> allOrders = orderRepo.getAllOrdersInSystem();
        List<PurchaseHistoryDTO> historyEntries = new ArrayList<>();
        for (OrderDTO order : allOrders) {
            int buyerId = order.getUserId();
            int storeId = order.getStoreId();
            UserDTO buyerData = userRepo.getUserDTO(buyerId);
            StoreDTO storeData = storeRepo.getStoreDTO(storeId);
            String buyerName = (buyerData != null ? buyerData.username : "Unknown buyer");
            String storeName = (storeData != null ? storeData.storeName : "Unknown store");
            List<ReceiptProduct> items = order.getProductsList();
            String timeStamp = order.getDate();
            double totalPrice = order.getFinalPrice();
            PurchaseHistoryDTO record = new PurchaseHistoryDTO(buyerName, storeName, items, timeStamp, totalPrice);
            historyEntries.add(record);
        }
        return historyEntries;
    }

    public void recordLoginEvent() {
        loginEvents.add(LocalDateTime.now());
    }

    public void recordLogoutEvent() {
        logoutEvents.add(LocalDateTime.now());
    }

    public void recordRegisterEvent() {
        registerEvents.add(LocalDateTime.now());
    }

    public SystemAnalyticsDTO getSystemAnalytics(String adminToken) throws Exception {
        if (!authRepo.validToken(adminToken)) {
            throw new UIException("Invalid Token!", ErrorCodes.INVALID_TOKEN);
        }
        int adminUserId = authRepo.getUserId(adminToken);
        userRepo.checkAdmin_ThrowException(adminUserId);
        //WTF is this
        Function<List<LocalDateTime>, Map<LocalDate, Integer>> countPerDay = (events) -> {
            return events.stream().collect(Collectors.groupingBy(
                    dt -> dt.toLocalDate(), // group by date (year-month-day)
                    Collectors.reducing(0, e -> 1, Integer::sum) // count occurrences
            ));
        };
        Map<LocalDate, Integer> loginsPerDay = countPerDay.apply(loginEvents);
        Map<LocalDate, Integer> logoutsPerDay = countPerDay.apply(logoutEvents);
        Map<LocalDate, Integer> registerPerDay = countPerDay.apply(registerEvents);
        List<OrderDTO> allOrders = orderRepo.getAllOrdersInSystem();
        Map<LocalDate, Integer> purchasesPerDay = new HashMap<>();
        for (OrderDTO order : allOrders) {
            LocalDate date;
            try {
                date = LocalDate.parse(order.getDate().substring(0, 10));
            } catch (Exception e) {
                try {
                    date = LocalDate.parse(order.getDate());
                } catch (Exception ex) {
                    continue;
                }
            }
            purchasesPerDay.merge(date, 1, Integer::sum);
        }
        return new SystemAnalyticsDTO(loginsPerDay, logoutsPerDay, registerPerDay, purchasesPerDay);
    }
}
