package workshop.demo.DomainLayer.Purchase;

import java.util.*;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SingleBid;


public interface IPurchaseRepo {

    // public List<ReceiptProduct> pay(List<ItemCartDTO> items );

    // public List<ReceiptProduct> pay(List<ItemCartDTO> items );

    void saveBid(SingleBid bid);

    List<SingleBid> getAllBidsByUser(int userId);

    List<SingleBid> getAcceptedBidsByUser(int userId);

    List<SingleBid> getWinningBidsByUser(int userId);

    void saveRandomParticipation(ParticipationInRandomDTO participation);

    List<ParticipationInRandomDTO> getAllRandomParticipationsByUser(int userId);
}
