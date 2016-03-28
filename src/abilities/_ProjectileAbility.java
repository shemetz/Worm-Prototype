package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

/**
 * An ability that shoots a projectile.
 * 
 * @author Itamar
 *
 */
public class _ProjectileAbility extends Ability
{
	double startingDistance;
	public double velocity;
	public double size;

	public _ProjectileAbility(String name, int p)
	{
		super(name, p);
		velocity = 0;
		size = 1;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	/**
	 * Rotates the player towards the target.
	 */
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);
		player.aimType = Player.AimType.NONE;
		player.target = new Point((int) (player.x + startingDistance * Math.cos(angle)), (int) (player.y + startingDistance * Math.sin(angle)));
		if (!player.leftMousePressed)
			player.rotate(angle, 3.0 * deltaTime);
	}
}
