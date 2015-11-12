import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BankAccessMySQL extends BankAccess {

	public static final int ISOLATION_LEVEL_DEFAULT = Connection.TRANSACTION_REPEATABLE_READ;

	private Connection connection;
	private PreparedStatement preparedStatement = null;

	private Statement statement;
	private MySQLConnectionFactory connectionFactory;
	
	protected BankAccessMySQL(BankAccess.Builder builder) {
		super(builder);
		
		switch (builder.engine) {
		case MyIsam:
			connectionFactory = new ConnectionFactoryMyIsam(); 
			break;
		default:
			connectionFactory = new ConnectionFactoryInnoDB();
			break;
		}
		runnableFactory = new MySQLRunnableFactory(connectionFactory, generator,
				transfersPerThread, isolationLevel, transactional);
		
		setUpConnection();
	}

	private void setUpConnection() {
		try {
		connection = connectionFactory.createConnection();
		statement = connection.createStatement();
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void initialize() {
		try {		
			ResultSet result = statement
					.executeQuery("SELECT COUNT(*) FROM accounts");
			if (result.next()) {
				numberOfAccounts = result.getInt(1);				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public double removeAccounts() {
		Timer timer = new Timer();
		timer.start();
		
		try {
			
			statement.executeUpdate("DELETE FROM accounts");
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		timer.stop();
		return timer.elapsed();
	}

	@Override
	public double insertAccounts(int numberOfAccounts) {
		try {
			connection = connectionFactory.createConnection();
			statement = connection.createStatement();
			preparedStatement = connection
					.prepareStatement("INSERT INTO accounts VALUES (?, ?)");

			int balance;

			connection.setAutoCommit(false);

			Timer timer = new Timer();
			timer.start();

			for (int i = 1; i <= numberOfAccounts; i++) {
				balance = generator.nextInt(1000) + 1;
				preparedStatement.setInt(1, i);
				preparedStatement.setInt(2, balance);
				preparedStatement.executeUpdate();
			}

			connection.commit();
			timer.stop();
			return timer.elapsed();

		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
