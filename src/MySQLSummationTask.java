import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLSummationTask extends SummationTask {

	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private MySQLConnectionFactory connectionFactory;
	
	private final static String SUM_ACCOUNTS_QUERY = "SELECT SUM(balance) FROM accounts";
	private Timer timer;

	public MySQLSummationTask(MySQLConnectionFactory connectionFactory, int isolationLevel) {
		this.connectionFactory = connectionFactory;
		timer = new Timer();
		initialize(isolationLevel);
	}

	private void initialize(int isolationLevel) {
		try {
			connection = connectionFactory.createConnection();
			connection.setTransactionIsolation( isolationLevel  );
			statement = connection.createStatement();
			initialSum = doSumQuery();
		} catch (SQLException e) { 
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		long sum;
		
		try {
			while (running) {
				sum = doSumQuery();
				collectStats(sum, timer.elapsed());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private long doSumQuery() throws SQLException {
		long sumResult = -1;
		
		timer.start();
		resultSet = statement.executeQuery( SUM_ACCOUNTS_QUERY );
		
		if (resultSet.next()) {
			sumResult = resultSet.getInt(1);
		}
		timer.stop();
		
		return sumResult;
	}
}
