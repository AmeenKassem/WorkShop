package workshop.demo.ApplicationLayer.DataInitilizer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.Registered;

// import jakarta.transaction.Transactional;



@Component
public class UserParser extends ManagerDataInit {




    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void user(List<String> construction) {
        List<String> toSend = construction.subList(1, construction.size());
       
        switch (construction.get(0).toLowerCase()) {
            case "reg":
                register(toSend);
                break;
            case "login":
                login(toSend);
                break;
            case "cart":
                cart(toSend);
                break;
            case "auction":
                auction(toSend);
                break;
            case "random":
                random(toSend);
                break;
            case "bid":
                bid(toSend);
                break;
            case "purchase":
                purchase(toSend);
                break;
            case "add-manager":
                offerManager(toSend);
                break;
            case "offer-answer":
                answerOffer(toSend);
                break;
            case "delete-manager":
                deleteManager(toSend);
                break;
            case "add-owner":
                offerOwner(toSend);
                break;
            case "delete-owner":
                deleteOwner(toSend);
                break;
            case "logout":
                logout(toSend);
                break;

            default:
                log("undefined function for user on line " + line + " : " + construction.get(0));
                error = true;
                break;
        }
    }

    private void bid(List<String> toSend) {
        if (toSend.size() != 4) {
            log("syntax error on line " + line
                    + " : params does not match bid <username> <storeName> <productName> <bid>");
            error = true;
            return;
        }
        String token = getTokenForUserName(toSend.get(0));
        if (token == null) {
            return;
        }
        String storeName = toSend.get(1).replace("-", " ");
        int id = getStoreIdByName(storeName);
        String productName = toSend.get(2).replace("-", " ");
        double offer = Double.parseDouble(toSend.get(3));
        try {
            BidDTO[] bidsOnStore = activeService.getAllActiveBids_user(token, id);
            int specialId = -1;
            for (BidDTO bidDTO : bidsOnStore) {
                if (bidDTO.productName.equals(productName))
                    specialId = bidDTO.bidId;
            }
            activeService.addUserBidToBid(token, specialId, id, offer);
            log("user bid placed successfuly!");
        } catch (Exception e) {
            log("Error msg , " + e.getMessage());
        }
    }

    @Transactional
    public void random(List<String> toSend) {
        if (toSend.size() != 4) {
            log("syntax error on line " + line
                    + " : params does not match random <username> <storeName> <productName> <bid>");
            error = true;
            return;
        }
        String token = getTokenForUserName(toSend.get(0));
        if (token == null) {
            return;
        }
        String storeName = toSend.get(1).replace("-", " ");
        int id = getStoreIdByName(storeName);
        String productName = toSend.get(2).replace("-", " ");
        double price = Double.parseDouble(toSend.get(3));
        try {
            RandomDTO[] randoms = activeService.getAllActiveRandoms_user(token, id);
            RandomDTO random = null;
            for (RandomDTO randomDTO : randoms) {
                if (randomDTO.productName.equals(productName))
                    random = randomDTO;
            }
            if (random == null) {
                log("random " + productName + " does not found!");
                return;
            }

            purchaseService.participateInRandom(token, random.id, id, price, PaymentDetails.testPayment());
            log("user " + toSend.get(0) + "bid on auction set successfully ");
        } catch (Exception e) {
            log("got error on line " + line + " :" + e.getMessage());
        }
    }

