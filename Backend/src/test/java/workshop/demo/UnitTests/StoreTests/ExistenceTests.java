package workshop.demo.UnitTests.StoreTests;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;

@SpringBootTest
@ActiveProfiles("test")
public class ExistenceTests {

    private ISUConnectionRepo repository;
    private IStoreRepo storeRepo;
    private int storeId = 1;
    private int owner1 = 1;

    @BeforeEach
    void setUp() throws UIException {
        try {
            resetIdGenerator();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        repository = new SUConnectionRepository();
        storeRepo = new StoreRepository();
        storeRepo.addStoreToSystem(owner1, "TechStore", "Electronics");
        repository.addNewStoreOwner(storeId, storeId);

    }
    //for regenerating ID and get the same ID ->1

    void resetIdGenerator() throws Exception {
        Field counterField = StoreRepository.class.getDeclaredField("counterSId");
        counterField.setAccessible(true);
        AtomicInteger counter = (AtomicInteger) counterField.get(null);
        counter.set(1);
    }

    @Test
    void testDeactivateStoreByMainOwner() throws Exception {
        int storeId = 1;
        // Assert that the store is active before deactivation
        assertTrue(storeRepo.findStoreByID(storeId).isActive());
        storeRepo.deactivateStore(storeId, owner1);
        // Assert store is now inactive
        assertFalse(storeRepo.findStoreByID(storeId).isActive());
        //assertEquals(1, workerIds.size());
        //after notfiing must check it I got notfied 
    }

    @Test
    void testDeactivateStoreByAnotherOwner() throws Exception {
        int storeId = 1;
        int owner2 = 2;
        repository.AddOwnershipToStore(storeId, owner1, owner2);
        // Assert that the store is active before deactivation
        assertTrue(storeRepo.findStoreByID(storeId).isActive());
        assertFalse(repository.checkDeactivateStore(storeId, owner2));
        // Assert that the store is still active
        assertTrue(storeRepo.findStoreByID(storeId).isActive());
    }

}
