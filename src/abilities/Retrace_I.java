package abilities;

public class Retrace_I extends _LoopAbility
{
	public Retrace_I(int p)
	{
		super("Retrace I", p, "SELF");

		cost = 4 - 0.5 * level;
		costType = "Mana";
		cooldown = 2;
		range = 0;
		rangeType = "Self";
		instant = false;

		amount = level;
		position = true;
		state = false;
	}
}
