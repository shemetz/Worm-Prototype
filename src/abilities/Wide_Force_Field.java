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
		cooldown = Math.max(7 - level, 0.3);
		range = 68;
		cost = 4;

		length = 300 + 100 * level;
		width = 5 + level * 2;
		life = 50 * level;
		armor = (int) (0.5 * level);
		
	}
}
