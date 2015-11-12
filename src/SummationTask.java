import java.text.DecimalFormat;
import java.util.ArrayList;

public abstract class SummationTask implements Runnable {

	protected boolean running = true;
	protected long initialSum;

	private int count;
	private ArrayList<Long> deviations = new ArrayList<>();
	private ArrayList<Long> elapsedTimes = new ArrayList<>();
	private long deviation;
	private int correctResults;
	private long averageDeviation;
	private long averageElapsed;
	private double averageDeviationPercent;
	private double correctnessInPercent;

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	protected void collectStats(long sum, long elapsed) {
		count++;
		deviation = initialSum - sum;
		deviations.add(deviation);
		elapsedTimes.add(elapsed);
	}

	public String prepareAndReportStatsOfSumQueries() {

		if (count > 0) {
			calculateAverageDeviationAndCorrectResultsStats();
			calculateAverageElapsedTime();
		}

		DecimalFormat df = new DecimalFormat("#.####");

		return "Stats for sumqueries, discarding last run:\n\n"
				+ "Number of queries: " + (count - 1) + "\nOf which correct: "
				+ correctResults + "\nLevel of correctness in percent: "
				+ correctnessInPercent +// df.format(correctnessInPercent) +
				"\nAverage elapsed time per query: " + averageElapsed
				+ "\nAverage deviation from initial sum: " + averageDeviation
				+ "\nAverage deviation in percent of initial sum: "
				+ df.format(averageDeviationPercent) + "\nInitial sum: "
				+ initialSum;
	}

	private void calculateAverageDeviationAndCorrectResultsStats() {
		long totalSum = 0;

		// discard last run
		deviations.remove(count - 1);

		for (long deviation : deviations) {
			totalSum += deviation;
			if (deviation == 0) {
				correctResults++;
			}
		}
		correctnessInPercent = ((double)correctResults / (double)(count - 1)) * 100;
		averageDeviation = totalSum / (count - 1);
		averageDeviationPercent = ((double) averageDeviation / (double) initialSum) * 100;
	}

	private void calculateAverageElapsedTime() {
		long totalElapsed = 0;

		// discard last run
		elapsedTimes.remove(count - 1);

		for (long elapsed : elapsedTimes) {
			totalElapsed += elapsed;
		}
		averageElapsed = totalElapsed / (count - 1);
	}
}
