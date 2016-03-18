package abilities;

public class Teleport_II extends _TeleportAbility
{

	public Teleport_II(int p)
	{
		super("Teleport II", p);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		type = Type.TARGET_POINT;
	}

	public void updateStats()
	{
		cooldown = 4;
		range = LEVEL * 700;
		cost = 0.99; // * distance / level
	}

	double getCost(double distance)
	{
		return Math.min(cost * distance / LEVEL * LEVEL / 100, 1);
	}
}
