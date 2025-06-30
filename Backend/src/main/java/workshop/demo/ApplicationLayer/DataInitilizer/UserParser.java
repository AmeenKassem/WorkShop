package workshop.demo.ApplicationLayer.DataInitilizer;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.User.CartItem;

import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
public class UserParser extends ManagerDataInit {
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
            default:
                log("undefined function for user on line " + line + " : " + construction.get(0));
                error = true;
                break;
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
            if (auctionDTO.productName.equals(productName) && auctionDTO.status==AuctionStatus.IN_PROGRESS) {
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
                log("removing " + productName + " from user success !!");

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
}
