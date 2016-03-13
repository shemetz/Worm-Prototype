package abilities;

import mainResourcesPackage.SoundEffect;

public class Shattering_Fists extends _PunchAbility
{

	public Shattering_Fists(int p)
	{
		super("Shattering Fists", p);
		sounds.add(new SoundEffect("Shattering Fists.wav"));
	}

	public void updateStats()
	{
		chance = 0.2 * level;
		
	}
}
