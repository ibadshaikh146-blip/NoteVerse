import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDBConnect {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: TestDBConnect <jdbc-url> <user> <password>");
            System.exit(1);
        }
        String url = args[0];
        String user = args[1];
        String pass = args[2];

        System.out.println("Trying: user='" + user + "' password='" + (pass.isEmpty() ? "(empty)" : "(hidden)") + "' url='" + url + "'");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found: " + e.getMessage());
            System.exit(2);
        }

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            System.out.println("SUCCESS: connected as '" + user + "'");
        } catch (SQLException e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}
