package workshop.demo.Controllers.DataInitilizer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import workshop.demo.ApplicationLayer.ActivePurchasesService;
import workshop.demo.ApplicationLayer.AppSettingsService;
import workshop.demo.ApplicationLayer.DatabaseCleaner;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.InfrastructureLayer.AppSettingsRepository;
import workshop.demo.InfrastructureLayer.UserJpaRepository;

public class ManagerDataInit {
    protected static final Logger logger = LoggerFactory.getLogger(InitDataService.class);
    protected static String output = new String();
    protected static boolean error;
    protected static int line;

    // maps for identfing variables!
    protected static Map<String, Integer> ids = new HashMap<>();
    // protected static Map<String, Integer> storeids = new HashMap<>();
    protected static Map<String, String> tokens = new HashMap<>();


    // Services:
    @Autowired
    protected UserService userService;
    @Autowired
    protected IAuthRepo authRepo;
    @Autowired
    protected StoreService storeService;
    @Autowired
    protected StockService stockService;
    @Autowired
    protected UserSuspensionService suspensionService;
    @Autowired
    protected AppSettingsService settings;
    @Autowired
    protected DatabaseCleaner dataBase;
    @Autowired
    protected AppSettingsRepository appSettingsRepository;
    @Autowired
    protected ActivePurchasesService activeService;
    @Autowired
    protected PurchaseService purchaseService;
    @Autowired
    protected UserJpaRepository regJpaRepo;



    protected static void log(String toLog) {
        logger.info("line "+line+":"+toLog);
        output += ("line "+line+":"+toLog + "\n");
    }

    protected static String getTokenForUserName(String string) {
        if (!tokens.containsKey(string)) {
            log("there is no user with " + string + " , you must login with this user from B-Script");
            error = true;
            return null;
        }
        return tokens.get(string);
    }

    protected  int getStoreIdByName(String storeName) {
        for (StoreDTO iterable_element : storeService.getAllStores()) {
            if (storeName.equals(iterable_element.storeName))
                return iterable_element.storeId;
        }
        return -1;
    }

    protected ItemStoreDTO getProductByNameAndStore(int id, String productName,String userToken,String storeName) throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(productName, null, null, id, null, null,
                null, null);

        ItemStoreDTO[] items = stockService.searchProductsOnAllSystem(userToken, criteria);
        if (items.length == 0) {
            log("product " + productName + " not found on store " + storeName);
            error = true;
            return null;
        }
        return items[0];
    }

}
