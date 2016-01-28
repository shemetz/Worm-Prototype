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
	public double	minimumDistanceBetweenPortalsPow2;
	public boolean	alignPortals;

	public Point	holdTarget	= null;

	public Portals(int p)
	{
		super("Portals", p);
		cost = (10 - level) / 2; // divided by 2 because 2 portals
		costType = "mana";
		cooldown = 0;
		range = 1000;
		rangeType = "Ranged circular area";

		alignPortals = false;
		p1 = null;
		p2 = null;
		minPortalLength = 100;
		maxPortalLength = 2000;
		minimumDistanceBetweenPortalsPow2 = 80 * 80;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (holdTarget == null)
			return;
		if (p2 != null)
		{
			removePortals(env);
			holdTarget = null;
			return;
		}
		double angle = Math.atan2(target.y - holdTarget.y, target.x - holdTarget.x);
		// While pressing left- mouse button
		if (alignPortals)
		{
			if (p1 != null) // parallel portals
			{
				holdTarget.x = (int) (target.x - p1.length / 2 * Math.cos(p1.angle));
				holdTarget.y = (int) (target.y - p1.length / 2 * Math.sin(p1.angle));
				angle = p1.angle;
			} else // lock to cardinal directions
			{
				double length = Math.min(maxPortalLength, Math.sqrt(Methods.DistancePow2(holdTarget.x, holdTarget.y, target.x, target.y)));
				length = Math.max(minPortalLength, length);
				angle += Math.PI; // angle is between 0 and TAU
				angle = (int) ((angle / Math.PI * 180 + 45) / 90) * 90 + 180;
				angle = angle / 180 * Math.PI;
			}
		}
		if (user.mana >= cost)
		{
			if (p1 == null)
			{
				double length = Math.min(maxPortalLength, Math.sqrt(Methods.DistancePow2(holdTarget.x, holdTarget.y, target.x, target.y)));
				length = Math.max(minPortalLength, length);
				p1 = new Portal(holdTarget.x + length / 2 * Math.cos(angle), holdTarget.y + length / 2 * Math.sin(angle), user.z, angle, length);
				env.portals.add(p1);
				user.mana -= cost;
			} else if (p2 == null)
			{
				double length = p1.length;
				p2 = new Portal(holdTarget.x + length / 2 * Math.cos(angle), holdTarget.y + length / 2 * Math.sin(angle), user.z, angle, length);
				if (portalsCollide(p1, p2))
				{
					p2 = null;
					// TODO portal creation failure sound effect
				} else
				{
					env.portals.add(p2);
					p1.join(p2);
					on = true;
					user.mana -= cost;
				}
			}
		}
		holdTarget = null;
	}

	public void removePortals(Environment env)
	{
		on = false;
		env.portals.remove(p1);
		env.portals.remove(p2);
		p1 = null;
		p2 = null;
	}

	public boolean portalsCollide(Portal a, Portal b)
	{
		if (a.Line2D().intersectsLine(b.Line2D()))
			return true;
		if (Methods.getSegmentPointDistancePow2(b.start.x, b.start.y, b.end.x, b.end.y, a.start.x, a.start.y) < minimumDistanceBetweenPortalsPow2)
			return true;
		if (Methods.getSegmentPointDistancePow2(b.start.x, b.start.y, b.end.x, b.end.y, a.end.x, a.end.y) < minimumDistanceBetweenPortalsPow2)
			return true;
		if (Methods.getSegmentPointDistancePow2(a.start.x, a.start.y, a.end.x, a.end.y, b.start.x, b.start.y) < minimumDistanceBetweenPortalsPow2)
			return true;
		if (Methods.getSegmentPointDistancePow2(a.start.x, a.start.y, a.end.x, a.end.y, b.end.x, b.end.y) < minimumDistanceBetweenPortalsPow2)
			return true;
		return false;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "portals";
		if (holdTarget == null) // when starting to hold mouse
			holdTarget = new Point(target.x, target.y);
		if (player.leftMousePressed) // Parallel portals
			alignPortals = true;
		else
			alignPortals = false;
		if (player.rightMousePressed) // cancel
			holdTarget = null;
	}
}
