package abilities;

import mainResourcesPackage.SoundEffect;

public class Elemental_Fists_E extends _PunchAbility
{

	public static String[] applicable = new String[]
	{ "Fire", "Lava", "Acid", "Electricity", "Energy", "Wind", "Ice", "Flesh" };

	public Elemental_Fists_E(String e, int p)
	{
		super("Elemental Fists <" + e + ">", p);

		sounds.add(new SoundEffect(e + " Smash.wav"));
	}

	public void updateStats()
	{
		damage = level;
	}
}
