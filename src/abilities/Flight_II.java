package abilities;

public class Flight_II extends _FlightAbility
{

	public Flight_II(int p)
	{
		super("Flight II", p);
	}

	public void updateStats()
	{
		flySpeed = 300 * level; // 800 to 1200 pixels per second
		cooldown = 1;
		costPerSecond = 0.4;
	}
}
