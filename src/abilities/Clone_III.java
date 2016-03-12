package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Clone;
import mainClasses.Environment;
import mainClasses.NPC;
import mainClasses.Person;
import mainClasses.Player;

public class Clone_III extends Ability
{
	public int clonesMade;

	public Clone_III(int p)
	{
		super("Clone III", p);
		cost = 5;
		costType = CostType.MANA;
		cooldown = 2; // after last clone died
		range = 300;
		rangeType = RangeType.NONE;

		clonesMade = 0;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (clonesMade < 3 && user.mana >= cost && cooldownLeft == 0)
		{
			user.mana -= cost;
			double angle = Math.atan2(target.y - user.y, target.x - user.x);
			Clone clone = new Clone(user.x + range * Math.cos(angle), user.y + range * Math.sin(angle), user, this);
			clone.updateSubStats();
			clone.maxLife = 30 * level;
			clone.life = clone.maxLife;
			clone.strategy = NPC.Strategy.CLONE;
			clone.initAnimation();
			for (Ability a : user.abilities)
				if (!a.equals(this))
					clone.abilities.add(a.clone());
			env.people.add(clone);
			clonesMade++;
			if (clonesMade == 3)
				on = true;
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.CLONE;
	}
}
