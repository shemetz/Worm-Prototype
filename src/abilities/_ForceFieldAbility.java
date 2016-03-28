package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Person;
import mainClasses.Player;

/**
 * An activated ability that creates a force field in front of you.
 * 
 * @author Itamar
 *
 */
public class _ForceFieldAbility extends Ability
{
	public int length, width, height, life, armor;
	public double decayRate;

	public _ForceFieldAbility(String n, int p)
	{
		super(n, p);
		costType = CostType.MANA;

		height = 1;
		length = 0;
		width = 0;
		life = 0;
		armor = 0;
	}

	/**
	 * Creates a force field in front of the user (depending on angle to target).
	 */
	public void use(Environment env, Person user, Point target)
	{
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		if (!user.maintaining && cost <= user.mana && cooldownLeft == 0)
		{
			// TODO check collisions
			ForceField forcey = new ForceField(user.x + range * Math.cos(angle), user.y + range * Math.sin(angle), user.z, length, width, angle + 0.5 * Math.PI, life, decayRate);
			forcey.armor = armor;
			env.FFs.add(forcey);
			user.mana -= cost;
			cooldownLeft = cooldown;
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	/**
	 * Also rotates the player towards target.
	 */
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);

		player.aimType = Player.AimType.CREATE_FF;
		player.target = new Point((int) (player.x + range * Math.cos(angle)), (int) (player.y + range * Math.sin(angle)));

		if (!player.leftMousePressed)
			player.rotate(angle, 3.0 * deltaTime);
	}
}
