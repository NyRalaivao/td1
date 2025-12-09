import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Variables en clair comme demandé
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/product_management_db";
    private static final String DB_USER = "product_manager_user";
    private static final String DB_PASSWORD = "123456";

    static {
        try {
            // Charger le driver (optionnel depuis Java 6+ si driver présent)
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // Si driver non trouvé, afficher erreur
            System.err.println("Postgres JDBC driver non trouvé. Assurez-vous d'avoir le driver dans le classpath.");
            e.printStackTrace();
        }
    }

    public Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }
}
