package workshop.demo.ConcurrencyTests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.SuperDataStructure;

@SpringBootTest
@ActiveProfiles("test")
public class ManagementTests {

    @Autowired
    private SuperDataStructure data;

    //StoreRepository repository;
    int storeId = 1;
    int bossId = 100;
    int owner1Id = 101;
    int owner2Id = 102;
    int newOwnerId = 200;

    @BeforeEach
    void setUp() {
        data = new SuperDataStructure();
        data.addNewStore(storeId, bossId);
    }

    @Test
    public void testOnlyOneOwnerCanAssignSameNewOwnerConcurrently() throws Exception {
        data.addNewOwner(storeId, bossId, owner1Id);
        data.addNewOwner(storeId, bossId, owner2Id);
        //2 owners make offer to the same person to be an owner:
        data.checkToAddOwner(storeId, owner1Id, newOwnerId);
        data.checkToAddOwner(storeId, owner2Id, newOwnerId);

        //simulate that the preson accept for the both offers:
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Boolean>> results = new ArrayList<>();

        Callable<Boolean> fromOwner1 = () -> {
            try {
                data.addNewOwner(storeId, owner1Id, newOwnerId);
                System.out.println("Owner1 succeeded");
                return true;
            } catch (Exception e) {
                System.out.println("Owner1 failed: " + e.getMessage());
                return false;
            }
        };
        Callable<Boolean> fromOwner2 = () -> {
            try {
                data.addNewOwner(storeId, owner2Id, newOwnerId);
                System.out.println("Owner2 succeeded");
                return true;
            } catch (Exception e) {
                System.out.println("Owner2 failed: " + e.getMessage());
                return false;
            }
        };

        results.add(executor.submit(fromOwner1));
        results.add(executor.submit(fromOwner2));

        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);
        int successCount = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                successCount++;
            }
        }

        assertEquals(1, successCount, "Only one owner should succeed in assigning the new owner");
        // confirnm that only persant (first acceptance) assigned:
        Node newOwnerNode = data.getEmployees().get(storeId).getNodeById(newOwnerId);
        assertNotNull(newOwnerNode, "New owner must exist in the tree");
        int parent = newOwnerNode.getParentId();
        assertTrue(parent == owner1Id || parent == owner2Id, "Parent must be one of the two owners");
        long count = data.getAllWorkers(storeId).stream()
                .filter(n -> n.getMyId() == newOwnerId)
                .count();
        assertEquals(1, count, "New owner should only be added once to the tree");
    }

    @Test
    public void testOnlyOneOwnerCanAssignSameNewManagerConcurrently() throws Exception {
        data.addNewOwner(storeId, bossId, owner1Id);
        data.addNewOwner(storeId, bossId, owner2Id);

        //2 owners make offer to the same person to be a manager:
        data.checkToAddManager(storeId, owner1Id, newOwnerId);
        data.checkToAddManager(storeId, owner2Id, newOwnerId);

        //simulate that the preson accept for the both offers:
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<Boolean>> results = new ArrayList<>();

        Callable<Boolean> fromOwner1 = () -> {
            try {
                data.addNewManager(storeId, owner1Id, newOwnerId);
                System.out.println("Owner1 assigned manager");
                return true;
            } catch (Exception e) {
                System.out.println("Owner1 failed: " + e.getMessage());
                return false;
            }
        };

        Callable<Boolean> fromOwner2 = () -> {
            try {
                data.addNewManager(storeId, owner2Id, newOwnerId);
                System.out.println("Owner2 assigned manager");
                return true;
            } catch (Exception e) {
                System.out.println("Owner2 failed: " + e.getMessage());
                return false;
            }
        };

        results.add(executor.submit(fromOwner1));
        results.add(executor.submit(fromOwner2));

        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);

        //only one should succeed
        int successCount = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                successCount++;
            }
        }

        assertEquals(1, successCount, "Only one owner should succeed in assigning the new manager");

        Node managerNode = data.getEmployees().get(storeId).getNodeById(newOwnerId);
        assertNotNull(managerNode, "Manager must exist in the tree");
        assertTrue(managerNode.getIsManager(), "User should be marked as manager");

        int parent = managerNode.getParentId();
        assertTrue(parent == owner1Id || parent == owner2Id, "Manager's parent should be one of the owners");

        long count = data.getAllWorkers(storeId).stream()
                .filter(n -> n.getMyId() == newOwnerId)
                .count();
        assertEquals(1, count, "Manager should be added only once to the tree");
    }

}
