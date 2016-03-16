package abilities;

public class Flight_I extends _FlightAbility
{
	public Flight_I(int p)
	{
		super("Flight I", p);
	}

	public void updateStats()
	{
		costPerSecond = 5 - level;
		flySpeed = 300 * level; // 100 pixels per second
		cooldown = 1;
	}
}
