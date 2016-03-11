package abilities;

public class Wide_Force_Field extends ForceFieldAbility
{

	public Wide_Force_Field(int p)
	{
		super("Wide Force Field", p);
		cooldown = Math.max(7 - level, 0.3);
		range = 68;
		rangeType = RangeType.EXACT_RANGE;
		cost = 4;

		length = 300 + 100 * level;
		width = 5 + level * 2;
		life = 50 * level;
		armor = (int) (0.5*level);
	}
}