    @Transactional
    public void purchase(List<String> toSend) {
        if (toSend.size() != 3) {
            log("invalid params purchase <userName> <special/cart> <valid/unvalid> . line " + line);
            error = true;
            return;
        }

        String token = getTokenForUserName(toSend.get(0));
        if (token == null) {
            return;
        }
        PaymentDetails paymentDetails = null;
        SupplyDetails supply = null;
        if (toSend.get(2).equals("valid")) {
            paymentDetails = PaymentDetails.testPayment();
            supply = SupplyDetails.getTestDetails();
        } else if (toSend.get(2).equals("invalid")) {
            try {
                paymentDetails = PaymentDetails.test_fail_Payment();
                supply = SupplyDetails.getTestDetails();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            log("you must add payment test if valid");
            error = true;
            return;
        }
        ReceiptDTO[] reciept = new ReceiptDTO[0];
        if (toSend.get(1).equals("cart")) {
            try {
                reciept = purchaseService.buyRegisteredCart(token, paymentDetails, supply);
            } catch (Exception e) {

                log(e.getMessage());
            }
        } else if (toSend.get(1).equals("special")) {
            try {
                reciept = purchaseService.finalizeSpecialCart(token, paymentDetails, supply);
            } catch (Exception e) {
                log(e.getMessage());
            }
        }
        for (ReceiptDTO rec : reciept) {
            log("recipt: store name" + rec.getStoreName() + " ,final price: " + rec.getFinalPrice());
            for (ReceiptProduct receiptDTO : rec.getProductsList()) {
                log("product :" + receiptDTO.getProductName() + ",qauntity:" + receiptDTO.getQuantity() + ",price"
                        + receiptDTO.getPrice());
            }
        }
    }

    @Transactional
    public void auction(List<String> toSend) {
        if (toSend.size() != 4) {
            log("syntax error on line " + line
                    + " : params does not match auction <username> <storeName> <productName> <bid>");
            error = true;
            return;
        }
        String token = getTokenForUserName(toSend.get(0));
        if (token == null) {
            return;
        }
        String storeName = toSend.get(1).replace("-", " ");
        int id = getStoreIdByName(storeName);
        String productName = toSend.get(2).replace("-", " ");
        double bid = Double.parseDouble(toSend.get(3));
        try {
            AuctionDTO auction = getAuction(token, id, productName);

            if (activeService.addBidOnAucction(token, auction.auctionId, id, bid))
                log("user " + toSend.get(0) + "bid on auction set successfully ");
        } catch (Exception e) {
            log("got error on line " + line + " :" + e.getMessage());
        }

    }

    @Transactional
    public AuctionDTO getAuction(String token, int id, String productName) throws Exception {
        AuctionDTO[] auctions = activeService.getAllActiveAuctions_user(token, id);
        for (AuctionDTO auctionDTO : auctions) {
            if (auctionDTO.productName.equals(productName) && auctionDTO.status == AuctionStatus.IN_PROGRESS) {
                return auctionDTO;
            }
        }
        error = true;
        return null;
    }

    private void cart(List<String> toSend) {
        if (toSend.size() != 5) {
            log("in line " + line
                    + " , wrong syntax error (number of params) correct syntax :user cart <+/-> #userName #productName #storeName #quantity");
            error = true;
            return;
        }
        String userToken = getTokenForUserName(toSend.get(1));
        String productName = toSend.get(2).replace("-", " ");
        String storeName = toSend.get(3).replace("-", " ");
        int storeId = getStoreIdByName(storeName);
        if (storeId == -1) {
            log("store " + storeName + " not found!");
            error = true;
            return;
        }
        int quantity = Integer.parseInt(toSend.get(4));
        if (toSend.get(0).equals("+")) {
            ProductSearchCriteria criteria = new ProductSearchCriteria(productName, null, null, storeId, null, null,
                    null, null);
            try {
                ItemStoreDTO[] items = stockService.searchProductsOnAllSystem(userToken, criteria);
                if (items.length == 0) {
                    log("product " + productName + " not found on store " + storeName);
                    error = true;
                    return;
                }
                userService.addToUserCart(userToken, items[0], quantity);
                log("adding " + productName + " from user success !!");

            } catch (Exception e) {
                log("line " + line + " got error :" + e.getMessage());
                error = true;
            }
        } else if (toSend.get(0).equals("-")) {
            try {
                ItemCartDTO[] items = userService.getRegularCart(userToken);
                int itemToModify = getIdMatch(items, productName, storeId);
                if (itemToModify == -1) {
                    log("line " + line + " got error :there is no product for " + productName + " on user cart!");
                    // error = true;
                    return;
                }
                userService.removeItemFromCart(userToken, itemToModify);
                log("removing " + productName + " from user success !!");
            } catch (UIException e) {
                log("line " + line + " got error :" + e.getMessage());
                // error = true;
            }
        } else {
            log("in line " + line
                    + " , wrong syntax error (number of params) correct syntax :user cart <+/-> #userName #productName #storeName #quantity");
            error = true;
            return;
        }
    }

    private int getIdMatch(ItemCartDTO[] items, String productName, int storeId) {
        for (ItemCartDTO itemCartDTO : items) {
            String cartItemName = itemCartDTO.name.toLowerCase();
            String product = productName.toLowerCase();
            if (product.equals(cartItemName) && itemCartDTO.storeId == storeId) {
                return itemCartDTO.itemCartId;
            }

        }
        return -1;
    }

    protected void login(List<String> subList) {
        if (subList.size() != 2) {
            error = true;
            log("Error : login must recieve 2 params : username password .");
            return;
        }
        String username = subList.get(0);
        String password = subList.get(1);
        try {
            String token = userService.generateGuest();
            String newToken = userService.login(token, username, password);
            tokens.put(username, newToken);
            int userId = authRepo.getUserId(newToken);
            ids.put(username, userId);
            log("user " + username + " loged in success! , curr token" + newToken);
        } catch (Exception e) {
            log("login failed in line " + line + " , error :" + e.getMessage());
            error = true;
        }

    }

    protected void register(List<String> subList) {
        if (subList.size() != 3) {
            error = true;
            log("Error : register must recieve 3 params : username password age .");
        }
        String username = subList.get(0);
        String password = subList.get(1);
        int age = Integer.parseInt(subList.get(2));
        try {
            boolean res = userService.register(userService.generateGuest(), username, password, age);
            if (res) {
                log("register success to user " + username);
            } else {
                log("register failed to user " + username);
            }

        } catch (UIException e) {
            log("got ui error : " + e.getMessage());
            error = true;
        } catch (Exception e) {
            log("got error " + e.getMessage());
            error = true;
        }
    }

    private void offerManager(List<String> toSend) {
        if (toSend.size() < 4) {
            log("syntax error on line " + line + ": user manager <ownerName> <storeName> <managerName> <perm1> ...");
            //error = true;
            return;
        }

        String ownerName = toSend.get(0);
        String storeName = toSend.get(1);
        String managerName = toSend.get(2);
        List<String> permissionNames = toSend.subList(3, toSend.size());

        String token = getTokenForUserName(ownerName);
        if (token == null) return;

        int storeId = getStoreIdByName(storeName);
        if (storeId == -1) {
            log("store " + storeName + " not found");
            //error = true;
            return;
        }

        List<Permission> permissions = new ArrayList<>();
        for (String name : permissionNames) {
            try {
                permissions.add(Permission.valueOf(name));
            } catch (IllegalArgumentException e) {
                log("invalid permission '" + name + "' on line " + line);
                //error = true;
                return;
            }
        }
        try {
            storeService.MakeOfferToAddManagerToStore(storeId, token, managerName, permissions);
            log("offered manager role to " + managerName + " on store " + storeName);
        } catch (Exception e) {
            log("error making manager offer on line " + line + ": " + e.getMessage());
            error = true;
        }
    }

    private void answerOffer(List<String> toSend) {
        if (toSend.size() != 5) {
            log("syntax error on line " + line + ": user offer-answer <storeName> <senderName> <receiverName> <accept/reject> <manager/owner>");
            error = true;
            return;
        }

        String storeName = toSend.get(0);
        String senderName = toSend.get(1);
        String receiverName = toSend.get(2);
        boolean accepted = toSend.get(3).equalsIgnoreCase("accept");
        boolean toBeOwner = toSend.get(4).equalsIgnoreCase("owner");
        int storeId = getStoreIdByName(storeName);
        if (storeId == -1) {
            log("store " + storeName + " not found");
            error = true;
            return;
        }
        try {
            storeService.reciveAnswerToOffer(storeId, senderName, receiverName, accepted, toBeOwner);
            log("offer answer handled successfully: " + (accepted ? "accepted" : "rejected") + " as " + (toBeOwner ? "owner" : "manager"));
        } catch (Exception e) {
            log("error handling offer-answer on line " + line + ": " + e.getMessage());
            error = true;
        }
    }

    private void deleteManager(List<String> toSend) {
        if (toSend.size() != 3) {
            log("syntax error on line " + line + ": user delete-manager <ownerName> <storeName> <managerName>");
            error = true;
            return;
        }
        String ownerName = toSend.get(0);
        String storeName = toSend.get(1);
        String managerName = toSend.get(2);
        String token = getTokenForUserName(ownerName);
        if (token == null) return;
        int storeId = getStoreIdByName(storeName);
        if (storeId == -1) {
            log("store " + storeName + " not found");
            error = true;
            return;
        }
        Integer managerId = ids.get(managerName);
        if (managerId == null) {
            log("manager " + managerName + " not found in id ");
            error = true;
            return;
        }
        try {
            storeService.deleteManager(storeId, token, managerId);
            log("manager " + managerName + " removed from store " + storeName);
        } catch (Exception e) {
            log("error deleting manager on line " + line + ": " + e.getMessage());
            error = true;
        }
    }

    private void deleteOwner(List<String> toSend) {
        if (toSend.size() != 3) {
            log("syntax error on line " + line + ": user delete-owner <ownerUsername> <storeName> <ownerToDeleteUsername>");
            error = true;
            return;
        }

        String ownerName = toSend.get(0);
        String storeName = toSend.get(1);
        String ownerToDelete = toSend.get(2);

        String token = getTokenForUserName(ownerName);
        if (token == null) return;

        int storeId = getStoreIdByName(storeName);
        if (storeId == -1) {
            log("store " + storeName + " not found");
            error = true;
            return;
        }

        List<Registered> users = regJpaRepo.findRegisteredUsersByUsername(ownerToDelete);


        if (users.isEmpty()) {
            log("owner to delete not found: " + ownerToDelete);
            error = true;
            return;
        }

        int ownerToDeleteId = users.get(0).getId();
        try {
            storeService.DeleteOwnershipFromStore(storeId, token, ownerToDeleteId);
            log("deleted ownership of " + ownerToDelete + " from store " + storeName);
        } catch (Exception e) {
            log("error deleting ownership on line " + line + ": " + e.getMessage());
            error = true;
        }
    }

    private void offerOwner(List<String> toSend) {
        if (toSend.size() != 3) {
            log("syntax error on line " + line + ": user add-owner <ownerName> <storeName> <newOwnerName>");
            error = true;
            return;
        }

        String ownerName = toSend.get(0);
        String storeName = toSend.get(1);
        String newOwnerName = toSend.get(2);

        String token = getTokenForUserName(ownerName);
        if (token == null) return;

        int storeId = getStoreIdByName(storeName);
        if (storeId == -1) {
            log("store " + storeName + " not found");
            error = true;
            return;
        }

        try {
            storeService.MakeofferToAddOwnershipToStore(storeId, token, newOwnerName);
            log("offered ownership to " + newOwnerName + " on store " + storeName);
        } catch (Exception e) {
            log("error making ownership offer on line " + line + ": " + e.getMessage());
            error = true;
        }
    }

    private void logout(List<String> toSend) {
        if (toSend.size() != 1) {
            log("syntax error on line " + line + ": logout <username>");
            error = true;
            return;
        }

        String username = toSend.get(0);
        String token = getTokenForUserName(username);
        if (token == null) {
            log("logout failed: no token for " + username);
            error = true;
            return;
        }

        try {
            userService.logoutUser(token);
            log("user " + username + " logged out successfully");
        } catch (Exception e) {
            log("logout failed on line " + line + ": " + e.getMessage());
            error = true;
        }
    }






    }
