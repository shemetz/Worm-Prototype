package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Charged_Regeneration extends _PassiveAbility
{

	double currLife;
	double timer;
	boolean wasDamaged;

	public Charged_Regeneration(int p)
	{
		super("Charged Regeneration", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;

		currLife = user.life;
		wasDamaged = false;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		wasDamaged = false;
		double amount = (0.1 * level * (currLife - user.life));
		if (amount > 0)
		{
			wasDamaged = true;
			user.charge += amount;
		}

		if (user.timeSinceLastHit < 3)
			timer = 0;
		else
			timer += deltaTime;

		double healAmount = 0.5 * 0.1 * level * (user.charge + 4); // +4 because.
		healAmount = Math.min(healAmount, user.maxLife - user.life);
		if (user.life < user.maxLife)
			if (user.charge >= healAmount)
				if (timer > 0.5)
				{
					user.charge -= healAmount;
					user.heal(healAmount);
					timer = 0;
				}
		currLife = user.life;
	}

	public boolean checkCharge(Environment env, Person user, double deltaTime)
	{
		if (wasDamaged)
			user.isChargingChargeAbility = true;
		return false;
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
