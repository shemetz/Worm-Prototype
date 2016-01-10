package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Ranged_Explosion extends Ability
{

	public Ranged_Explosion(int p)
	{
		super("Ranged Explosion", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
		if (user.mana >= cost && !user.maintaining && cooldownLeft == 0 && !user.prone)
		{
			// TODO make it not only in the user'z Z but in the one the user tried to do (most likely 0, unless cursor is above another object)
			env.createExplosion(target.x, target.y, user.z, areaRadius, points * 3, points * 3, -1);
			user.mana -= cost;
			cooldownLeft = cooldown;
		}
	}
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "explosion";
	}
}
