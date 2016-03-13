package abilities;

import java.awt.Point;

import effects.Ethereal;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Ghost_Mode_I extends Ability
{
	Ethereal effect;

	public Ghost_Mode_I(int p)
	{
		super("Ghost Mode I", p);
		costType = CostType.MANA;
		rangeType = RangeType.NONE;
		instant = true;
	}

	public void updateStats()
	{
		cost = Math.max(10, 2 * level);
		costPerSecond = 0.3;
		cooldown = 5;
		duration = level;
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
			user.onlyNaturalAbilities = true;
			user.mana -= cost;
			timeLeft = duration;
			cooldownLeft = 0.5;
			effect = new Ethereal(duration, this);
			user.affect(effect, true);
		}
		else if (cooldownLeft == 0) // NOTE: This ability can't normally be turned off prematurely because of the onlyNaturalAbilities part.
		{
			if (!user.insideWall)
			{
				on = false;
				user.ghostMode = false;
				user.onlyNaturalAbilities = false;
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
			user.onlyNaturalAbilities = false;
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
