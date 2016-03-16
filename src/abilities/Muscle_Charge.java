package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Muscle_Charge extends Ability
{

	public Muscle_Charge(int p)
	{
		super("Muscle Charge", p);
		costType = CostType.CHARGE;
		instant = true;
	}

	public void updateStats()
	{
		cooldown = 5;
		costPerSecond = 20;
		amount = Math.pow(2, level);
		chargeRate = 5;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (cooldownLeft == 0)
		{
			on = !on;
			cooldownLeft = cooldown;
			if (on)
				user.STRENGTH += amount;
			else
				user.STRENGTH -= amount;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		user.charge -= costPerSecond * deltaTime;
		if (user.charge <= 0)
			use(env, user, target);
	}

	public boolean checkCharge(Environment env, Person user, double deltaTime)
	{
		return user.inCombat;
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
