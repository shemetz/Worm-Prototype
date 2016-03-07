package abilities;

public class Elemental_Fists_E extends _PunchAbility
{

	public static String[] applicable = new String[]
	{ "Fire", "Lava", "Acid", "Electricity", "Energy", "Wind", "Ice", "Flesh" };

	public Elemental_Fists_E(String e, int p)
	{
		super("Elemental Fists <" + e + ">", p);

		damage = level;
		pushback = 0;
	}
}
