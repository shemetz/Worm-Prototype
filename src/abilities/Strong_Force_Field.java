package abilities;

public class Strong_Force_Field extends ForceFieldAbility
{

	public Strong_Force_Field(int p)
	{
		super("Strong Force Field", p);
		cooldown = Math.max(7 - level, 0.3);
		range = 68;
		rangeType = RangeType.EXACT_RANGE;
		cost = 4;

		length = 100 + 50 * (int) level;
		width = 5 + (int) level * 7;
		life = 80 * level;
		armor = level;
	}
}
