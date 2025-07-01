package workshop.demo.ApplicationLayer.DataInitilizer;

import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Component
public class StoreParser extends ManagerDataInit {

    @Transactional
    public void store(List<String> construction) {
        List<String> toSend = construction.subList(1, construction.size());
        switch (construction.getFirst()) {
            case "create":
                createStore(toSend);
                break;
            case "item":
                addItem(toSend);
                break;
            case "auction":
                auction(toSend);
                break;
            case "random":
                random(toSend);
                break;
            case "bid":
                addBidToStore(toSend);
                break;
            case "close":
                closeStore(toSend);
                break;
            case "discount":
                addDiscountToStore(toSend);
                break;
            case "removediscount":
                removeDiscountFromStore(toSend);
                break;
            case "policy":
                policy(toSend);
                break;
            default:
                log("undefined function for store on line " + line + " : " + construction.getFirst());
                error = true;
                break;
        }

    }

    private void policy(List<String> toSend) {
        if (toSend.size() != 6) {
            log("request policy must be : policy <+/-> <user name> <store name> <product name> <policy Key> <param>;");
            return;
        }
        String token = getTokenForUserName(toSend.get(1));
        String storeName = toSend.get(2).replace("-", " ");
        int storeId = getStoreIdByName(storeName);
        String productName = toSend.get(3).replace("-", " ");
        String ploicyKey = toSend.get(4);
        int param = Integer.parseInt(toSend.get(5));
        try {
            ItemStoreDTO item = getProductByNameAndStore(param, productName, token, storeName);
            if (toSend.get(0).equals("+"))
                storeService.addPurchasePolicy(token, storeId, ploicyKey, item.getProductId(), param);
            else if (toSend.get(0).equals("-"))
                storeService.removePurchasePolicy(token, storeId, ploicyKey, item.getProductId(), param);
            else {
                log("syntax error :policy <+/-> <user name> <store name> <product name> <policy Key> <param>;");
            }
            log(ploicyKey + " added to store success on product " + productName);
        } catch (Exception e) {
            log("failed to add policy " + e.getMessage());
        }

    }

