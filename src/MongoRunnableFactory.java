import java.util.Random;

import com.mongodb.DBCollection;


public class MongoRunnableFactory implements AbstractRunnableFactory {

	private DBCollection collection;
	private int numberOfTransfers;
	private Random generator;
	
	public MongoRunnableFactory(DBCollection collection, int numberOfTransfers, Random generator) {
		this.collection = collection;
		this.numberOfTransfers = numberOfTransfers;
		this.generator = generator;
	}
	
	@Override
	public Transfer createTransfer() {		
		return new MongoTransfer(collection, generator, numberOfTransfers);
	}

	@Override
	public SummationTask createSummationTask() {
		
		return new MongoSummationTask(collection);
	}

}
