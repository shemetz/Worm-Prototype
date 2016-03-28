package abilities;

import mainClasses.Ability;

/**
 * An ability that summons one or multiple clones.
 * 
 * @author Itamar
 *
 */
public class _SummoningAbility extends Ability
{
	public int clonesMade;
	public int maxNumOfClones;
	public int life;
	public double statMultiplier;

	public _SummoningAbility(String n, int p)
	{
		super(n, p);
		clonesMade = 0;
		maxNumOfClones = 1;
		statMultiplier = 1;
		life = 0;
	}
}
