import java.sql.Connection;

public class ExperimentRunner {

	static int connections = BankAccess.CONNECTIONS_DEFAULT;
	static int numberOfThreads = BankAccess.THREADS_DEFAULT;
	static int transfersPerThread = BankAccess.TRANSFERS_PER_THREAD_DEFAULT;
	static int isolationLevel = BankAccessMySQL.ISOLATION_LEVEL_DEFAULT;
	static BankAccess.MySql_Engine engine = BankAccess.MYSQL_ENGINE_DEFAULT;
	
	static boolean cleanAndPopulateDB = false;
	static boolean doSumQueries = true;
	static boolean transactional = true;
	static int rowsToInsert;

	static String dbType = BankAccess.DB_TYPE_DEFAULT;

	private static BankAccess accesser;

	public static void main(String[] args) {

		if (args.length > 0 && args[0].equalsIgnoreCase("-help")) {
			displayUsageAndExit();
		}

		parseArguments(args);
		checkOptions();

		accesser = new BankAccess.Builder().
				numberOfThreads(numberOfThreads).
				transfersPerThread(transfersPerThread).
				dbType(dbType).
				isolationLevel(isolationLevel).
				engine(engine).
				doSumQueries(doSumQueries).
				transactional(transactional).
				build();

		if (cleanAndPopulateDB) {
			System.out.println("Cleaning up table and inserting "
					+ rowsToInsert + " new accounts...");
			accesser.cleanAndPopulateDB(rowsToInsert);
		}

		accesser.doExperiment();
	}

	private static void parseArguments(String[] args) {

		for (String arg : args) {
			switch (arg.substring(0, 2)) {

			case "-t":
				numberOfThreads = getInt(arg);
				break;
			case "-m":
				transfersPerThread = getInt(arg);
				break;
			case "-d":
				dbType = getString(arg);
				break;
			case "-i":
				isolationLevel = parseIsolationLevel(arg);
				break;
			case "-e":
				engine = parseEngineType(arg);
				break;
			case "-c":
				cleanAndPopulateDB = true;
				rowsToInsert = getInt(arg);
			case "-q":
				doSumQueries = parseBoolean(arg);
				break;
			case "-x":
				transactional = parseBoolean(arg);
				break;
			default:
				displayUsageAndExit();
			}
		}
	}

	private static BankAccess.MySql_Engine parseEngineType(String arg) {
		BankAccess.MySql_Engine engine;
		
		switch (arg.toLowerCase().substring(2)) {
		case "myisam":
			engine = BankAccess.MySql_Engine.MyIsam;
			break;
		default:
			engine = BankAccess.MySql_Engine.InnoDB;
			break;
		}
		return engine;
	}

	private static boolean parseBoolean(String arg) {	
		if ( arg.substring(2).equalsIgnoreCase("no") ) {
			return false;
		}
		return true;
	}

	private static int parseIsolationLevel(String arg) {
		switch (arg.substring(2)) {
		case "RU":
			return Connection.TRANSACTION_READ_UNCOMMITTED;
		case "RC":
			return Connection.TRANSACTION_READ_COMMITTED;
		case "RR":
			return Connection.TRANSACTION_REPEATABLE_READ;
		default:
			return Connection.TRANSACTION_SERIALIZABLE;
		}
	}

	private static String getString(String arg) {
		return arg.substring(2);
	}

	private static int getInt(String arg) {
		try {
			return Integer.parseInt(arg.substring(2));
		} catch (NumberFormatException e) {
			System.out.println("Invalid option\n");
			displayUsageAndExit();
			return -1;
		}
	}

	private static void displayUsageAndExit() {

		System.out
				.println("\nUsage: BankRunner -[OPTION][VALUE]\n\n"
						+ "Options: \n\n"
						+ "-t number of threads, default is "
						+ BankAccess.THREADS_DEFAULT
						+ "\n"
						+ "-m number of transfers (moves) per thread, default is "
						+ BankAccess.TRANSFERS_PER_THREAD_DEFAULT
						+ "\n"
						+ "-d database type, Mongo or MySQL\n"
						+ "-c clean/truncate table and insert rows\n"
						+ "-i isolationlevel, only relevant for MySQL\n\n"
						+ "Example: BankRunner -dMysql -iRR -c10000 -t25 -m50\n\n"
						+ "Runs experiments using MySQL database with isolation level reapeatable read (RR),\n"
						+ "truncate the table and inserts 10000 new rows, executes 25 threads each performing\n"
						+ "50 transfers between accounts.\n\n");

		System.exit(0);
	}

	private static void checkOptions() {

	}
}
