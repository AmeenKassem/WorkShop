package workshop.demo.UnitTests.PurchaseTests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.InfrastructureLayer.PurchaseRepository;

//@SpringBootTest
@ActiveProfiles("test")
public class PurchaseTests {

    private PurchaseRepository repo;

    @BeforeEach
    void setUp() {
        repo = new PurchaseRepository();
    }

    @Test
    void testSaveBidAndRetrieve() {
        int userId = 1;
        SingleBid bid = new SingleBid(1, 2, userId, 100.0, SpecialType.BID, 5, 10);
        repo.saveBid(bid);
        List<SingleBid> result = repo.getAllBidsByUser(userId);

        assertEquals(1, result.size());
        assertEquals(bid, result.get(0));
    }


    @Test
    void testSaveRandomParticipationAndRetrieve() {
        ParticipationInRandomDTO dto = new ParticipationInRandomDTO(10, 2, 333, 99, 50.0);
        repo.saveRandomParticipation(dto);
        List<ParticipationInRandomDTO> result = repo.getAllRandomParticipationsByUser(333);

        assertEquals(1, result.size());
        assertEquals(333, result.get(0).userId);
        assertEquals(dto.getStoreId(), result.get(0).getStoreId());
        assertEquals(dto.getProductId(), result.get(0).getProductId());
        assertEquals(dto.getRandomId(), result.get(0).getRandomId());

    }

    @Test
    public void testGetAllRandomParticipationsByUser() {
        ParticipationInRandomDTO dto = new ParticipationInRandomDTO(5, 3, 333, 0000, 20.0);
        repo.saveRandomParticipation(dto);
        List<ParticipationInRandomDTO> participations = repo.getAllRandomParticipationsByUser(333);
        assertEquals(1, participations.size());
        assertEquals(333, participations.get(0).userId);
        assertEquals(5, participations.get(0).getProductId());
        assertEquals(3, participations.get(0).getStoreId());
        assertEquals(0000, participations.get(0).getRandomId());

    }
}
