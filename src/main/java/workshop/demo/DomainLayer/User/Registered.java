package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.InfrastructureLayer.Encoder;

public class Registered extends Guest {

    private String username;
    private String encrybtedPassword; 
    private boolean isOnline;
    private RoleOnSystem systemRole=RoleOnSystem.Regular;

    private List<SingleBid> regularBids;
    private List<SingleBid> auctionBids;
    private List<ParticipationInRandomDTO> participationsOnRandoms;


    public Registered(int id2,String username,String encrybtedPassword ) {
        super(id2);
        this.username=username;
        this.encrybtedPassword = encrybtedPassword;
        regularBids = new ArrayList<SingleBid>();
        auctionBids = new ArrayList<SingleBid>();
        participationsOnRandoms = new ArrayList<ParticipationInRandomDTO>();
    }

    public boolean check(Encoder encoder, String username, String password) {
        System.out.println("pass1 : "+password+",pass2:"+encrybtedPassword);
        return encoder.matches(password,encrybtedPassword) && username.equals(this.username);
    }

    public void setAdmin(){
        systemRole = RoleOnSystem.Admin;
    }

    public boolean isAdmin(){
        return systemRole == RoleOnSystem.Admin;
    }

   

    public boolean isOnlien(){
        return isOnline;
    }

    public void logout() {
        isOnline=false;
    }

    public void login(){
        isOnline = true;
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
        ParticipationInRandomDTO cardToAdd = getCardForRandom(card.randomId);
        if(cardToAdd == null){
            participationsOnRandoms.add(card);
        }
        else{
            //cardToAdd.addCard();

        }
    }

    public ParticipationInRandomDTO getCardForRandom(int randomId) {
        for(ParticipationInRandomDTO card: participationsOnRandoms){
            if(card.getRandomId() == randomId && !card.ended)
                return card;
        }
        return null;
    }

    public void addRegularBid(SingleBid bid) {
        regularBids.add(bid);
    }

    public void addAuctionBid(SingleBid bid) {
        auctionBids.add(bid);
    }

    public void removeCardForRandom(int randomId) {
        for(ParticipationInRandomDTO card: participationsOnRandoms){
            if(card.getRandomId() == randomId){
                participationsOnRandoms.remove(card);
                return;
            }
        }
    }

    public void removeRegularBid(SingleBid bid) {
        regularBids.remove(bid);
    }

    public void removeAuctionBid(SingleBid bid) {
        auctionBids.remove(bid);
    }

    public List<ParticipationInRandomDTO> getWinningCards() {
        List<ParticipationInRandomDTO> res = new ArrayList<>();
        for (ParticipationInRandomDTO cardForRandomDTO : res) {
            if(cardForRandomDTO.won()) res.add(cardForRandomDTO);
        }
        return res;
    }

    public List<SingleBid> getWinningBids() {
        List<SingleBid> bidsResult = new ArrayList<>();
        for (SingleBid singleBid : auctionBids) {
            if(singleBid.isWon()) bidsResult.add(singleBid);
        }
        for (SingleBid singleBid : regularBids) {
            if(singleBid.isWon()) bidsResult.add(singleBid);
        }
        return bidsResult;
    }




}
