package workshop.demo.UnitTests.StoreTests;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import workshop.demo.DemoApplication;
import workshop.demo.ApplicationLayer.DatabaseCleaner;
import workshop.demo.DomainLayer.StoreUserConnection.Authorization;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;

@SpringBootTest(
    classes = DemoApplication.class
)
@ActiveProfiles("test")
@Transactional
public class ManagerTests {
    @Autowired
    private ISUConnectionRepo repository;
    private int storeId = 1;
    private int owner1 = 1;
    @Autowired
    DatabaseCleaner data;

    @BeforeEach
    void setUp() {
        data.wipeDatabase();
        try {
            // resetIdGenerator();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        storeId = 1;
        owner1 = 1;
        repository.addNewStoreOwner(storeId, storeId);

        // repository.add(owner1, "TechStore", "Electronics");
    }
    // for regenerating ID and get the same ID ->1

    // void resetIdGenerator() throws Exception {
    // // Field counterField = StoreRepository.class.getDeclaredField("counterSId");
    // counterField.setAccessible(true);
    // AtomicInteger counter = (AtomicInteger) counterField.get(null);
    // counter.set(1);
    // }
    // adding ownership--------------------------------------------------------

    @Test
    void testAddMenegerSuccessfully() throws Exception {
        int newManager = 2;

        // Step 1: Check ownership preconditions
        repository.checkToAddManager(storeId, owner1, newManager);

        // Step 2: Add ownership
        repository.AddManagerToStore(storeId, owner1, newManager);

        // Step 3: Verify that the new owner exists in the store structure
        Node newManagerNode = repository.getData().getEmployees().get(storeId).getNodeById(newManager);
        assertNotNull(newManagerNode, "New owner should be added to the store's ownership tree");
        assertTrue(newManagerNode.getIsManager(), "New owner should be marked as a manager");
        assertEquals(owner1, newManagerNode.getParentId(), "New owner's parent should be the current owner");
        assertNotNull(newManagerNode.getMyAuth(), "Manager node should have a non-null Authorization object");

    }

    @Test
    void testAddMenegerFails_AlreadyAManager() throws Exception {
        int managerId = 2;

        // First time should succeed
        repository.checkToAddManager(storeId, owner1, managerId);
        repository.AddManagerToStore(storeId, owner1, managerId);

        // Second time should fail: already an owner
        Exception exception = assertThrows(Exception.class,
                () -> repository.checkToAddManager(storeId, owner1, managerId));

        assertEquals("This worker is already an owner/manager", exception.getMessage());
    }

    @Test
    void testDeleteManagerFails_ManagerDoesNotManagerStore() {
        int owner2 = 2;

        // Try delete without adding Owner2
        Exception exception = assertThrows(Exception.class, () -> {
            repository.deleteManager(storeId, owner1, owner2);
        });

        assertEquals("Manager not found", exception.getMessage());
    }

    @Test
    void testDeleteMangaerFails_NotTheGiver() throws Exception {
        int owner2 = 2;
        int manager1 = 3;

        // Owner1 adds Owner2
        repository.checkToAddManager(storeId, owner1, owner2);
        repository.AddOwnershipToStore(storeId, owner1, owner2);

        // Owner2 adds manager1
        repository.checkToAddManager(storeId, owner2, manager1);
        repository.AddManagerToStore(storeId, owner2, manager1);

        // Owner1 tries to delete manager1 (who was added by Owner2)
        Exception exception = assertThrows(Exception.class, () -> {
            repository.deleteManager(storeId, owner1, manager1);
        });

        assertEquals(String.format("Owner does not have permission to delete this manager", owner1, manager1),
                exception.getMessage());
    }

    @Test
    void testChangePermissionsByCorrectOwner() throws Exception {
        int newManagerId = 5;
        List<Permission> newPermissions = List.of(Permission.AddToStock, Permission.UpdateQuantity);

        // Owner1 adds the manager
        repository.checkToAddManager(storeId, owner1, newManagerId);
        repository.AddManagerToStore(storeId, owner1, newManagerId);

        // Owner1 updates the manager's permissions
        repository.changePermissions(owner1, newManagerId, storeId, newPermissions);

        // Verify that the manager's node has the updated permissions
        Node managerNode = repository.getData().getEmployees().get(storeId).getNodeById(newManagerId);
        Authorization auth = managerNode.getMyAuth();
        assertNotNull(auth, "Manager should have a non-null authorization object");

        for (Permission permission : newPermissions) {
            assertTrue(auth.hasAutho(permission), "Manager should have permission: " + permission);
        }
    }

    @Test
    void testChangePermissionsByWrongOwnerThrows() throws Exception {
        int newManagerId = 6;
        List<Permission> updatedPermissions = List.of(Permission.DeleteFromStock);

        // Owner1 adds the manager
        repository.checkToAddManager(storeId, owner1, newManagerId);
        repository.AddManagerToStore(storeId, owner1, newManagerId);

        // Another owner (owner2) tries to update the manager's permissions
        Node owner2 = new Node(1, 99, false, null); // Not in the tree
        Exception exception = assertThrows(Exception.class, () -> {
            repository.changePermissions(owner2.getMyId(), newManagerId, storeId, updatedPermissions);
        });

        assertEquals(String.format("Owner does not have permission to change this manager", owner2, newManagerId),
                exception.getMessage());

    }

}
