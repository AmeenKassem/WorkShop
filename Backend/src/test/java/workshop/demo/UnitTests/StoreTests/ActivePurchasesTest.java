package workshop.demo.UnitTests.StoreTests;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.ActivePurcheses;

@SpringBootTest
public class ActivePurchasesTest {

    private ActivePurcheses active = new ActivePurcheses(0); // or @Autowired if it's a Spring bean

    public int setAuction() throws Exception {
        active = new ActivePurcheses(0);
        return active.addProductToAuction(0, 1, 1000);
    }

    public int setRandom() throws Exception {
        active = new ActivePurcheses(0);
        return active.addProductToRandom(0, 1, 20.0, 0, 10000);
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

            Thread.sleep(1100); // correct sleep usage

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
    public void testConcurrencyAuction() {
        try {
            for (int i = 0; i < 10; i++) {
                int id = setAuction();
                // SingleBid loser;
                // SingleBid winner;
                Thread t1 = new Thread(() -> {
                    try {
                        loser = active.addUserBidToAuction(id, 0, 10);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                Thread t2 = new Thread(() -> {
                    try {
                        winner = active.addUserBidToAuction(id, 1, 11);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                if (Math.random() < 0.5) {
                    t1.start();
                    t2.start();
                    Thread.sleep(1100);
                    Assertions.assertTrue(winner.isWon());
                } else {
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
    public void testBid() {
        try {
            int BidId = active.addProductToBid(0, 1);
            SingleBid bid = active.addUserBidToBid(BidId, 0, 100);
            active.rejectBid(bid.getId(), BidId);
            Assertions.assertFalse(bid.isWon());
            SingleBid secondBid = active.addUserBidToBid(BidId, 0, 100);
            active.acceptBid(secondBid.getId(), BidId);
            Assertions.assertTrue(secondBid.isWon());
            try {
                SingleBid nullBid = active.addUserBidToBid(BidId, 0, 10);
                Assertions.assertTrue(false);
            } catch (UIException ex) {
                Assertions.assertTrue(true);

            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.getMessage());
            Assertions.assertTrue(false);
        }
    }

    @Test
    public void TestAddRandom() {
        try {
            int id = setRandom();
            Assertions.assertTrue(id > 0);
            ParticipationInRandomDTO user0 = active.participateInRandom(0, id, 15.0);
            ParticipationInRandomDTO user1 = active.participateInRandom(1, id, 5.0);

            Assertions.assertTrue(user0.won() || user1.won(), "One of the users should have won the random");

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void TestAddRandomFail() {
        Assertions.assertThrows(UIException.class, () -> {
            active.addProductToAuction(0, 0, 10000);
        }, "Expected participateInRandom to throw, but it didn't");
    }

    @Test
    public void TestAddRandomFail2() {
        Assertions.assertThrows(UIException.class, () -> {
            active.addProductToAuction(0, 20, 0);
        }, "Expected participateInRandom to throw, but it didn't");
    }

    @Test
    public void TestParticipateInRandom() {
        try {
            int id = setRandom();
            ParticipationInRandomDTO user0 = active.participateInRandom(0, id, 15.0);
            ParticipationInRandomDTO user1 = active.participateInRandom(1, id, 5.0);
            Assertions.assertTrue(user0.won() || user1.won(), "One of the users should have won the random");
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void TestParticipateInRandomFail() {
        try {
            int id = setRandom();
            UIException ex = Assertions.assertThrows(UIException.class, () -> {
                active.participateInRandom(0, id, 20.5);
            }, "Expected participateInRandom to throw, but it didn't");
            Assertions.assertEquals("Maximum amount you can pay is: " + active.getRandom(id).getAmountLeft(), ex.getMessage());;
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }

    }

    @Test
    public void TestParticipateInRandomFail2() {
        try {
            int id = setRandom();
            UIException ex = Assertions.assertThrows(UIException.class, () -> {
                active.participateInRandom(0, id, 0);
            }, "Expected participateInRandom to throw, but it didn't");
            Assertions.assertEquals("Product price must be positive!", ex.getMessage());;
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }

    }

    @Test
    public void TestParticipateInRandomFail3() {
        try {
            int id = setRandom();
            active.participateInRandom(0, id, 15.0);
            active.participateInRandom(1, id, 5.0);
            UIException ex = Assertions.assertThrows(UIException.class, () -> {
                active.participateInRandom(2, id, 10.0);
            }, "Expected participateInRandom to throw, but it didn't");
            DevException ex2 = Assertions.assertThrows(DevException.class, () -> {
                active.getRandom(id);
            }, "Expected participateInRandom to throw, but it didn't");
            Assertions.assertEquals("Random has ended!", ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void TestParticipateInRandomFail4() {
        try {
            int id = active.addProductToRandom(0, 1, 20.0, 0, 10);
            Thread.sleep(1000);
            UIException ex = Assertions.assertThrows(UIException.class, () -> {
                active.participateInRandom(0, id, 15.0);
            }, "Expected participateInRandom to throw, but it didn't");
            Assertions.assertEquals("Random has ended!", ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void concurrencyTest1() {
        try {
            int id = setRandom();
            boolean[] isSuccess = new boolean[2];
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(2); // 2 threads to wait for
            Thread t1 = new Thread(() -> {
                try {
                    //startSignal.await(); // wait for the signal to start
                    active.participateInRandom(0, id, 20.0);
                    isSuccess[0] = true;
                    // doneSignal.countDown(); // signal that this thread is done
                } catch (Exception e) {
                    e.printStackTrace();
                    isSuccess[0] = false;
                }
            });
            Thread t2 = new Thread(() -> {
                try {
                    //startSignal.await(); // wait for the signal to start
                    active.participateInRandom(1, id, 20.0);
                    isSuccess[1] = true;
                    //doneSignal.countDown(); // signal that this thread is done
                } catch (Exception e) {
                    e.printStackTrace();
                    isSuccess[1] = false;
                }
            });
            t1.start();
            t2.start();
            t1.join(); // wait for t1 to finish
            t2.join(); // wait for t2 to finish
            //startSignal.countDown(); // let both threads proceed
            //doneSignal.await(); // wait for both threads to finish
            Assertions.assertTrue(active.getRandom(id).getAmountLeft() <= 0, "The random should be over");
            Assertions.assertFalse(active.getRandom(id).isActive());
            Assertions.assertTrue(isSuccess[0] || isSuccess[1], "At least one thread should have succeeded");
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }
    @Test
    public void concurrencyTest2() {
        try {
            int id = setRandom();
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(2); // 2 threads to wait for
            Thread t1 = new Thread(() -> {
                try {
                    startSignal.await(); // wait for the signal to start
                    active.participateInRandom(0, id, 10.0);
                    doneSignal.countDown(); // signal that this thread is done 
                } catch (Exception e) {
                    e.printStackTrace();

                }
            });
            Thread t2 = new Thread(() -> {
                try {
                    startSignal.await(); // wait for the signal to start
                    active.participateInRandom(1, id, 5.0);
                    doneSignal.countDown(); // signal that this thread is done
                } catch (Exception e) {
                    e.printStackTrace();

                }
            });
            t1.start();
            t2.start();
            //t1.join(); // wait for t1 to finish
            //t2.join(); // wait for t2 to finish
            startSignal.countDown(); // let both threads proceed
            doneSignal.await(); // wait for both threads to finish
            Assertions.assertTrue(active.getRandom(id).getAmountLeft() == 5.0);
            Assertions.assertTrue(active.getRandom(id).isActive());

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

}
