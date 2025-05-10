<<<<<<< HEAD:src/main/java/workshop/demo/DomainLayer/User/Registered.java
package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.InfrastructureLayer.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registered extends Guest {
    private static final Logger logger = LoggerFactory.getLogger(Registered.class);

    private String username;
    private String encrybtedPassword;
    private boolean isOnline;
    private RoleOnSystem systemRole = RoleOnSystem.Regular;

    private List<SingleBid> regularBids;
    private List<SingleBid> auctionBids;
    private List<ParticipationInRandomDTO> participationsOnRandoms;

    public Registered(int id2, String username, String encrybtedPassword) {

        super(id2);
        this.username = username;
        this.encrybtedPassword = encrybtedPassword;
        regularBids = new ArrayList<SingleBid>();
        auctionBids = new ArrayList<SingleBid>();
        participationsOnRandoms = new ArrayList<ParticipationInRandomDTO>();
    }

    public boolean check(Encoder encoder, String username, String password) {
        logger.debug("Registered user created:username={}", username);
        boolean res = encoder.matches(password, encrybtedPassword) && username.equals(this.username);
        logger.debug("Password match result: {}", res);

        return res;
    }

    public void setAdmin() {
        systemRole = RoleOnSystem.Admin;
        logger.debug("User {} set as Admin", username);

    }

    public boolean isAdmin() {
        return systemRole == RoleOnSystem.Admin;
    }

    public boolean isOnlien() {
        return isOnline;
    }

    public void logout() {
        isOnline = false;
        logger.debug("User {} logged out", username);

    }

    public void login() {
        isOnline = true;
        logger.debug("User {} logged in", username);

    }

    public String getUsername() {
        return username;
    }

    public List<SingleBid> getRegularBids() {
        return regularBids;
    }

    public List<SingleBid> getAuctionBids() {
        return auctionBids;
    }

    public List<ParticipationInRandomDTO> getParticipationsOnRandoms() {
        return participationsOnRandoms;
    }

    public void addParticipationForRandom(ParticipationInRandomDTO card) {
        logger.debug("User {} adding participation to random ID={}", username, card.randomId);

        ParticipationInRandomDTO cardToAdd = getCardForRandom(card.randomId);
        if (cardToAdd == null) {
            participationsOnRandoms.add(card);
            logger.debug("Participation added for random ID={}", card.randomId);

        } else {
            logger.debug("Participation already exists for random ID={}", card.randomId);

            // cardToAdd.addCard();

        }
    }

    public ParticipationInRandomDTO getCardForRandom(int randomId) {
        logger.debug("Fetching participation for random ID={}", randomId);

        for (ParticipationInRandomDTO card : participationsOnRandoms) {
            if (card.getRandomId() == randomId && !card.ended)
                return card;
        }
        return null;
    }

    public void addRegularBid(SingleBid bid) {
        regularBids.add(bid);
        logger.debug("Regular bid added by user {}", username);

    }

    public void addAuctionBid(SingleBid bid) {
        auctionBids.add(bid);
        logger.debug("Auction bid added by user {}", username);

    }

    public void removeCardForRandom(int randomId) {
        logger.debug("Removing participation for random ID={} by user {}", randomId, username);

        for (ParticipationInRandomDTO card : participationsOnRandoms) {
            if (card.getRandomId() == randomId) {
                participationsOnRandoms.remove(card);
                return;
            }
        }
    }

    public void removeRegularBid(SingleBid bid) {
        regularBids.remove(bid);
        logger.debug("Regular bid removed by user {}", username);

    }

    public void removeAuctionBid(SingleBid bid) {
        auctionBids.remove(bid);
        logger.debug("Auction bid removed by user {}", username);

    }

    public List<ParticipationInRandomDTO> getWinningCards() {
        logger.debug("Fetching winning cards for user {}", username);

        List<ParticipationInRandomDTO> res = new ArrayList<>();
        for (ParticipationInRandomDTO cardForRandomDTO : res) {
            if (cardForRandomDTO.won()) {
                res.add(cardForRandomDTO);
                logger.debug("User {} won ", username);
            }
        }
        return res;
    }

    public List<SingleBid> getWinningBids() {
        logger.debug("Fetching winning bids for user {}", username);

        List<SingleBid> bidsResult = new ArrayList<>();
        for (SingleBid singleBid : auctionBids) {
            if (singleBid.isWon()) {
                bidsResult.add(singleBid);
                logger.debug("User {} won auction bid", username);
            }
        }
        for (SingleBid singleBid : regularBids) {
            if (singleBid.isWon()) {
                bidsResult.add(singleBid);
                logger.debug("User {} won regular bid ID={}", username);
            }
        }
        return bidsResult;
    }

}
=======
package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.InfrastructureLayer.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registered extends Guest {
    private static final Logger logger = LoggerFactory.getLogger(Registered.class);

    private String username;
    private String encrybtedPassword;
    private boolean isOnline;
    private RoleOnSystem systemRole = RoleOnSystem.Regular;

    private List<SingleBid> regularBids;
    private List<SingleBid> auctionBids;
    private List<ParticipationInRandomDTO> participationsOnRandoms;

    public Registered(int id2, String username, String encrybtedPassword) {

        super(id2);
        this.username = username;
        this.encrybtedPassword = encrybtedPassword;
        regularBids = new ArrayList<SingleBid>();
        auctionBids = new ArrayList<SingleBid>();
        participationsOnRandoms = new ArrayList<ParticipationInRandomDTO>();
    }

    public boolean check(Encoder encoder, String username, String password) {
        logger.debug("Registered user created:username={}", username);
        boolean res = encoder.matches(password, encrybtedPassword) && username.equals(this.username);
        logger.debug("Password match result: {}", res);

        return res;
    }

    public void setAdmin() {
        systemRole = RoleOnSystem.Admin;
        logger.debug("User {} set as Admin", username);

    }

    public boolean isAdmin() {
        return systemRole == RoleOnSystem.Admin;
    }

    public boolean isOnlien() {
        return isOnline;
    }

    public void logout() {
        isOnline = false;
        logger.debug("User {} logged out", username);

    }

    public void login() {
        isOnline = true;
        logger.debug("User {} logged in", username);

    }

    public String getUsername() {
        return username;
    }

    public List<SingleBid> getRegularBids() {
        return regularBids;
    }

    public List<SingleBid> getAuctionBids() {
        return auctionBids;
    }

    public List<ParticipationInRandomDTO> getParticipationsOnRandoms() {
        return participationsOnRandoms;
    }

    public void addParticipationForRandom(ParticipationInRandomDTO card) {
        logger.debug("User {} adding participation to random ID={}", username, card.randomId);

        ParticipationInRandomDTO cardToAdd = getCardForRandom(card.randomId);
        if (cardToAdd == null) {
            participationsOnRandoms.add(card);
            logger.debug("Participation added for random ID={}", card.randomId);

        } else {
            logger.debug("Participation already exists for random ID={}", card.randomId);

            // cardToAdd.addCard();

        }
    }

    public ParticipationInRandomDTO getCardForRandom(int randomId) {
        logger.debug("Fetching participation for random ID={}", randomId);

        for (ParticipationInRandomDTO card : participationsOnRandoms) {
            if (card.getRandomId() == randomId && !card.ended)
                return card;
        }
        return null;
    }

    public void addRegularBid(SingleBid bid) {
        regularBids.add(bid);
        logger.debug("Regular bid added by user {}", username);

    }

    public void addAuctionBid(SingleBid bid) {
        auctionBids.add(bid);
        logger.debug("Auction bid added by user {}", username);

    }

    public void removeCardForRandom(int randomId) {
        logger.debug("Removing participation for random ID={} by user {}", randomId, username);

        for (ParticipationInRandomDTO card : participationsOnRandoms) {
            if (card.getRandomId() == randomId) {
                participationsOnRandoms.remove(card);
                return;
            }
        }
    }

    public void removeRegularBid(SingleBid bid) {
        regularBids.remove(bid);
        logger.debug("Regular bid removed by user {}", username);

    }

    public void removeAuctionBid(SingleBid bid) {
        auctionBids.remove(bid);
        logger.debug("Auction bid removed by user {}", username);

    }

    public List<ParticipationInRandomDTO> getWinningCards() {
        logger.debug("Fetching winning cards for user {}", username);

        List<ParticipationInRandomDTO> res = new ArrayList<>();
        for (ParticipationInRandomDTO cardForRandomDTO : res) {
            if (cardForRandomDTO.won()) {
                res.add(cardForRandomDTO);
                logger.debug("User {} won ", username);
            }
        }
        return res;
    }

    public List<SingleBid> getWinningBids() {
        logger.debug("Fetching winning bids for user {}", username);

        List<SingleBid> bidsResult = new ArrayList<>();
        for (SingleBid singleBid : auctionBids) {
            if (singleBid.isWon()) {
                bidsResult.add(singleBid);
                logger.debug("User {} won auction bid", username);
            }
        }
        for (SingleBid singleBid : regularBids) {
            if (singleBid.isWon()) {
                bidsResult.add(singleBid);
                logger.debug("User {} won regular bid ID={}", username);
            }
        }
        return bidsResult;
    }

}
>>>>>>> main:Backend/src/main/java/workshop/demo/DomainLayer/User/Registered.java
