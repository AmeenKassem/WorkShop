package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Stock.SingleBid;

@Repository
public class PurchaseRepository implements IPurchaseRepo {

    private final Map<Integer, List<SingleBid>> bidsByUser;
    private final Map<Integer, List<ParticipationInRandomDTO>> randomParticipationByUser;

    @Autowired
    public PurchaseRepository() {
        this.bidsByUser = new HashMap<>(); //userId -> List of bids
        this.randomParticipationByUser = new HashMap<>(); //userId -> List of random participations

    }

    @Override
    public void saveBid(SingleBid bid) {
        bidsByUser.computeIfAbsent(bid.getUserId(), k -> new ArrayList<>()).add(bid);
    }

    @Override
    public List<SingleBid> getAllBidsByUser(int userId) {
        return bidsByUser.getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public List<SingleBid> getAcceptedBidsByUser(int userId) {
        return bidsByUser.getOrDefault(userId, Collections.emptyList())
                .stream()
                .filter(SingleBid::isAccepted)
                .toList();
    }

    @Override
    public List<SingleBid> getWinningBidsByUser(int userId) {
        return bidsByUser.getOrDefault(userId, Collections.emptyList())
                .stream()
                .filter(SingleBid::isWinner)
                .toList();
    }

    @Override
    public void saveRandomParticipation(ParticipationInRandomDTO participation) {
        randomParticipationByUser
                .computeIfAbsent(participation.userId, k -> new ArrayList<>())
                .add(participation);
    }

    @Override
    public List<ParticipationInRandomDTO> getAllRandomParticipationsByUser(int userId) {
        return randomParticipationByUser.getOrDefault(userId, Collections.emptyList());
    }
   public void clear() {
            bidsByUser.clear();
        
            randomParticipationByUser.clear();
    
}
}
