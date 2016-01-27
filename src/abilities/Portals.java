package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.Portal;

public class Portals extends Ability
{

	public Portal	p1, p2;
	public double	minPortalLength;
	public double	maxPortalLength;

	public Point	holdTarget	= null;

	public Portals(int p)
	{
		super("Portals", p);
		cost = (10 - level) / 2; // divided by 2 because 2 portals
		costType = "mana";
		cooldown = 0;
		range = 500;
		rangeType = "Ranged point";

		p1 = null;
		p2 = null;
		minPortalLength = 100;
		maxPortalLength = 2000;
	}

	public void use(Environment env, Person user, Point target)
	{
		double angle = Math.atan2(target.y - holdTarget.y, target.x - holdTarget.x);
		if (user.mana >= cost)
		{
			if (p1 == null)
			{
				double length = Math.min(maxPortalLength, Math.sqrt(Methods.DistancePow2(holdTarget.x, holdTarget.y, target.x, target.y)));
				length = Math.max(minPortalLength, Math.sqrt(Methods.DistancePow2(holdTarget.x, holdTarget.y, target.x, target.y)));
				p1 = new Portal(holdTarget.x + length / 2 * Math.cos(angle), holdTarget.y + length / 2 * Math.sin(angle), user.z, angle, length);
				env.portals.add(p1);
				user.mana -= cost;
			} else if (p2 == null)
			{
				double length = p1.length;
				p2 = new Portal(holdTarget.x + length / 2 * Math.cos(angle), holdTarget.y + length / 2 * Math.sin(angle), user.z, angle, length);
				env.portals.add(p2);
				p1.join(p2);
				on = true;
				user.mana -= cost;
			} else
			{
				on = false;
				// TODO fix the bugs that will result from removing portals in here
				env.portals.remove(p1);
				env.portals.remove(p2);
				p1 = null;
				p2 = null;
			}
		}
		holdTarget = null;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "portals";
		if (holdTarget == null) // when starting to hold mouse
			holdTarget = new Point(target.x, target.y);
	}
}
