package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Ghost_Mode_I extends Ability
{

	public Ghost_Mode_I(int p)
	{
		super("Ghost Mode I", p);
		cost = 2 * level;
		costType = CostType.MANA;
		costPerSecond = 0.3;
		cooldown = 5;
		range = -1;
		rangeType = RangeType.NONE;
		instant = true;
	}

	public void use(Environment env, Person user, Point target)
	{
		/*
		 * On/Off: while On, be in Ghost Mode.
		 */
		if (!on) // entering
		{
			if (cooldownLeft > 0 || cost > user.mana)
				return;
			on = true;
			// TODO some kind of visual effect maybe?
			user.ghostMode = true;
			user.mana -= cost;
			timeLeft = level;
			cooldownLeft = 0.5;
		}
		else if (cooldownLeft == 0)
		{
			if (!user.insideWall)
			{
				on = false;
				user.ghostMode = false;
				cooldownLeft = cooldown;
				timeLeft = 0;
			}
			else
				timeLeft = 0;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (timeLeft <= 0)
		{
			if (!user.insideWall)
			{
				user.panic = false;
				use(env, user, target);
			}
			else
			{
				user.mana -= 1.5 * deltaTime; // punish
				env.hitPerson(user, 15, 0, 0, 9, deltaTime); // punish
				user.stamina -= 1.5 * deltaTime; // punish
				timeLeft = 0;
				user.panic = true;
			}

		}
		else
			timeLeft -= deltaTime;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
		{
			on = false;
			user.ghostMode = false;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
		player.target = new Point(-1, -1);
	}
}
