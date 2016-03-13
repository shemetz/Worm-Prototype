package abilities;

import mainResourcesPackage.SoundEffect;

public class Sapping_Fists extends _PunchAbility
{

	public Sapping_Fists(int p)
	{
		super("Sapping Fists", p);

		sounds.add(new SoundEffect("Sapping Fists.wav"));
	}

	public void updateStats()
	{
		
	}
}
