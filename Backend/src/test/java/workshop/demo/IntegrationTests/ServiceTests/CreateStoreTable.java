package workshop.demo.IntegrationTests.ServiceTests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateStoreTable {

    private static final String JDBC_URL = "jdbc:sqlserver://ws-server.database.windows.net:1433;" +
            "database=test_assi_bshar;encrypt=true;trustServerCertificate=false;" +
            "hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    private static final String JDBC_USER = "rahaf";
    private static final String JDBC_PASSWORD = "RaPass2025";

    public static void main(String[] args) {
        String sql = """
            CREATE TABLE [dbo].[store] (
                [store_id]     INT           IDENTITY (1, 1) NOT NULL,
                [active]       BIT           NOT NULL,
                [category]     VARCHAR (255) NULL,
                [store_name]   VARCHAR (255) NULL,
                [rank_1_count] INT           NULL,
                [rank_2_count] INT           NULL,
                [rank_3_count] INT           NULL,
                [rank_4_count] INT           NULL,
                [rank_5_count] INT           NULL,
                PRIMARY KEY CLUSTERED ([store_id] ASC)
            );

            CREATE UNIQUE NONCLUSTERED INDEX [UKs1wrksusdh1r9nhoau7qxggjn]
                ON [dbo].[store]([store_name] ASC) WHERE ([store_name] IS NOT NULL);
        """;

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Split by semicolon to execute each statement separately
            for (String s : sql.split(";")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                    System.out.println("✅ Executed:\n" + (trimmed.length() > 80 ? trimmed.substring(0, 80) + "..." : trimmed));
                }
            }

            System.out.println("✅ Store table created successfully.");

        } catch (SQLException e) {
            System.err.println("❌ Error creating store table:");
            e.printStackTrace();
        }
    }
}
