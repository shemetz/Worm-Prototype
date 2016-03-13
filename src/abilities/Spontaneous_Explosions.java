package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Spontaneous_Explosions extends _PassiveAbility
{
	public double minimumDamageNeeded;
	public boolean prepareToExplode;
	double range2;

	public Spontaneous_Explosions(int p)
	{
		super("Spontaneous Explosions", p);
		rangeType = Ability.RangeType.EXACT_RANGE;
		costType = Ability.CostType.MANA;
	}

	public void updateStats()
	{
		costPerSecond = 1;
		range = 200;
		range2 = 100;
		cooldown = (double) 5 / level; // doubled if user is on <50% health. No pun intended
		radius = 100;
		damage = level * 4;
		pushback = level * 8;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (user.mana >= costPerSecond * deltaTime)
		{
			user.mana -= costPerSecond * deltaTime;
			if (cooldownLeft == 0)
			{
				double angle = Math.random() * Math.PI * 2;
				double distance = range + (Math.random() * 2 - 1) * range2;
				env.createExplosion(user.x + Math.cos(angle) * distance, user.y + Math.sin(angle) * distance, user.z, radius, damage, pushback, -1);
				cooldownLeft = cooldown;
				if (user.life < user.maxLife / 2)
					cooldownLeft /= 2;
			}
		}
		else
			on = false;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		on = false;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
	}
}
