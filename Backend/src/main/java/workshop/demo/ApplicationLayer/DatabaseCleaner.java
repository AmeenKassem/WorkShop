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
            -- Disable all foreign key constraints
            ALTER TABLE [dbo].[authorization_permissions] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[cart_item] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[user_special_item_cart] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[user_suspensions] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[shopping_cart] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[registered] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[guest] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[offer_permissions] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[offer] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[user_auction_bid] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[auction] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[single_bid_vote_ids] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[single_bid] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[bid] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[receipt_product] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[product] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[review] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[store_stock_items] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[store_stock] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[participation_in_random] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[random] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[orders] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[delayed_notification] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[app_settings_entity] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[composite_sub_discounts] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[composite_discount_entity] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[invisible_discount_entity] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[visible_discount_entity] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[discount_entity] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[active_purcheses] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[node] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[store_tree_entity] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[authorization] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[store] NOCHECK CONSTRAINT ALL;

            -- Disable constraints for new tables
            ALTER TABLE [dbo].[purchase_policy] NOCHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[policy_manager] NOCHECK CONSTRAINT ALL;

            -- Delete all data from all tables
            DELETE FROM [dbo].[authorization_permissions];
            DELETE FROM [dbo].[cart_item];
            DELETE FROM [dbo].[user_special_item_cart];
            DELETE FROM [dbo].[user_suspensions];
            DELETE FROM [dbo].[shopping_cart];
            DELETE FROM [dbo].[registered];
            DELETE FROM [dbo].[guest];
            DELETE FROM [dbo].[offer_permissions];
            DELETE FROM [dbo].[offer];
            DELETE FROM [dbo].[user_auction_bid];
            DELETE FROM [dbo].[auction];
            DELETE FROM [dbo].[single_bid_vote_ids];
            DELETE FROM [dbo].[single_bid];
            DELETE FROM [dbo].[bid];
            DELETE FROM [dbo].[receipt_product];
            DELETE FROM [dbo].[product];
            DELETE FROM [dbo].[review];
            DELETE FROM [dbo].[store_stock_items];
            DELETE FROM [dbo].[store_stock];
            DELETE FROM [dbo].[participation_in_random];
            DELETE FROM [dbo].[random];
            DELETE FROM [dbo].[orders];
            DELETE FROM [dbo].[delayed_notification];
            DELETE FROM [dbo].[app_settings_entity];
            DELETE FROM [dbo].[composite_sub_discounts];
            DELETE FROM [dbo].[composite_discount_entity];
            DELETE FROM [dbo].[invisible_discount_entity];
            DELETE FROM [dbo].[visible_discount_entity];
            DELETE FROM [dbo].[discount_entity];
            DELETE FROM [dbo].[active_purcheses];
            DELETE FROM [dbo].[node];
            DELETE FROM [dbo].[store_tree_entity];
            DELETE FROM [dbo].[authorization];
            DELETE FROM [dbo].[store];

            -- Delete data from new tables
            DELETE FROM [dbo].[purchase_policy];
            DELETE FROM [dbo].[policy_manager];

            -- Re-enable all constraints
            ALTER TABLE [dbo].[authorization_permissions] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[cart_item] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[user_special_item_cart] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[user_suspensions] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[shopping_cart] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[registered] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[guest] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[offer_permissions] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[offer] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[user_auction_bid] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[auction] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[single_bid_vote_ids] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[single_bid] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[bid] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[receipt_product] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[product] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[review] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[store_stock_items] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[store_stock] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[participation_in_random] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[random] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[orders] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[delayed_notification] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[app_settings_entity] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[composite_sub_discounts] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[composite_discount_entity] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[invisible_discount_entity] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[visible_discount_entity] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[discount_entity] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[active_purcheses] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[node] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[store_tree_entity] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[authorization] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[store] CHECK CONSTRAINT ALL;

            ALTER TABLE [dbo].[purchase_policy] CHECK CONSTRAINT ALL;
            ALTER TABLE [dbo].[policy_manager] CHECK CONSTRAINT ALL;

            """);

        String[] statements = sqlBuilder.toString().split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                entityManager.createNativeQuery(trimmed).executeUpdate();
            }
        }
    }
  @Transactional
public void wipeDiscounts() {
    StringBuilder sqlBuilder = new StringBuilder();

    sqlBuilder.append("""
        -- Disable foreign key constraints on discount-related tables and store.discount_id
        ALTER TABLE [dbo].[store] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[composite_sub_discounts] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[composite_discount_entity] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[invisible_discount_entity] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[visible_discount_entity] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[discount_entity] NOCHECK CONSTRAINT ALL;

        -- Clear discount references in store
        UPDATE [dbo].[store] SET discount_id = NULL;

        -- Delete all data from discount-related tables
        DELETE FROM [dbo].[composite_sub_discounts];
        DELETE FROM [dbo].[composite_discount_entity];
        DELETE FROM [dbo].[invisible_discount_entity];
        DELETE FROM [dbo].[visible_discount_entity];
        DELETE FROM [dbo].[discount_entity];

        -- Re-enable constraints
        ALTER TABLE [dbo].[store] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[composite_sub_discounts] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[composite_discount_entity] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[invisible_discount_entity] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[visible_discount_entity] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[discount_entity] CHECK CONSTRAINT ALL;
    """);

    String[] statements = sqlBuilder.toString().split(";");
    for (String statement : statements) {
        String trimmed = statement.trim();
        if (!trimmed.isEmpty()) {
            entityManager.createNativeQuery(trimmed).executeUpdate();
        }
    }
}
    @Transactional
    public void cleanAuthorizationTables() {
        String[] statements = new String[] {
                // Disable constraints
                "ALTER TABLE [dbo].[node] NOCHECK CONSTRAINT ALL",
                "ALTER TABLE [dbo].[authorization_permissions] NOCHECK CONSTRAINT ALL",
                "ALTER TABLE [dbo].[authorization] NOCHECK CONSTRAINT ALL",

                // Delete from referencing table first
                "DELETE FROM [dbo].[node]",

                // Then delete from dependent tables
                "DELETE FROM [dbo].[authorization_permissions]",
                "DELETE FROM [dbo].[authorization]",

                // Re-enable constraints
                "ALTER TABLE [dbo].[node] CHECK CONSTRAINT ALL",
                "ALTER TABLE [dbo].[authorization_permissions] CHECK CONSTRAINT ALL",
                "ALTER TABLE [dbo].[authorization] CHECK CONSTRAINT ALL"
        };

        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                entityManager.createNativeQuery(trimmed).executeUpdate();
            }
        }
    }
@Transactional
public void wipeSpecialsOnly() {
    String sql = """
        ALTER TABLE [dbo].[participation_in_random] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[user_auction_bid] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[single_bid_vote_ids] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[single_bid] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[bid] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[auction] NOCHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[random] NOCHECK CONSTRAINT ALL;

        DELETE FROM [dbo].[participation_in_random];
        DELETE FROM [dbo].[user_auction_bid];
        DELETE FROM [dbo].[single_bid_vote_ids];
        DELETE FROM [dbo].[single_bid];
        DELETE FROM [dbo].[bid];
        DELETE FROM [dbo].[auction];
        DELETE FROM [dbo].[random];

        ALTER TABLE [dbo].[participation_in_random] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[user_auction_bid] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[single_bid_vote_ids] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[single_bid] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[bid] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[auction] CHECK CONSTRAINT ALL;
        ALTER TABLE [dbo].[random] CHECK CONSTRAINT ALL;
    """;

    String[] statements = sql.split(";");
    for (String statement : statements) {
        String trimmed = statement.trim();
        if (!trimmed.isEmpty()) {
            entityManager.createNativeQuery(trimmed).executeUpdate();
        }
    }
}

}
