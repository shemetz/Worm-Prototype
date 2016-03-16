package abilities;

import java.awt.Point;

import mainClasses.Clone;
import mainClasses.Environment;
import mainClasses.NPC;
import mainClasses.Person;
import mainClasses.Player;

public class Clone_I extends _SummoningAbility
{

	public Clone_I(int p)
	{
		super("Clone I", p);
		rangeType = RangeType.NONE;
		costType = CostType.MANA;
	}

	public void updateStats()
	{
		cost = 5;
		cooldown = 10 - level; // after last clone died
		range = 300;
		life = 15 * level;
		maxNumOfClones = 1;
		statMultiplier = 2/3;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (clonesMade < maxNumOfClones && user.mana >= cost && cooldownLeft == 0)
		{
			user.mana -= cost;
			double angle = Math.atan2(target.y - user.y, target.x - user.x);
			Clone clone = new Clone(user.x + range * Math.cos(angle), user.y + range * Math.sin(angle), user, this);
			clone.multiplyStats(statMultiplier);
			clone.updateSubStats();
			clone.maxLife = life;
			clone.life = clone.maxLife;
			clone.lifeRegen = 0;
			clone.strategy = NPC.Strategy.CLONE;
			clone.legs = 0;
			clone.chest = 0;
			clone.initAnimation();
			env.people.add(clone);
			if (clonesMade == maxNumOfClones)
				on = true;
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (clonesMade < maxNumOfClones)
			on = false;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.CLONE;
	}
}
