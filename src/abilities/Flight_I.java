package abilities;

public class Flight_I extends _FlightAbility
{
	public Flight_I(int p)
	{
		super("Flight I", p);
	}

	public void updateStats()
	{
		costPerSecond = 5 - LEVEL;
		flySpeed = 300 * LEVEL; // 100 pixels per second
		cooldown = 1;
	}
}
