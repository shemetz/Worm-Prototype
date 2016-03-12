package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Muscle_Charge extends Ability
{

	double extraStrength;

	public Muscle_Charge(int p)
	{
		super("Muscle Charge", p);
		costPerSecond = 20;
		cost = 0;
		costType = CostType.CHARGE;
		instant = true;
		cooldown = 5;

		extraStrength = Math.pow(2, level);
	}

	public void use(Environment env, Person user, Point target)
	{
		if (cooldownLeft == 0)
		{
			on = !on;
			cooldownLeft = cooldown;
			if (on)
				user.STRENGTH += extraStrength;
			else
				user.STRENGTH -= extraStrength;
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
