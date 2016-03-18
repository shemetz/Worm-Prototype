package abilities;

public class Blink extends _TeleportAbility
{

	public Blink(int p)
	{
		super("Blink", p);
		costType = CostType.MANA;
		rangeType = RangeType.EXACT_RANGE; 
		type = Type.FIXED_DISTANCE;
	}

	public void updateStats()
	{
		cooldown = 0.1 + (double) (LEVEL) / 4;
		range = LEVEL * 100;
		cost = 1 + (double) (LEVEL) / 3;
	}
	
	double getCost(double distance)
	{
		return cost;
	}
}
