package abilities;

public class Force_Shield extends _ForceFieldAbility
{

	public Force_Shield(int p)
	{
		super("Force Shield", p);
		rangeType = RangeType.EXACT_RANGE;
	}

	public void updateStats()
	{
		cost = 3;
		cooldown = 1;
		range = 68;
		length = 100 + 100 * LEVEL / 3;
		width = 10 + LEVEL * 1;
		life = 20 * LEVEL;
		armor = 0;
		decayRate = 0.5;
	}
}
