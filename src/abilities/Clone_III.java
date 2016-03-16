package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Clone;
import mainClasses.Environment;
import mainClasses.NPC;
import mainClasses.Person;
import mainClasses.Player;

public class Clone_III extends _SummoningAbility
{

	public Clone_III(int p)
	{
		super("Clone III", p);
		costType = CostType.MANA;
		rangeType = RangeType.NONE;
	}

	public void updateStats()
	{
		range = 300;
		cooldown = 2; // after last clone died
		cost = 5;
		life = 30 * level;
		maxNumOfClones = 3;
		statMultiplier = 1.1;
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
			clone.strategy = NPC.Strategy.CLONE;
			clone.initAnimation();
			for (Ability a : user.abilities)
				if (!a.equals(this))
					clone.abilities.add(a.clone());
			env.people.add(clone);
			clonesMade++;
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
