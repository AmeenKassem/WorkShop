package workshop.demo.StoreTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.ActivePurcheses;

@SpringBootTest
public class ActivePurchasesTest {

    private ActivePurcheses active = new ActivePurcheses(0); // or @Autowired if it's a Spring bean

    public int setAuction() throws Exception{
        active = new ActivePurcheses(0);
        return active.addProductToAuction(0, 1, 1000);
    }

    @Test
    public void testAddAuction() {
        try {
            int id = setAuction();
            AuctionDTO[] auctions = active.getAuctions();

            Assertions.assertNotNull(auctions);
            Assertions.assertNotNull(auctions[0]);
            Assertions.assertEquals(id, auctions[0].auctionId);

            SingleBid looserBid = active.addUserBidToAuction(id, 0, 10);
            SingleBid winnerBid = active.addUserBidToAuction(id, 1, 11);

            Thread.sleep(1000); // correct sleep usage

            Assertions.assertFalse(looserBid.isWon());
            Assertions.assertTrue(winnerBid.isWon());

        } catch (Exception ex) {
            ex.printStackTrace();
            Assertions.fail("Exception thrown: " + ex.getMessage());
        }
    }

    SingleBid loser;
    SingleBid winner;
    @Test
    public void testConcurrencyAuction(){
        try {
            for(int i=0;i<10;i++){
                int id = setAuction();
                // SingleBid loser;
                // SingleBid winner;
                Thread t1 =new Thread(()->{
                    try {
                        loser= active.addUserBidToAuction(id, 0, 10);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                Thread t2 =new Thread(()->{
                    try {
                        winner = active.addUserBidToAuction(id, 1, 11);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                if(Math.random()<0.5){
                    t1.start();
                    t2.start();
                    Thread.sleep(1100);
                    Assertions.assertTrue(winner.isWon());
                }else{
                    t2.start();
                    t1.start();
                    Thread.sleep(1100);
                    Assertions.assertTrue(winner.isWon());
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.getMessage());
            Assertions.assertFalse(true);
        }
    } 


    @Test
    public void testBid(){
        try {
            int BidId = active.addProductToBid(0, 1);
            SingleBid bid = active.addUserBidToBid(BidId, 0, 100);
            SingleBid failedBid =null;
            try{
                failedBid = active.addUserBidToBid(BidId, 0, 100);
                Assertions.assertTrue(false);
            }catch(UIException e){
                // Assertions.assertTrue(true);
                Assertions.assertTrue(failedBid==null);
            }
            active.rejectBid(BidId, 0);
            Assertions.assertFalse(bid.isWon());
            SingleBid secondBid = active.addUserBidToBid(BidId,0,100);
            active.acceptBid(secondBid.getId(), BidId);
            Assertions.assertTrue(secondBid.isWon());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.getMessage());
            Assertions.assertTrue(false);
        }
    } 
}
