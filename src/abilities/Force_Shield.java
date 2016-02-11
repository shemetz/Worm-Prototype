package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Person;
import mainClasses.Player;

public class Force_Shield extends ForceFieldAbility
{

	public Force_Shield(int p)
	{
		super("Force Shield", p);
		cost = 3;
		costType = CostType.MANA;
		cooldown = 1;
		range = 68;
		rangeType = RangeType.EXACT_RANGE;
	}

	public void use(Environment env, Person user, Point target)
	{
		/*
		 * Create a decaying static force field, to be used as temporary defense.
		 */
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		if (!user.maintaining && cost <= user.mana && cooldownLeft == 0)
		{
			// TODO check collisions
			env.FFs.add(new ForceField(user.x + range * Math.cos(angle), user.y + range * Math.sin(angle), user.z, 100 + 100 * level / 3, 10 + level * 1, angle + 0.5 * Math.PI, 20 * level, 0));
			user.mana -= cost;
			cooldownLeft = cooldown;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);

		player.aimType = Player.AimType.CREATE_FF;
		player.target = new Point((int) (player.x + range * Math.cos(angle)), (int) (player.y + range * Math.sin(angle)));
		
		width = 100 + 100 * (int) level / 3;
		height = 10 + (int) level * 1;

		if (!player.leftMousePressed)
			player.rotate(angle, 3.0 * deltaTime);
	}
}
