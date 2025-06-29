package workshop.demo.ApplicationLayer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void wipeDatabase() {
        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("""
         DELETE FROM dbo.[authorization_permissions];
         DELETE FROM dbo.[cart_item];
         DELETE FROM dbo.[user_special_item_cart];
         DELETE FROM dbo.[user_suspensions];
         DELETE FROM dbo.[shopping_cart];
         DELETE FROM dbo.[registered];
         DELETE FROM dbo.[guest];
         DELETE FROM dbo.[offer_permissions];
         DELETE FROM dbo.[offer];
         DELETE FROM dbo.[user_auction_bid];
         DELETE FROM dbo.[auction];
         DELETE FROM dbo.[single_bid_vote_ids];
         DELETE FROM dbo.[single_bid];
         DELETE FROM dbo.[bid];
         DELETE FROM dbo.[receipt_product];
         DELETE FROM dbo.[product];
         DELETE FROM dbo.[review];
         DELETE FROM dbo.[store_stock_items];
         DELETE FROM dbo.[store_stock];
         DELETE FROM dbo.[participation_in_random];
         DELETE FROM dbo.[random];
         DELETE FROM dbo.[orders];
         DELETE FROM dbo.[delayed_notification];
         DELETE FROM dbo.[app_settings_entity];
         DELETE FROM dbo.[composite_sub_discounts];
         DELETE FROM dbo.[composite_discount_entity];
         DELETE FROM dbo.[invisible_discount_entity];
         DELETE FROM dbo.[visible_discount_entity];
         DELETE FROM dbo.[discount_entity];
         DELETE FROM dbo.[active_purcheses];
         DELETE FROM dbo.[node];
         DELETE FROM dbo.[store_tree_entity];
         DELETE FROM dbo.[authorization];
         DELETE FROM dbo.[store];
        """);

        // ✅ Keep same execution style — split and run each
        String[] statements = sqlBuilder.toString().split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                entityManager.createNativeQuery(trimmed).executeUpdate();
            }
        }
    }
}
