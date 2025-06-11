package workshop.demo.UnitTests.StoreTests;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
@SpringBootTest
public class OwnershipTests {

    private ISUConnectionRepo repository;
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
        repository = new SUConnectionRepository();

        storeId = 1;
        owner1 = 1;

        repository.addNewStoreOwner(storeId, storeId);

    }

    //for regenerating ID and get the same ID ->1
    void resetIdGenerator() throws Exception {
        Field counterField = StoreRepository.class.getDeclaredField("counterSId");
        counterField.setAccessible(true);
        AtomicInteger counter = (AtomicInteger) counterField.get(null);
        counter.set(1);
    }
    //adding ownership--------------------------------------------------------

    // @Test
    // void testStoreIsAddedToSystem() {
    //     int bossId = 1;
    //     // first store added should be ID 1
    //     Store addedStore = repository.findStoreByID(1);
    //     assertNotNull(addedStore, "Store should be added and retrievable by ID.");
    // }
    @Test
    void testAddOwnershipSuccessfully() throws Exception {
        int newOwnerId = 2;

        // Step 1: Check ownership preconditions
        repository.checkToAddOwner(storeId, owner1, newOwnerId);

        // Step 2: Add ownership
        repository.AddOwnershipToStore(storeId, owner1, newOwnerId);

        // Step 3: Verify that the new owner exists in the store structure
        Node newOwnerNode = repository.getData().getEmployees().get(storeId).getNodeById(newOwnerId);
        assertNotNull(newOwnerNode, "New owner should be added to the store's ownership tree");
        assertFalse(newOwnerNode.getIsManager(), "New owner should be marked as an owner (not a manager)");
        assertEquals(owner1, newOwnerNode.getParentId(), "New owner's parent should be the current owner");
    }

    @Test
    void testChainedOwnershipAdditionSuccessfully() throws Exception {
        int owner2 = 2;
        int owner3 = 3;

        // Owner1 adds Owner2
        repository.checkToAddOwner(storeId, owner1, owner2);
        repository.AddOwnershipToStore(storeId, owner1, owner2);

        // Owner2 adds Owner3
        repository.checkToAddOwner(storeId, owner2, owner3);
        repository.AddOwnershipToStore(storeId, owner2, owner3);

        // Assert that Owner2 and Owner3 were added successfully
        assertNotNull(repository.getData().getEmployees().get(storeId).getNodeById(owner2));
        assertNotNull(repository.getData().getEmployees().get(storeId).getNodeById(owner2));
    }

    @Test //-> it won't pass in 
    void testAddOwnershipFails_StoreDoesNotExist() {
        int nonExistentStoreId = 999;
        int newOwnerId = 2;

        Exception exception = assertThrows(Exception.class, ()
                -> repository.checkToAddOwner(nonExistentStoreId, owner1, newOwnerId)
        );

        assertEquals("store does not exist in supreDS", exception.getMessage());
    }

    @Test
    void testAddOwnershipFails_NotAnOwner() {
        int notAnOwnerId = 99; // This ID was never added as an owner
        int newOwnerId = 2;

        Exception exception = assertThrows(Exception.class, ()
                -> repository.checkToAddOwner(storeId, notAnOwnerId, newOwnerId)
        );

        assertEquals("Owner does not exist in this store", exception.getMessage());
    }

    @Test
    void testAddOwnershipFails_AlreadyAnOwner() throws Exception {
        int newOwnerId = 2;

        // First time should succeed
        repository.checkToAddOwner(storeId, owner1, newOwnerId);
        repository.AddOwnershipToStore(storeId, owner1, newOwnerId);

        // Second time should fail: already an owner
        Exception exception = assertThrows(Exception.class, ()
                -> repository.checkToAddOwner(storeId, owner1, newOwnerId)
        );

        assertEquals("This worker is already an owner/manager", exception.getMessage());
    }

    //delete ownership-------------------------------------------------------------
    @Test
    void testDeleteOwnershipSuccessfully_WithChildrenCleanup() throws Exception {
        int owner2 = 2;
        int owner3 = 3;
        int owner4 = 4;

        // Owner1 adds owner2
        repository.checkToAddOwner(storeId, owner1, owner2);
        repository.AddOwnershipToStore(storeId, owner1, owner2);

        // Owner2 adds owner3
        repository.checkToAddOwner(storeId, owner2, owner3);
        repository.AddOwnershipToStore(storeId, owner2, owner3);

        // Owner2 adds owner4
        repository.checkToAddOwner(storeId, owner2, owner4);
        repository.AddOwnershipToStore(storeId, owner2, owner4);

        // Ensure all nodes exist before deletion
        assertNotNull(repository.getData().getEmployees().get(storeId).getNodeById(owner2));
        assertNotNull(repository.getData().getEmployees().get(storeId).getNodeById(owner3));
        assertNotNull(repository.getData().getEmployees().get(storeId).getNodeById(owner4));

        // Delete Owner2
        repository.DeleteOwnershipFromStore(storeId, owner1, owner2);

        // Ensure owner2, owner3, and owner4 are removed
        assertNull(repository.getData().getEmployees().get(storeId).getNodeById(owner2));
        assertNull(repository.getData().getEmployees().get(storeId).getNodeById(owner3));
        assertNull(repository.getData().getEmployees().get(storeId).getNodeById(owner4));
    }

    @Test
    void testDeleteOwnershipFails_OwnerDoesNotOwnStore() {
        int owner2 = 2;

        // Try delete without adding Owner2
        Exception exception = assertThrows(Exception.class, () -> {
            repository.DeleteOwnershipFromStore(storeId, owner1, owner2);
        });

        assertEquals("Cannot delete: user is not an owner", exception.getMessage());
    }

    @Test
    void testDeleteOwnershipFails_NotTheGiver() throws Exception {
        int owner2 = 2;
        int owner3 = 3;

        // Owner1 adds Owner2
        repository.checkToAddOwner(storeId, owner1, owner2);
        repository.AddOwnershipToStore(storeId, owner1, owner2);

        // Owner2 adds Owner3
        repository.checkToAddOwner(storeId, owner2, owner3);
        repository.AddOwnershipToStore(storeId, owner2, owner3);

        // Owner1 tries to delete Owner3 (who was added by Owner2)
        Exception exception = assertThrows(Exception.class, () -> {
            repository.DeleteOwnershipFromStore(storeId, owner1, owner3);
        });

        assertEquals(String.format("You do not own this ownership", owner1, owner3),
                exception.getMessage());
    }

}