    private void addBidToStore(List<String> toSend) {
        String ownerToken = getTokenForUserName(toSend.get(1));
        int storeId = getStoreIdByName(toSend.get(2));
        try {
            ItemStoreDTO item = getProductByNameAndStore(storeId, toSend.get(3), ownerToken,
                    toSend.get(2).replace("-", " "));
            switch (toSend.get(0).toLowerCase()) {
                case "set":
                    setProductToBid(ownerToken, storeId, item.getProductId(), Integer.parseInt(toSend.get(4)));
                    break;
                case "accept":
                    acceptBid(ownerToken, storeId, item.getProductId(), toSend.get(4));
                    break;
                case "reject":
                    rejectBid(ownerToken, storeId, item.getProductId(), toSend.get(4), toSend.get(5));
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void rejectBid(String ownerToken, int storeId, int productId, String string, String string2) {
        // TODO we have to get the bid that user belong to
        BidDTO[] bids;
        try {
            bids = activeService.getAllBids(ownerToken, storeId);
            int bidId = -1;
            for (BidDTO bidDTO : bids) {
                if (bidDTO.productId == productId) {
                    bidId = bidDTO.bidId;
                }
            }
            int userId = -1;
            List<UserDTO> users = userService.getAllUsers(ownerToken);
            for (UserDTO user : users) {
                if (user.username.equals(string))
                    userId = user.id;
            }
            Double offer = null;
            if (!string2.equals("no"))
                offer = Double.parseDouble(string2);
            activeService.rejectBid(ownerToken, storeId, bidId, userId, offer);
            log("success to reject bid  .");
        } catch (Exception e) {
            log("failed to reject bid  ." + e.getMessage());
        }
    }

    private void acceptBid(String ownerToken, int storeId, int productId, String userName) {
        try {

            BidDTO[] bids = activeService.getAllBids(ownerToken, storeId);
            int bidId = -1;
            for (BidDTO bidDTO : bids) {
                if (bidDTO.productId == productId) {
                    bidId = bidDTO.bidId;
                }
            }

            int userId = -1;
            List<UserDTO> users = userService.getAllUsers(ownerToken);
            for (UserDTO user : users) {
                if (user.username.equals(userName))
                    userId = user.id;
            }
            if (bidId == -1 || userId == -1) {
                log(userId + " user id , " + bidId + " bidId ");
                return;
            }
            activeService.acceptBid(ownerToken, storeId, bidId, userId);
            log("bid for user accepted by one owner!");
        } catch (Exception e) {
            log("failed bid for user accepted by one owner! " + e.getMessage());
        }
    }

    private void setProductToBid(String ownerToken, int storeId, int productId, int int1) {
        try {
            activeService.setProductToBid(ownerToken, storeId, productId, int1);
            log("bid added succcessfuly to store! ");
        } catch (Exception e) {
            log("error on seting product to bid");
            error = true;
        }
    }

    @Transactional
    public void random(List<String> toSend) {
        if (toSend.size() != 6) {
            log("random must be : random <usernaame> <store name> <product name> <random time> <quantity> <total price>;");
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
        long time = Long.parseLong(toSend.get(3));
        int quantity = Integer.parseInt(toSend.get(4));
        double total = Double.parseDouble(toSend.get(5));
        try {
            ItemStoreDTO item = getProductByNameAndStore(id, productName, token, storeName);
            activeService.setProductToRandom(token, item.getProductId(), quantity, total, id, time);
            log("random successfuly added!");
        } catch (Exception e) {
            log("got error on creating random ," + e.getMessage());
        }

    }

    private void auction(List<String> toSend) {
        if (toSend.size() != 6) {
            log("syntax errror with auction , params not match : auction <username> <store name> <product name> <auction time> <quantity>  <start price> ; .line:"
                    + line);
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
        long time = Long.parseLong(toSend.get(3));
        int quantity = Integer.parseInt(toSend.get(4));
        double startPrice = Double.parseDouble(toSend.get(5));
        try {
            ItemStoreDTO item = getProductByNameAndStore(id, productName, token, storeName);
            activeService.setProductToAuction(token, id, item.getProductId(), quantity, time, startPrice);
            // entityManager.flush();
            log("auction set success!!");

        } catch (Exception e) {
            log("line " + line + " got error :" + e.getMessage());
        }
    }

    private void addItem(List<String> toSend) {
        if (toSend.size() < 8) {
            log("forgot one param or more :  #ownerName #stroeName #productName #productCategory #desc #quantity #price #keword1 #keyword2 ... ; ");
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
        Category category = Category.valueOf(toSend.get(3));
        String desc = toSend.get(4).replace("-", " ");
        int quantity = Integer.parseInt(toSend.get(5));
        int price = Integer.parseInt(toSend.get(6));
        String[] keyWords = new String[toSend.size() - 7];
        for (int i = 0; i < toSend.size() - 7; i++) {
            keyWords[i] = toSend.get(i + 7).replace("-", " ");
        }
        try {
            int productId = stockService.addProduct(token, productName, category, desc, keyWords);
            stockService.addItem(id, token, productId, quantity, price, category);
            log("successfuly adding " + productName + " on store " + storeName + "! ");
        } catch (Exception e) {
            log("error on adding item/product " + line + " : " + e.getMessage());
            error = true;
        }

    }

    protected void createStore(List<String> toSend) {
        if (toSend.size() != 3) {
            log("params not sufficent " + toSend.size() + " , must : store create #storeName #storeCategory");
            error = true;
            return;
        }
        String userName = toSend.get(0);
        String storeName = toSend.get(1).replace("-", " ");
        String category = toSend.get(2).replace("-", " ");
        if (!tokens.containsKey(userName)) {
            error = true;
            log("you must login using B-Script");
            return;
        }
        String token = tokens.get(userName);
        try {
            int storeId = storeService.addStoreToSystem(token, storeName, category);
            ids.put(storeName, storeId);
            log("successfuly create store " + storeName);
        } catch (UIException | DevException e) {
            // error = true;
            log("got error on line " + line + " :" + e.getMessage());
        }

    }

    private void closeStore(List<String> toSend) {
        if (toSend.size() != 2) {
            log("invalid close command format. Should be: store close <ownerUsername> <storeName>;");
            error = true;
            return;
        }
        String ownerName = toSend.get(0);
        String storeName = toSend.get(1);
        String token = getTokenForUserName(ownerName);
        if (token == null) {
            log("missing login for user: " + ownerName);
            error = true;
            return;
        }
        int storeId = getStoreIdByName(storeName);
        try {
            storeService.closeStore(storeId, token);
            log("Successfully closed store: " + storeName);
        } catch (Exception e) {
            log("Error closing store: " + e.getMessage());
            error = true;
        }
    }

    private void addDiscountToStore(List<String> toSend) {
    if (toSend.size() < 7) {
        log("Invalid syntax for discount. Must be: store discount <username> <storeName> <discountName> <percent> <type> <condition> <logic>");
        error = true;
        return;
    }
    String username = toSend.get(0);
    String token = getTokenForUserName(username);
    String storeName = toSend.get(1).replace("-", " ");
    int storeId = getStoreIdByName(storeName);

    try {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(toSend.get(2));
        dto.setPercent(Double.parseDouble(toSend.get(3)));
        dto.setType(CreateDiscountDTO.Type.valueOf(toSend.get(4).toUpperCase()));
        String cond = toSend.get(5).equalsIgnoreCase("null") ? null : toSend.get(5).replace("-", " ");
        dto.setCondition(cond);
        dto.setLogic(CreateDiscountDTO.Logic.valueOf(toSend.get(6).toUpperCase()));
        storeService.addDiscount(storeId, token, dto);
        log("Discount added successfully to store " + storeName);
    } catch (Exception e) {
        log("Failed to add discount: " + e.getMessage());
        error = true;
    }
    }

    private void removeDiscountFromStore(List<String> toSend) {
        if (toSend.size() != 3) {
            log("Invalid syntax. Must be: store removediscount <username> <storeName> <discountName>");
            error = true;
            return;
        }

        String username = toSend.get(0);
        String token = getTokenForUserName(username);
        String storeName = toSend.get(1).replace("-", " ");
        String discountName = toSend.get(2).replace("-", " ");
        int storeId = getStoreIdByName(storeName);

        try {
            storeService.removeDiscountFromStore(token, storeId, discountName);
            log("Successfully removed discount '" + discountName + "' from store " + storeName);
        } catch (Exception e) {
            log("Failed to remove discount: " + e.getMessage());
            error = true;
        }
    }

}






