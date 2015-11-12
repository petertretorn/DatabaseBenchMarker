import java.util.Random;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;

public class MongoTransfer extends Transfer {

	private DBCollection collection;
	private BasicDBObject updateFrom;
	private BasicDBObject fromObject;
	private BasicDBObject toObject;
	private BasicDBObject updateTo;
	private int numberOfTransfers;

	
	public MongoTransfer(DBCollection coll, Random generator, int numberOfTransfers) {
		super(generator);
		collection = coll;
		this.numberOfTransfers = numberOfTransfers;
	}

	@Override
	public void run() {
		for (int i = 0; i < numberOfTransfers; i++) {
			setUpTransfer();
			executeTransfer();
		}
	}

	private void executeTransfer() {
		collection.update(fromObject, updateFrom, false, false,
				WriteConcern.ACKNOWLEDGED);

		collection.update(toObject, updateTo, false, false,
				WriteConcern.ACKNOWLEDGED);	
	}

	
	private void setUpTransfer() {
		
		generateRandomNumbers();

		fromObject = new BasicDBObject();
		fromObject.put("_id", fromAccountID);

		updateFrom = new BasicDBObject();
		updateFrom.append("$inc",
				new BasicDBObject().append("balance", -amount));

		toObject = new BasicDBObject();
		toObject.put("_id", toAccountID);

		updateTo = new BasicDBObject();
		updateTo.append("$inc", new BasicDBObject().append("balance", amount));
	}
}
