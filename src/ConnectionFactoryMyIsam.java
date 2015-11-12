import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnectionFactoryMyIsam implements MySQLConnectionFactory {

		private static Connection connection = null;

		public synchronized Connection createConnection() {

			try {
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager
						.getConnection("jdbc:mysql://localhost/bank_myisam?"
								+ "user=root&password=flamenco");
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return connection;
		}
}
