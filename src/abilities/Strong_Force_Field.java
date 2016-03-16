package abilities;

public class Strong_Force_Field extends _ForceFieldAbility
{

	public Strong_Force_Field(int p)
	{
		super("Strong Force Field", p);
		rangeType = RangeType.EXACT_RANGE;
	}

	public void updateStats()
	{
		cooldown = Math.max(7 - LEVEL, 0.3);
		range = 68;
		cost = 4;

		length = 100 + 50 * LEVEL;
		width = 5 + LEVEL * 7;
		life = 80 * LEVEL;
		armor = LEVEL;
		decayRate = 0.05;
	}
}
