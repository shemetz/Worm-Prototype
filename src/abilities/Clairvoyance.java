package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Clairvoyance extends Sense_Powers
{
	double damagePerSecond;

	public Clairvoyance(int p)
	{
		super(p);
		name = "Clairvoyance";
		updateTags();

		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		stopsMovement = true;
		maintainable = true;
		instant = true;
	}

	public void updateStats()
	{
		costPerSecond = 2;
		damagePerSecond = costPerSecond * 5;
		cooldown = 3;
		range = 100 * Math.pow(2, LEVEL);
		updatePeriod = 10 - LEVEL;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (cooldownLeft == 0 || disabled)
		{
			if (!on)
				if (user.maintaining)
					return;
			if (on)
				costType = CostType.MANA;

			user.maintaining = !user.maintaining;
			on = !on;

			if (on)
			{
				user.notMovingTimer = 99;
				timer = updatePeriod - 0.3;
			}
			else
			{
				cooldownLeft = cooldown;
				user.notMovingTimer = 0;
			}

			// TODO: consider, instead / in addition to the normal effect of this, simply extending the visibility of the player (flightvisionradius?)
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		super.maintain(env, user, target, deltaTime);
		if (costType == CostType.MANA)
		{
			user.mana -= costPerSecond * deltaTime;
			if (user.mana <= 0)
				costType = CostType.LIFE;
		}
		else if (costType == CostType.LIFE)
		{
			user.damage(damagePerSecond * deltaTime);
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, null);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
	}
}
