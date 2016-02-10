package abilities;

public class Retrace_III extends _LoopAbility
{
	public Retrace_III(int p)
	{
		super("Retrace III", p, "AREA");

		cost = 8 - 0.5 * level;
		costType = "Mana";
		cooldown = 2;
		range = 1500;
		rangeType = "Circle area";
		instant = false;

		amount = level;
		position = true;
		state = false;
	}
}
