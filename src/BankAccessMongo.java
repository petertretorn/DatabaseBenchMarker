import java.net.UnknownHostException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

public class BankAccessMongo extends BankAccess {

	private MongoClient mongo = null;
	private DB db;
	
	private DBCollection collection;
	
	private final String DB_NAME = "bank";
	private final String COLLECTION_NAME = "accounts";

	protected BankAccessMongo(BankAccess.Builder builder) {
		super(builder);
		connect();		
		runnableFactory = new MongoRunnableFactory(collection, transfersPerThread, generator);
	}

	private void connect() {
		try {
			MongoClientOptions options = new MongoClientOptions.Builder()
					.connectionsPerHost(numberOfThreads + 1).build();

			mongo = new MongoClient(new ServerAddress("localhost"), options);

			db = mongo.getDB(DB_NAME);
			collection = db.getCollection(COLLECTION_NAME);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initialize() {
		numberOfAccounts = (int)collection.getCount();
	}

	
	@Override
	public double removeAccounts() {
		Timer timer = new Timer();
		timer.start();
		
		// passing an empty BasicDBObject, all documents will be deleted
		collection.remove(new BasicDBObject(), WriteConcern.FSYNC_SAFE);

		timer.stop();
		return timer.elapsed();
	}

	@Override
	public double insertAccounts(int numberOfAccounts) {
		Timer timer = new Timer();
		timer.start();

		DBObject account;
		int amount;

		for (int i = 1; i <= numberOfAccounts; i++) {
			amount = generator.nextInt(10000) + 1;
			account = new BasicDBObject("_id", i).append("balance", amount);
			collection.insert(account, WriteConcern.ACKNOWLEDGED);
		}
		timer.stop();
		return timer.elapsed();
	}
}
