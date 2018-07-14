import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DatabaseConnection {

    public Connection getConnection() {
        final String DRIVER = "com.mysql.jdbc.Driver";
        final String NAME = "sakila";
        final String URL = "jdbc:mysql://localhost/" + NAME;
        final String USER = "root";
        final String PASS = "";
        Connection connection;
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USER, PASS);
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}