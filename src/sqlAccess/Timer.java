package sqlAccess;

public class Timer {

	private long startTime;
	private long stopTime;
	
	public void start() {
		startTime = System.currentTimeMillis();
	}
	
	public void stop() {
		stopTime = System.currentTimeMillis();
	}
	
	public long elapsed() {
		return (stopTime - startTime) / 1000;
	}
	
}
