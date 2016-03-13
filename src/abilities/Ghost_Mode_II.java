package abilities;

import java.awt.Point;

import effects.Ethereal;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Ghost_Mode_II extends Ability
{
	Ethereal effect;

	public Ghost_Mode_II(int p)
	{
		super("Ghost Mode II", p);
		costType = CostType.MANA;
		rangeType = RangeType.NONE;
		instant = true;
	}

	public void updateStats()
	{
		cost = level;
		costPerSecond = 0.2;
		cooldown = 3;
		duration = 2 * level;
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
			user.ghostMode = true;
			user.mana -= cost;
			timeLeft = duration;
			cooldownLeft = 0.5;
			effect = new Ethereal(duration, this);
			user.affect(effect, true);
		}
		else if (cooldownLeft == 0)
		{
			if (!user.insideWall)
			{
				on = false;
				user.ghostMode = false;
				cooldownLeft = cooldown;
				timeLeft = 0;
				user.affect(effect, false);
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
			cooldownLeft = cooldown;
			timeLeft = 0;
			user.affect(effect, false);
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
		player.target = new Point(-1, -1);
	}
}
