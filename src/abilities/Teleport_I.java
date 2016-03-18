package abilities;

public class Teleport_I extends _TeleportAbility
{

	public Teleport_I(int p)
	{
		super("Teleport I", p);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA; 
		type = Type.TARGET_POINT;
	}

	public void updateStats()
	{
		cooldown = 8;
		range = LEVEL * 200;
		cost = 0.99; // * distance / level
	}

	double getCost(double distance)
	{
		return Math.min(cost * distance / LEVEL / 100, 1);
	}
}
