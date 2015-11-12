import java.util.Random;

public class MySQLRunnableFactory implements AbstractRunnableFactory {

	private Random generator;
	private int numberOfTransfers;
	private int isolationLevel;
	private MySQLConnectionFactory connectionFactory;
	private boolean transactional = true;
	
	public MySQLRunnableFactory(MySQLConnectionFactory connectionFactory, Random generator, int numberOfTransfers,
			int isolationLevel, boolean transactional) {
		this.connectionFactory = connectionFactory;
		this.generator = generator;
		this.numberOfTransfers = numberOfTransfers;
		this.isolationLevel = isolationLevel;
		this.transactional = transactional;
	}

	@Override
	public Transfer createTransfer() {
		MySQLTransfer transfer = new MySQLTransfer(connectionFactory, generator, numberOfTransfers, isolationLevel);
		
		if (!transactional)
			transfer.setTransactional(transactional);
		
		return transfer;
	}

	@Override
	public SummationTask createSummationTask() {
		return new MySQLSummationTask(connectionFactory, isolationLevel);
	}
}
