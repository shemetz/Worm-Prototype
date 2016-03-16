package abilities;

public class Wide_Force_Field extends _ForceFieldAbility
{

	public Wide_Force_Field(int p)
	{
		super("Wide Force Field", p);
		rangeType = RangeType.EXACT_RANGE;
	}

	public void updateStats()
	{
		cooldown = Math.max(7 - LEVEL, 0.3);
		range = 68;
		cost = 4;

		length = 300 + 100 * LEVEL;
		width = 5 + LEVEL * 2;
		life = 50 * LEVEL;
		armor = (int) (0.5 * LEVEL);
		decayRate = 0.05;
	}
}
