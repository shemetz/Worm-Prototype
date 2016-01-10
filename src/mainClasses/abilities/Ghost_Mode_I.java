package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Ghost_Mode_I extends Ability
{

	public Ghost_Mode_I(int p)
	{
		super("Ghost Mode I", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		/*
		 * On/Off: while On, be in Ghost Mode.
		 */
		if (!on) // entering
		{
			if (cooldownLeft > 0 || cost > user.mana)
				return;
			on = true;
			// TODO some kind of visual effect maybe?
			user.ghostMode = true;
			user.mana -= cost;
			timeLeft = points;
			cooldownLeft = 0.5;
		} else if (cooldownLeft == 0)
		{
			if (!user.insideWall)
			{
				on = false;
				user.ghostMode = false;
				cooldownLeft = cooldown;
				timeLeft = 0;
			} else
				timeLeft = 0;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "";
		player.target = new Point(-1, -1);
	}
}
