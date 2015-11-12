import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoSummationTask extends SummationTask {

	private DBCollection collection;
	private Timer timer;
	
	public MongoSummationTask(DBCollection collection) {
		this.collection = collection;
		timer = new Timer();
		AggregationOutput ao = doAggregationQuery();
		initialSum = extractBalanceFromAggregation(ao);
	}
	
	@Override
	public void run() {
		long elapsed;
		long sum;
		
		while( running ) {
			timer.start();
			AggregationOutput ao = doAggregationQuery();
			timer.stop();
			
			sum = extractBalanceFromAggregation(ao);
			elapsed = timer.elapsed();
			collectStats(sum, elapsed);
		}
	}
	
	private AggregationOutput doAggregationQuery() {
		DBObject groupFields = new BasicDBObject("_id", "");
		groupFields.put("balance", new BasicDBObject("$sum", "$balance"));

		DBObject group = new BasicDBObject();
		group.put("$group", groupFields);

		DBObject project = new BasicDBObject();
		DBObject projectFields = new BasicDBObject();
		
		projectFields.put("_id", 0);
		projectFields.put("balance", "$balance");
		project.put("$project", projectFields);

		AggregationOutput ao = collection.aggregate(project, group);

		return ao;
	}

	private long extractBalanceFromAggregation(AggregationOutput ao) {
		long balance = 0;

		for (DBObject obj : ao.results()) {
			balance = (int) obj.get("balance");
		}	
		return balance;
	}
}