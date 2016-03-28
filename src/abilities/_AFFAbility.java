package abilities;

import mainClasses.Ability;

/**
 * An Ability that creates an Arc Force Field around a target.
 * 
 * @author Itamar
 *
 */
public class _AFFAbility extends Ability
{

	public double life;
	public double decayRate;
	public int armor;

	public _AFFAbility(String name, int level)
	{
		super(name, level);
		life = 0;
		decayRate = 0;
		armor = 0;
	}
}
