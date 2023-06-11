import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:8889/JavaPro";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "updatedata";

    private Connection connection;

    public DatabaseManager() {
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver class not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertUser(String username, String password, String role) throws SQLException {
        try {
            // Create a prepared statement for the INSERT query
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);

            // Execute the INSERT query
            statement.executeUpdate();

            // Commit the changes to the database
            connection.commit();

            // Close the statement
            statement.close();
        } catch (SQLException e) {
            // Rollback the transaction if an error occurs
            connection.rollback();
            throw e;
        } finally {
            // Close the connection
            if (connection != null) {
                connection.close();
            }
        }
    }
}

