package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

/**
 * An ability that when activated makes you fly.
 * 
 * @author Itamar
 *
 */
public class _FlightAbility extends Ability
{

	public double flySpeed;

	public _FlightAbility(String s, int p)
	{
		super(s, p);
		costType = CostType.STAMINA;
		instant = true;
		flySpeed = -1;
	}

	/**
	 * Starts flying: sets user's Z to 0.1 if it was 0, fly speed to whatever, and turns ability on. If already flying, stop flying.
	 */
	public void use(Environment env, Person user, Point target)
	{
		if (!on && !user.prone && cooldownLeft == 0)
		{
			on = true;
			if (user.z == 0)
				user.z += 0.1;
			user.flySpeed = flySpeed;
			cooldownLeft = 0.5; // constant activation cooldown - to fix keys being stuck, etc.
		}
		else if (on && cooldownLeft == 0)
		{
			on = false;
			cooldownLeft = cooldown;
			user.flySpeed = -1;
		}
	}

	/**
	 * Take stamina from the user (cost per second).
	 */
	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		user.stamina -= deltaTime * costPerSecond;
	}

	/**
	 * Stop flying.
	 */
	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			user.flySpeed = -1;
		on = false;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
		player.target = player.Point();
	}
}
