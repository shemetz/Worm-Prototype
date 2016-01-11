package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class _TEMPLATE_ extends Ability
{

	public _TEMPLATE_(int p)
	{
		super("TEMPLATE", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		@SuppressWarnings("unused")
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
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
