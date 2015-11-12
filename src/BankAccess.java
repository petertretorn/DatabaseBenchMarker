import java.sql.Connection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BankAccess {

	public enum MySql_Engine {
		InnoDB, MyIsam
	};

	static final int CONNECTIONS_DEFAULT = 15;
	static final int THREADS_DEFAULT = 10;
	static final int TRANSFERS_PER_THREAD_DEFAULT = 25;
	static final String DB_TYPE_DEFAULT = "Mongo";
	static final boolean DO_SUMS_DEFAULT = true;
	static final boolean TRANSACTIONAL_DEFAUT = true;
	static final MySql_Engine MYSQL_ENGINE_DEFAULT = MySql_Engine.InnoDB;

	protected int numberOfThreads;
	protected int transfersPerThread;
	protected static int numberOfAccounts;
	protected String dbType;
	protected int isolationLevel;
	protected boolean doSumQueries;
	protected boolean transactional;

	protected AbstractRunnableFactory runnableFactory;

	protected final static Random generator = new Random();

	protected ExecutorService executor;

	protected Transfer[] transfers;
	protected SummationTask sumAccounts;

	private int estimatedThroughPut;
	
	public static class Builder {

		int connections;
		int numberOfThreads;
		int transfersPerThread;
		int isolationLevel;
		String dbType;
		boolean doSumQueries;
		boolean transactional;
		MySql_Engine engine;

		public Builder() {
			this.connections = CONNECTIONS_DEFAULT;
			this.numberOfThreads = THREADS_DEFAULT;
			this.transfersPerThread = TRANSFERS_PER_THREAD_DEFAULT;
			this.dbType = DB_TYPE_DEFAULT;
			this.doSumQueries = DO_SUMS_DEFAULT;
			this.transactional = TRANSACTIONAL_DEFAUT;
			this.isolationLevel = BankAccessMySQL.ISOLATION_LEVEL_DEFAULT;
			this.engine = MYSQL_ENGINE_DEFAULT;
		}

		public Builder transfersPerThread(int transfers) {
			this.transfersPerThread = transfers;
			return this;
		}

		public Builder numberOfThreads(int number) {
			this.numberOfThreads = number;
			return this;
		}

		public Builder numberOfConnections(int connections) {
			this.connections = connections;
			return this;
		}

		public Builder dbType(String type) {
			this.dbType = type;
			return this;
		}

		public Builder isolationLevel(int level) {
			isolationLevel = level;
			return this;
		}

		public Builder doSumQueries(boolean doSums) {
			doSumQueries = doSums;
			return this;
		}
		
		public Builder transactional(boolean trans) {
			transactional = trans;
			return this;
		}

		public Builder engine(MySql_Engine engine) {
			this.engine = engine;
			return this;
		}

		public BankAccess build() {
			switch (dbType.toLowerCase()) {
			case "mongo":
				return new BankAccessMongo(this);
			case "mysql":
				return new BankAccessMySQL(this);
			default:
				return new BankAccessMongo(this);
			}
		}
	}

	protected BankAccess(Builder builder) {
		numberOfThreads = builder.numberOfThreads;
		transfers = new Transfer[numberOfThreads];
		transfersPerThread = builder.transfersPerThread;
		isolationLevel = builder.isolationLevel;
		doSumQueries = builder.doSumQueries;
		transactional = builder.transactional;
		dbType = builder.dbType;
		executor = Executors.newFixedThreadPool(numberOfThreads);
	}

	public void cleanAndPopulateDB(int numberOfRows) {
		removeAccounts();
		double elapsed = insertAccounts(numberOfRows);
		System.out.println("Insertion time: " + elapsed);
	}

	protected abstract double removeAccounts();

	protected abstract double insertAccounts(int numberOfAccounts);

	public void doExperiment() {
		reportSpecificationOfExperiment();
		initialize();
		setUpExperiment();
		executeTransfers();

		if (doSumQueries) {
			doSumQueries();
		}
		finishUp();
	}

	private void reportSpecificationOfExperiment() {
		System.out.println("\nDatabase type: " + dbType
				+ "\nNumber of transfer threads: " + numberOfThreads
				+ "\nTransfers per thread: " + transfersPerThread);

		if (dbType.equalsIgnoreCase("mysql")) {
			reportMySQLSpecifics();
		}
		System.out.println("\nRunning experiment...\n");

	}

	private void reportMySQLSpecifics() {
		String isolationAsString;
		
		switch (isolationLevel) {
		case Connection.TRANSACTION_READ_UNCOMMITTED:
			isolationAsString = "read uncommited";
			break;
		case Connection.TRANSACTION_READ_COMMITTED:
			isolationAsString = "read commited";
			break;
		case Connection.TRANSACTION_REPEATABLE_READ:
			isolationAsString = "repeatable read";
			break;
		case Connection.TRANSACTION_SERIALIZABLE:
			isolationAsString = "serialiable";
			break;
		default:
			isolationAsString = "unmatched isolation level";
			break;
		}
		System.out.println("Isolationlevel: " + isolationAsString);
	}

	protected abstract void initialize();

	private void setUpExperiment() {
		Transfer transfer;
		for (int i = 0; i < numberOfThreads; i++) {
			transfer = runnableFactory.createTransfer();
			transfers[i] = transfer;
		}
		
		if (doSumQueries) {
			sumAccounts = runnableFactory.createSummationTask();
		}
	}

	private void executeTransfers() {
		for (Transfer transfer : transfers) {
			executor.execute(transfer);
		}
	}

	private void doSumQueries() {
		new Thread(sumAccounts).start();
	}

	private void finishUp() {
		executor.shutdown();

		Timer timer = new Timer();
		timer.start();

		while (!executor.isTerminated()) {
		}
		timer.stop();

		if (doSumQueries) {
			finishUpSumQueries();
		}

		long elapsed = timer.elapsed();
		
		estimatedThroughPut = (int)( (numberOfThreads * transfersPerThread) / ((double)elapsed / 1000) ); 
		
		System.out.println("\nTotal elapsed time for experiment: " + elapsed );
		System.out.println("Estimated throughput (transactions per second): " + estimatedThroughPut);
		System.out.println("\nAll times reported are in milliseconds");
	}

	private void finishUpSumQueries() {
		sumAccounts.setRunning(false);
		while (sumAccounts.isRunning()) {
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(sumAccounts.prepareAndReportStatsOfSumQueries());
	}
}
