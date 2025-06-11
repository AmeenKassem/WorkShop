package workshop.demo.UnitTests.StoreTests;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
@SpringBootTest
public class NodeTests {

    private Node owner1;
    private Node owner12;
    private Node manager1;
    private Node owner123;

    @BeforeEach
    void setUp() throws UIException {

        owner1 = new Node(1, false, 0);
        owner12 = new Node(2, false, 1);//child of owner1
        owner123 = new Node(3, false, 2);//child of owner 2
        manager1 = new Node(10, true, 1);// manager by owner 1
        owner1.addChild(manager1);
        owner1.addChild(owner12);
        owner12.addChild(owner123);
    }

    @Test
    void testOwnersAreFullyAuthorized() {
        assertNull(owner1.getMyAuth(), "Owner1 should be fully authorized");
        assertNull(owner12.getMyAuth(), "Owner12 should be fully authorized");
        assertNull(owner123.getMyAuth(), "Owner123 should be fully authorized");
    }

    @Test
    void testManagerIsNotFullyAuthorized() {
        assertNotNull(manager1.getMyAuth(), "Manager1 should have a non-null Authorization");
    }

    @Test
    void testManagerHasAuthorization() {
        assertNotNull(manager1.getMyAuth(), "Manager should have an Authorization object");
        Map<Permission, Boolean> authMap = manager1.getMyAuth().getMyAutho();
        for (Boolean val : authMap.values()) {
            assertFalse(val, "All permissions should initially be false");
        }
    }

    @Test
    void testAddAuthorizationToManagerByParentOnly() {
        List<Permission> permsToAdd = Arrays.asList(Permission.ViewAllProducts, Permission.AddToStock);

        // Case 1: Correct parent (owner1) adds permissions to manager1
        try {
            manager1.addAuthrization(permsToAdd, owner1.getMyId());
            assertTrue(manager1.getMyAuth().hasAutho(Permission.ViewAllProducts));
            assertTrue(manager1.getMyAuth().hasAutho(Permission.AddToStock));
        } catch (Exception e) {
            fail("owner1 should be allowed to modify manager1's authorization");
        }

        // Case 2: Incorrect parent (owner12) tries to add permissions
        Exception exception = assertThrows(Exception.class, () -> {
            manager1.addAuthrization(permsToAdd, owner12.getMyId());
        });

        assertEquals("This owner cannot manipulate authorization for this manager", exception.getMessage());
    }

    @Test
    void testUpdateAuthorizationTogglesPermissions() {
        List<Permission> permsToToggle = Arrays.asList(Permission.ViewAllProducts, Permission.DeleteFromStock);

        // First, owner1 adds these permissions to manager1
        try {
            manager1.addAuthrization(permsToToggle, owner1.getMyId());
        } catch (Exception e) {
            fail("owner1 should be able to add initial permissions to manager1");
        }

        // Confirm the permissions are true
        assertTrue(manager1.getMyAuth().hasAutho(Permission.ViewAllProducts));
        assertTrue(manager1.getMyAuth().hasAutho(Permission.DeleteFromStock));

        // Now toggle them using updateAuthorization via correct parent
        try {
            manager1.updateAuthorization(permsToToggle, owner1.getMyId());
        } catch (Exception e) {
            fail("owner1 should be able to update permissions for manager1");
        }

        // Confirm the permissions are now false
        assertFalse(manager1.getMyAuth().hasAutho(Permission.ViewAllProducts));
        assertFalse(manager1.getMyAuth().hasAutho(Permission.DeleteFromStock));

        // Try with incorrect parent, should throw
        Exception exception = assertThrows(Exception.class, () -> {
            manager1.updateAuthorization(permsToToggle, owner12.getMyId());
        });

        assertEquals("This owner cannot manipulate authorization for this manager", exception.getMessage());
    }

}
