import java.util.Random;

abstract class Transfer implements Runnable {

	protected int fromAccountID;
	protected int toAccountID;
	protected int amount;

	private Random generator;
	
	static final int NUMBER_OF_ACCOUNTS = BankAccess.numberOfAccounts;
	
	public Transfer(Random generator) {
		this.generator = generator;
	}
	
	protected void generateRandomNumbers() {
		fromAccountID = getRandomNumber( NUMBER_OF_ACCOUNTS );
		toAccountID = getRandomNumber( NUMBER_OF_ACCOUNTS );
		
		while (fromAccountID == toAccountID) {
			toAccountID = getRandomNumber( NUMBER_OF_ACCOUNTS );
		}
		amount = getRandomNumber(1000);
	}
	
	protected int getRandomNumber(int range) {
		return generator.nextInt(range) + 1;
	}
}
