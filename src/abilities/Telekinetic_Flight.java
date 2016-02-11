package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Telekinetic_Flight extends Ability
{
	boolean gaveBonuses;
	public Telekinetic_Flight(int p)
	{
		super("Telekinetic Flight", p);
		costType = CostType.NONE;
		cooldown = 1;
		cost = 0;
		instant = true;
		gaveBonuses = false;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (!on && user.stamina > 2 && !user.prone && cooldownLeft == 0)
		{
			on = true;
			if (user.z == 0)
				user.z += 0.2;
			user.flySpeed = 900 * level; // 
			cooldownLeft = 0.5; // constant activation cooldown - to fix keys being stuck, etc.
			
			if (!gaveBonuses)
			{
				gaveBonuses = true;
				user.STRENGTH += 2;
				user.pushbackResistance += 0.5*(1 - user.pushbackResistance); //+50% pushback immunity
				user.evasion += 0.2*(1-user.evasion); //+20% evasion
			}
		} else if (on && cooldownLeft == 0)
		{
			on = false;
			cooldownLeft = cooldown;
			user.flySpeed = -1;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
		player.target = new Point(-1, -1);
	}
}
