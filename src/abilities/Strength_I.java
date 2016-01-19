package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Strength_I extends Ability
{

	public Strength_I(int p)
	{
		super("Strength I", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		int v = on ? -1 : 1;
		user.STRENGTH += v * points;
		user.FITNESS += v * 1;
		on = !on;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		@SuppressWarnings("unused")
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		@SuppressWarnings("unused")
		double angle = Math.atan2(player.target.y - player.y, player.target.x - player.x);
	}
}
