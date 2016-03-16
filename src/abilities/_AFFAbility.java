package abilities;

import mainClasses.Ability;

public class _AFFAbility extends Ability
{

	public double life;
	public double decayRate;
	public int armor;

	public _AFFAbility(String name, int p)
	{
		super(name, p);
		life = 0;
		decayRate = 0;
		armor = 0;
	}
}
