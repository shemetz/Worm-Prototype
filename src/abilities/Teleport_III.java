package abilities;

public class Teleport_III extends _TeleportAbility
{

	public Teleport_III(int p)
	{
		super("Teleport III", p);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		type = Type.TARGET_POINT;
	}

	public void updateStats()
	{
		cooldown = 2;
		range = LEVEL * 5000;
		cost = 0.5;
	}

	double getCost(double distance)
	{
		return cost;
	}
}
