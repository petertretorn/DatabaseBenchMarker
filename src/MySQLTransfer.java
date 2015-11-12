import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class MySQLTransfer extends Transfer {

	private Connection connection;
	private int numberOfTransfers;
	private PreparedStatement preparedIncrement;
	private PreparedStatement preparedDecrement;

	private int isolationLevel;

	static final String INCREMENT_UPDATE = "UPDATE accounts SET balance = balance + ? WHERE ID = ?";
	static final String DECREMENT_UPDATE = "UPDATE accounts SET balance = balance - ? WHERE ID = ?";

	static final int NUMBER_OF_ACCOUNTS = BankAccess.numberOfAccounts;

	private boolean transactional = true;

	public MySQLTransfer(MySQLConnectionFactory connectionFactory,
			Random generator, int number, int isolationLevel) {
		super(generator);
		this.isolationLevel = isolationLevel;
		numberOfTransfers = number;
		connection = connectionFactory.createConnection();
	}

	@Override
	public void run() {
		boolean commited;

		setUpStatements();

		for (int i = 0; i < numberOfTransfers; i++) {
			commited = false;
			generateRandomNumbers();
			prepareStatements();
			while (!commited) {
				try {
					preparedDecrement.executeUpdate();
					preparedIncrement.executeUpdate();

					if (isTransactional()) {
						connection.commit();
					}

					commited = true;
				} catch (SQLException e) {
					System.out
							.println("Rolling back transaction, trying again...");
					try {
						connection.rollback();
						Thread.sleep(getRandomNumber(100));
					} catch (Exception sq) {
						sq.printStackTrace();
					}
				}
			}
		}
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void prepareStatements() {
		try {
			preparedDecrement.setInt(1, amount);
			preparedDecrement.setInt(2, fromAccountID);
			preparedIncrement.setInt(1, amount);
			preparedIncrement.setInt(2, toAccountID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void setUpStatements() {
		try {
			preparedIncrement = connection.prepareStatement(INCREMENT_UPDATE);
			preparedDecrement = connection.prepareStatement(DECREMENT_UPDATE);

			//if (isTransactional()) {
				connection.setAutoCommit(false);
			//}

			connection.setTransactionIsolation(isolationLevel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isTransactional() {
		return transactional;
	}

	public void setTransactional(boolean transactional) {
		this.transactional = transactional;
	}
}
