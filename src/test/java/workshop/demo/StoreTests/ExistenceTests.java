package workshop.demo.StoreTests;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import workshop.demo.InfrastructureLayer.StoreRepository;

public class ExistenceTests {

    private StoreRepository repository;
    private int storeId = 1;
    private int owner1 = 1;

    @BeforeEach
    void setUp() {
        try {
            resetIdGenerator();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        repository = new StoreRepository();

        repository.addStoreToSystem(owner1, "TechStore", "Electronics");

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
        assertTrue(repository.findStoreByID(storeId).isActive());
        List<Integer> workerIds = repository.deactivateStore(storeId, owner1);
        // Assert store is now inactive
        assertFalse(repository.findStoreByID(storeId).isActive());
        assertEquals(1, workerIds.size());
        //after notfiing must check it I got notfied 
    }

    @Test
    void testDeactivateStoreByAnotherOwner() throws Exception {
        int storeId = 1;
        int owner2 = 2;
        repository.AddOwnershipToStore(storeId, owner1, owner2);
        // Assert that the store is active before deactivation
        assertTrue(repository.findStoreByID(storeId).isActive());
        // Expect an exception when a non-root owner tries to deactivate the store
        Exception exception = assertThrows(Exception.class, ()
                -> repository.deactivateStore(storeId, owner2)
        );

        assertEquals("only the boss/main owner can deactivate the store", exception.getMessage());

        // Assert that the store is still active
        assertTrue(repository.findStoreByID(storeId).isActive());
    }

}
