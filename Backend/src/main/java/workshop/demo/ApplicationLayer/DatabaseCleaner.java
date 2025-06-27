package workshop.demo.ApplicationLayer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void wipeDatabase() {
        // Get entity table names



        // Disable constraints
        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("""
                    DELETE FROM dbo.[authorization_permissions];
                    DELETE FROM dbo.[authorization];
                    DELETE FROM dbo.[cart_item];
                    DELETE FROM dbo.[user_special_item_cart];
                    DELETE FROM dbo.[user_suspensions];
                    DELETE FROM dbo.[shopping_cart];
                    DELETE FROM dbo.[registered];
                    DELETE FROM dbo.[guest];
                    DELETE FROM dbo.[offer_permissions];
                    DELETE FROM dbo.[offer];
                    DELETE FROM dbo.[auction];
                    DELETE FROM dbo.[node];
                    DELETE FROM dbo.[product];
                    DELETE FROM dbo.[store_stock_items];
                    DELETE FROM dbo.[store_stock];
                    DELETE FROM dbo.[store_tree_entity];
                    DELETE FROM dbo.[store];
                """);

        // Execute all in one call
        String finalSql = sqlBuilder.toString();
        entityManager.createNativeQuery(finalSql).executeUpdate();
    }
}
