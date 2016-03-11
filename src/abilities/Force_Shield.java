package abilities;

public class Force_Shield extends ForceFieldAbility
{

	public Force_Shield(int p)
	{
		super("Force Shield", p);
		cost = 3;
		cooldown = 1;
		range = 68;
		rangeType = RangeType.EXACT_RANGE;

		length = 100 + 100 * level / 3;
		width = 10 + level * 1;
		life = 20 * level;
		armor = 0;
	}
}
