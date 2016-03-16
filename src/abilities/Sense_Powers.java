package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.EP;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Sense_Powers extends Ability
{
	public double[] details;
	double timer;
	double updatePeriod;
	public double angle;

	public Sense_Powers(int p)
	{
		super("Sense Powers", p);
		costType = CostType.NONE;
		rangeType = RangeType.CIRCLE_AREA;
		instant = true;

		details = new double[MAIN.numOfElements];
		for (int i = 0; i < details.length; i++)
			details[i] = 0;
		timer = 0;
	}

	public void updateStats()
	{
		cooldown = 1;
		range = (int) (50 * Math.pow(2, LEVEL));
		updatePeriod = 10 - LEVEL;

	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		on = false;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (cooldownLeft == 0)
		{
			on = !on;
			cooldownLeft = cooldown;
			timer = updatePeriod - 0.3;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		angle += 1.26873 * deltaTime;
		if (timer >= updatePeriod)
		{
			timer = 0;
			for (int i = 0; i < details.length; i++)
			{
				details[i] = 0;
			}
			for (Person p : env.people)
				if (!p.equals(user) && !p.dead)
				{
					double distancePow2 = Methods.DistancePow2(user.x, user.y, p.x, p.y);
					if (distancePow2 < Math.pow(range, 2))
						for (EP ep : p.DNA)
							details[ep.elementNum] += ep.points * (10 - Math.log10(distancePow2));
				}
		}
		timer += deltaTime;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
	}
}
