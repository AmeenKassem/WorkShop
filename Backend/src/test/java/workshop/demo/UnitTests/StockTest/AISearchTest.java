package workshop.demo.UnitTests.StockTest;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.InfrastructureLayer.AISearch;
import workshop.demo.InfrastructureLayer.StockRepository;

@SpringBootTest
public class AISearchTest {

        private AISearch ai = new AISearch();
        private StockRepository stockRepo = new StockRepository();
        private boolean storeInited=true;

        @Test
        public void test() throws UIException, DevException {
                addProduct("Echo Dot (5th Gen)", Category.HOME, "Smart speaker with Alexa",
                                new String[] { "alexa", "speaker", "smart" });

                addProduct("Levi’s Denim Jacket", Category.FASHION, "Classic blue denim jacket",
                                new String[] { "jacket", "denim", "levis" });

                addProduct("Nikon Z6 II", Category.CAMERA, "Mirrorless camera with full-frame sensor",
                                new String[] { "nikon", "camera", "mirrorless" });

                addProduct("iRobot Roomba i7+", Category.HOME_APPLIANCE, "Self-emptying robot vacuum",
                                new String[] { "roomba", "vacuum", "robot" });

                addProduct("Ninja Foodi Grill", Category.KITCHEN, "Indoor electric grill and air fryer",
                                new String[] { "ninja", "grill", "kitchen" });

                addProduct("Husqvarna Lawn Mower", Category.GARDEN, "Self-propelled gas lawn mower",
                                new String[] { "lawn", "mower", "husqvarna" });

                addProduct("Ergonomic Office Chair", Category.OFFICE, "Adjustable mesh desk chair",
                                new String[] { "office", "chair", "ergonomic" });

                addProduct("Michelin Pilot Sport Tires", Category.AUTOMOTIVE, "High-performance summer tires",
                                new String[] { "michelin", "tires", "car" });

                addProduct("LEGO Star Wars Set", Category.TOYS, "Collectible building set for kids and adults",
                                new String[] { "lego", "star wars", "toys" });

                addProduct("Sony WH-CH720N", Category.ELECTRONICS, "Noise-cancelling wireless headphones",
                                new String[] { "sony", "headphones", "wireless" });

                System.out.println(System.currentTimeMillis() + " time now");
                double timer = System.currentTimeMillis();
                try {
                        ItemStoreDTO[] res1 = stockRepo
                                        .search(new ProductSearchCriteria(null, null, "samsung", 1, null, null, null,
                                                        null));
                        System.out.println(res1.length);
                        for (ItemStoreDTO itemStoreDTO : res1) {
                                System.out.println(itemStoreDTO.toString());
                        }

                } catch (UIException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

                System.out.println(System.currentTimeMillis() + " time now");

                try {
                        ItemStoreDTO[] res2 = stockRepo
                                        .search(new ProductSearchCriteria(null, null, "shoes", 1, null, null, null,
                                                        null));
                        for (ItemStoreDTO itemStoreDTO : res2) {
                                System.out.println(itemStoreDTO.toString());
                        }

                } catch (UIException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

                System.out.println(System.currentTimeMillis() + " time now");
                System.out.println("average search time :" + (timer - System.currentTimeMillis()) / 1000 + " sec");
        }

        @Test
        public void addOneProduct() throws UIException, DevException {
                addProduct("Nike ZoomX Vaporfly", Category.FASHION, "High-performance running shoes",
                                new String[] { "nike", "running", "shoes" });
        }

        private void addProduct(String string, Category electronics, String string2, String[] strings)
                        throws UIException, DevException {

                int id = stockRepo.addProduct(string, electronics, string2, strings);
                if (storeInited){
                        stockRepo.addStore(1);
                        storeInited=false;
                }
                
                // try {
                stockRepo.addItem(1, id, 10, 5, electronics);
        }

}
