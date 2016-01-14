package mainClasses.abilities;

import java.awt.Point;
import java.util.Random;

import mainClasses.Ability;
import mainClasses.EP;
import mainClasses.Environment;
import mainClasses.Main;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Sense_Powers extends Ability
{
	public int[]	details;
	Random			random	= new Random();
	double			timer;
	double updatePeriod;

	public Sense_Powers(int p)
	{
		super("Sense Powers", p);
		cost = 0;
		costType = "none";
		cooldown = 1;
		range = (int) (50 * Math.pow(2, points));
		rangeType = "Circle area";
		instant = true;
		
		details = new int[Main.numOfElements];
		for (int i = 0; i < details.length; i++)
			details[i] = 0;
		timer = 0;
		updatePeriod = 10-points;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (cooldownLeft == 0)
		{
			on = !on;
			cooldownLeft = cooldown;
			timer = updatePeriod;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (timer >= updatePeriod)
		{
			timer = 0;
			for (int i = 0; i < details.length; i++)
			{
				details[i] = (int) (random.nextGaussian() * 2); // deviation of 2 really seems like the best one.
			}
			for (Person p : env.people)
				if (!p.equals(user))
				{
					double distancePow2 = Methods.DistancePow2(user.x, user.y, p.x, p.y);
					if (distancePow2 < Math.pow(range, 2))
						for (EP ep : p.DNA)
							details[ep.elementNum] += ep.points;
				}
		}
		timer += deltaTime;
	}
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "";
		player.target = new Point(-1, -1);
	}
}
