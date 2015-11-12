package sqlAccess;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class BankAccessSQL {

	private Connection connection = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	private Random generator = new Random();

	private int numberOfTransfers;
	
	private PreparedStatement preparedIncrement;
	private PreparedStatement preparedDecrement;

	static final String INCREMENT_UPDATE = "UPDATE accounts SET balance = balance + ? WHERE ID = ?";
	static final String DECREMENT_UPDATE = "UPDATE accounts SET balance = balance - ? WHERE ID = ?";

	
	public void connect() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost/bank_myisam?"
				+ "user=root&password=flamenco");
	}

	public void readAccounts() throws SQLException {
		statement = connection.createStatement();
		// Result set get the result of the SQL query
		resultSet = statement.executeQuery("select * from accounts");
		writeResultSet(resultSet);
	}

	private void writeResultSet(ResultSet resultSet) throws SQLException {
		int id;
		int balance;

		while (resultSet.next()) {
			id = resultSet.getInt("id");
			balance = resultSet.getInt("balance");
			System.out.println("id: " + id + ", balance: " + balance);
		}
	}

	public double insertAccounts(int numberOfAccounts) throws SQLException {

		preparedStatement = connection
				.prepareStatement("insert into accounts values (?, ?)");
		int balance;

		connection.setAutoCommit(false);
		
		
		Timer t = new Timer();
		t.start();

		for (int i = 1; i <= numberOfAccounts; i++) {
			balance = generator.nextInt(1000) + 1;
			preparedStatement.setInt(1, i);
			preparedStatement.setInt(2, balance);
			preparedStatement.executeUpdate();
		}

		connection.commit();
		t.stop();
		return t.elapsed();
	}

	public void sumAccounts() throws SQLException {
		
		statement = connection.createStatement();
		resultSet = statement.executeQuery("select sum(balance) from accounts");
		if (resultSet.next()) {
			long sum = resultSet.getInt(1);
			System.out.println("Sum of accounts: " + sum);
		}
	}
	
	public void run() {

		int fromAccountID;
		int toAccountID;
		int amount;

		try {
			statement = connection.createStatement();

			preparedIncrement = connection.prepareStatement(INCREMENT_UPDATE);
			preparedDecrement = connection.prepareStatement(DECREMENT_UPDATE);

			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			
			for (int i = 0; i < 20; i++) {

				fromAccountID = generator
						.nextInt(20) + 1;
				toAccountID = generator
						.nextInt(20) + 1;
				amount = generator.nextInt(1000) + 1;

				preparedDecrement.setInt(1, amount);
				preparedDecrement.setInt(2, fromAccountID);

				preparedIncrement.setInt(1, amount);
				preparedIncrement.setInt(2, toAccountID);

				preparedDecrement.executeUpdate();
				preparedIncrement.executeUpdate();

				connection.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) throws Exception {
		System.out.println("starting...");
		BankAccessSQL ba = new BankAccessSQL();
		ba.connect();
		ba.run();
		//System.out.println(ba.insertAccounts(20));
		
		ba.readAccounts();
		ba.sumAccounts();
	}
}
