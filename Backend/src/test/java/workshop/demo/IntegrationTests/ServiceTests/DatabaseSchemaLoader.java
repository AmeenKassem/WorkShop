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
            CREATE TABLE [dbo].[user_suspensions] (
                [user_id]                     INT    NOT NULL,
                [paused]                      BIT    NOT NULL,
                [total_duration_minutes ]     BIGINT NOT NULL,
                [suspension_end_minutes ]     BIGINT NOT NULL,
                [remaining_at_pause_minutes ] BIGINT NULL,
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

            System.out.println("✅ All schema executed successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Connection or execution failed:");
            e.printStackTrace();
        }
    }
}
