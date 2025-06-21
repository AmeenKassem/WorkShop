package workshop.demo.IntegrationTests.ServiceTests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSchemaLoader {

    private static final String JDBC_URL = "jdbc:sqlserver://ws-server.database.windows.net:1433;database=test_assi_bshar;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    private static final String JDBC_USER = "rahaf";
    private static final String JDBC_PASSWORD = "RaPass2025";

    public static void main(String[] args) {
        String fullSchema = """
            CREATE TABLE [dbo].[auction] (
                [auction_id] INT IDENTITY (1, 1) NOT NULL,
                [end_time_millis] BIGINT NOT NULL,
                [max_bid] FLOAT (53) NOT NULL,
                [product_id] INT NOT NULL,
                [quantity] INT NOT NULL,
                [status] SMALLINT NULL,
                [store_id] INT NOT NULL,
                PRIMARY KEY CLUSTERED ([auction_id] ASC),
                CHECK ([status]>=(0) AND [status]<=(1))
            );
            GO
            CREATE TABLE [dbo].[authorization] (
                [id] BIGINT IDENTITY (1, 1) NOT NULL,
                PRIMARY KEY CLUSTERED ([id] ASC)
            );
            GO
            CREATE TABLE [dbo].[authorization_permissions] (
                [auth_id] BIGINT NOT NULL,
                [is_authorized] BIT NULL,
                [my_autho_key] VARCHAR (255) NOT NULL,
                PRIMARY KEY CLUSTERED ([auth_id] ASC, [my_autho_key] ASC),
                CHECK ([my_autho_key]='MANAGE_PURCHASE_POLICY' OR [my_autho_key]='MANAGE_STORE_POLICY' OR [my_autho_key]='SpecialType' OR [my_autho_key]='UpdatePrice' OR [my_autho_key]='UpdateQuantity' OR [my_autho_key]='DeleteFromStock' OR [my_autho_key]='AddToStock' OR [my_autho_key]='ViewAllProducts'),
                CONSTRAINT [FKe7lwp8hkb9wh85vsilo4oxs8k] FOREIGN KEY ([auth_id]) REFERENCES [dbo].[authorization] ([id])
            );
            GO
            CREATE TABLE [dbo].[guest] (
                [id] INT IDENTITY (1, 1) NOT NULL,
                PRIMARY KEY CLUSTERED ([id] ASC)
            );
            GO
            CREATE TABLE [dbo].[registered] (
                [age] INT NOT NULL,
                [encrybted_password] VARCHAR (255) NULL,
                [is_online] BIT NOT NULL,
                [system_role] SMALLINT NULL,
                [username] VARCHAR (255) NULL,
                [id] INT NOT NULL,
                PRIMARY KEY CLUSTERED ([id] ASC),
                CHECK ([system_role]>=(0) AND [system_role]<=(1)),
                CONSTRAINT [FK3a0bkifjyvgtpy8pbg9pq8cos] FOREIGN KEY ([id]) REFERENCES [dbo].[guest] ([id])
            );
            GO
            CREATE TABLE [dbo].[shopping_cart] (
                [id] INT IDENTITY (1, 1) NOT NULL,
                [guest_id] INT NULL,
                PRIMARY KEY CLUSTERED ([id] ASC),
                CONSTRAINT [FKd78vt5oxhbavxpx99aktc8f4h] FOREIGN KEY ([guest_id]) REFERENCES [dbo].[guest] ([id])
            );
            GO
            CREATE TABLE [dbo].[cart_item] (
                [id] INT IDENTITY (1, 1) NOT NULL,
                [category] VARCHAR (255) NULL,
                [name] VARCHAR (255) NULL,
                [price] INT NOT NULL,
                [product_id] INT NOT NULL,
                [quantity] INT NOT NULL,
                [store_id] INT NOT NULL,
                [guest_id] INT NULL,
                [shopping_cart_id] INT NULL,
                PRIMARY KEY CLUSTERED ([id] ASC),
                CHECK ([category]='ALCOHOL' OR [category]='Furniture' OR [category]='Grocery' OR [category]='Sports' OR [category]='Beauty' OR [category]='Toys' OR [category]='Books' OR [category]='Clothing' OR [category]='Home' OR [category]='Electronics'),
                CONSTRAINT [FKe89gjdx91fxnmkkssyoim8xfu] FOREIGN KEY ([shopping_cart_id]) REFERENCES [dbo].[shopping_cart] ([id]),
                CONSTRAINT [FKgbca1r4hkl165iluwmkr1grco] FOREIGN KEY ([guest_id]) REFERENCES [dbo].[guest] ([id])
            );
            GO
            CREATE TABLE [dbo].[product] (
                [product_id] INT IDENTITY (1, 1) NOT NULL,
                [category] SMALLINT NULL,
                [description] VARCHAR (255) NULL,
                [name] VARCHAR (255) NULL,
                PRIMARY KEY CLUSTERED ([product_id] ASC),
                CHECK ([category]>=(0) AND [category]<=(9))
            );
            GO
            CREATE TABLE [dbo].[orders] (
                [order_id] INT IDENTITY (1, 1) NOT NULL,
                [date] VARCHAR (255) NULL,
                [final_price] FLOAT (53) NOT NULL,
                [store_name] VARCHAR (255) NULL,
                [user_id] INT NOT NULL,
                PRIMARY KEY CLUSTERED ([order_id] ASC)
            );
            GO
            CREATE TABLE [dbo].[store] (
                [active] BIT NOT NULL,
                [category] VARCHAR (255) NULL,
                [store_name] VARCHAR (255) NULL,
                [store_id] INT IDENTITY (1, 1) NOT NULL,
                PRIMARY KEY CLUSTERED ([store_id] ASC)
            );
            GO
            CREATE TABLE [dbo].[store_stock] (
                [storeid] INT NOT NULL,
                PRIMARY KEY CLUSTERED ([storeid] ASC)
            );
            GO
            CREATE TABLE [dbo].[store_stock_items] (
                [store_stock_storeid] INT NOT NULL,
                [category] SMALLINT NULL,
                [price] INT NULL,
                [product_id] INT NULL,
                [quantity] INT NULL,
                CHECK ([category]>=(0) AND [category]<=(9)),
                CONSTRAINT [FKmj8kn5e19vyjay83d2yu7h3jp] FOREIGN KEY ([store_stock_storeid]) REFERENCES [dbo].[store_stock] ([storeid])
            );
            GO
            CREATE TABLE [dbo].[store_tree_entity] (
                [store_id] INT NOT NULL,
                PRIMARY KEY CLUSTERED ([store_id] ASC)
            );
            GO
            CREATE TABLE [dbo].[item] (
                [id] INT IDENTITY (1, 1) NOT NULL,
                [category] SMALLINT NULL,
                [price] INT NOT NULL,
                [product_id] INT NOT NULL,
                [quantity] VARBINARY (255) NULL,
                [store_id] INT NULL,
                PRIMARY KEY CLUSTERED ([id] ASC),
                CHECK ([category]>=(0) AND [category]<=(9)),
                CONSTRAINT [FK8kikcujfukd6fxj8ekvwl5b1u] FOREIGN KEY ([store_id]) REFERENCES [dbo].[store_stock] ([storeid])
            );
            GO
            CREATE TABLE [dbo].[offer] (
                [receiver_id] INT NOT NULL,
                [sender_id] INT NOT NULL,
                [store_id] INT NOT NULL,
                [approve] BIT NOT NULL,
                [message] VARCHAR (255) NULL,
                [to_be_owner] BIT NOT NULL,
                PRIMARY KEY CLUSTERED ([receiver_id] ASC, [sender_id] ASC, [store_id] ASC)
            );
            GO
            CREATE TABLE [dbo].[offer_permissions] (
                [offer_receiver_id] INT NOT NULL,
                [offer_sender_id] INT NOT NULL,
                [offer_store_id] INT NOT NULL,
                [permissions] VARCHAR (255) NULL,
                CHECK ([permissions]='MANAGE_PURCHASE_POLICY' OR [permissions]='MANAGE_STORE_POLICY' OR [permissions]='SpecialType' OR [permissions]='UpdatePrice' OR [permissions]='UpdateQuantity' OR [permissions]='DeleteFromStock' OR [permissions]='AddToStock' OR [permissions]='ViewAllProducts'),
                CONSTRAINT [FKiydnmk93xfjnkgh53dbschk8p] FOREIGN KEY ([offer_receiver_id], [offer_sender_id], [offer_store_id]) REFERENCES [dbo].[offer] ([receiver_id], [sender_id], [store_id])
            );
            GO
            CREATE TABLE [dbo].[node] (
                [my_id] INT NOT NULL,
                [store_id] INT NOT NULL,
                [is_manager] BIT NOT NULL,
                [my_auth_id] BIGINT NULL,
                [parent_id] INT NULL,
                PRIMARY KEY CLUSTERED ([my_id] ASC, [store_id] ASC),
                CONSTRAINT [FK1w8etobeb3thu97wlphhv7a1w] FOREIGN KEY ([parent_id], [store_id]) REFERENCES [dbo].[node] ([my_id], [store_id]),
                CONSTRAINT [FKlc6liynvcxjs4xcx4dserryyx] FOREIGN KEY ([store_id]) REFERENCES [dbo].[store_tree_entity] ([store_id]),
                CONSTRAINT [FKs54r3b7g7dy2x0y2c0u8s0bm9] FOREIGN KEY ([my_auth_id]) REFERENCES [dbo].[authorization] ([id])
            );
            GO
            CREATE TABLE [dbo].[user_special_item_cart] (
                [special_cart_id] INT IDENTITY (1, 1) NOT NULL,
                [bid_id] INT NOT NULL,
                [special_id] INT NOT NULL,
                [store_id] INT NOT NULL,
                [type] VARCHAR (255) NULL,
                [registered_id] INT NULL,
                PRIMARY KEY CLUSTERED ([special_cart_id] ASC),
                CHECK ([type]='Random' OR [type]='Auction' OR [type]='BID'),
                CONSTRAINT [FKqtb4t48yu0niekid1jc9dmmq6] FOREIGN KEY ([registered_id]) REFERENCES [dbo].[registered] ([id])
            );
            GO
            CREATE TABLE [dbo].[user_suspensions] (
                [user_id] INT NOT NULL,
                [last_start_time] DATETIME2 (6) NULL,
                [paused] BIT NOT NULL,
                [remaining_at_pause_minutes] BIGINT NOT NULL,
                [suspension_end_minutes] BIGINT NOT NULL,
                [total_duration_minutes] BIGINT NOT NULL,
                PRIMARY KEY CLUSTERED ([user_id] ASC)
            );
            GO
        """;

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement statement = connection.createStatement()) {

            String[] sqlStatements = fullSchema.split("(?i)\\bGO\\b");

            for (String sql : sqlStatements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    try {
                        statement.execute(sql);
                        System.out.println("✅ Executed:\n" + sql.substring(0, Math.min(120, sql.length())) + "\n");
                    } catch (SQLException e) {
                        System.err.println("❌ Error executing statement:\n" + sql);
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("✅ All schema executed.");
        } catch (SQLException e) {
            System.err.println("❌ Connection or execution failed:");
            e.printStackTrace();
        }
    }
}
