package abilities;

public class Retrace_II extends _LoopAbility
{
	public Retrace_II(int p)
	{
		super("Retrace II", p, "TARGETED");

		cost = 6 - 0.5 * level;
		costType = "Mana";
		cooldown = 2;
		range = 500;
		rangeType = "Circle area";
		instant = false;

		amount = level;
		position = true;
		state = false;
	}
}
