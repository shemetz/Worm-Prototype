package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Person;
import mainClasses.Player;

public class Strong_Force_Field extends Ability
{

	public Strong_Force_Field(int p)
	{
		super("Strong Force Field", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
		/*
		 * Create a strong, decaying static force field, to be used as temporary defense.
		 */
		double angle = Math.atan2(target.y-user.y, target.x-user.x);
		if (!user.maintaining && cost <= user.mana && cooldownLeft == 0)
		{
			// TODO check collisions
			ForceField forcey = new ForceField(user.x + range * Math.cos(angle), user.y + range * Math.sin(angle), user.z, 100 + 50 * points, 5 + points * 7,
					angle + 0.5 * Math.PI, 80 * points, 1);
			forcey.armor = points;
			env.FFs.add(forcey);
			user.mana -= cost;
			cooldownLeft = cooldown;
		}
	}
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y-player.y, target.x-player.x);

		player.targetType = "createFF";
		player.target = new Point((int) (player.x + range * Math.cos(angle)), (int) (player.y + range * Math.sin(angle)));
		// length
		targetEffect2 = 100 + 50 * (int) points;
		// width
		targetEffect3 = 5 + 7 * points;

		if (!player.leftMousePressed)
			player.rotate(angle, 3.0 * deltaTime);
	}
}
