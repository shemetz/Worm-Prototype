package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Clone;
import mainClasses.Environment;
import mainClasses.NPC;
import mainClasses.Person;
import mainClasses.Player;

public class Clone_I extends Ability
{
	public Clone_I(int p)
	{
		super("Clone I", p);
		cost = 5;
		costType = CostType.MANA;
		cooldown = 10 - level; // after last clone died
		range = 300;
		rangeType = RangeType.NONE;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (!on && user.mana >= cost && cooldownLeft == 0)
		{
			user.mana -= cost;
			double angle = Math.atan2(target.y - user.y, target.x - user.x);
			Clone clone = new Clone(user.x + range * Math.cos(angle), user.y + range * Math.sin(angle), user, this);
			clone.setStats(2, 2, 2, 2, 2, 2);
			clone.updateSubStats();
			clone.maxLife = 15 * level;
			clone.life = clone.maxLife;
			clone.lifeRegen = 0;
			clone.strategy = NPC.Strategy.CLONE_I;
			clone.legs = 0;
			clone.chest = 0;
			clone.initAnimation();
			env.people.add(clone);
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
