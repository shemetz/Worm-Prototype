package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.Portal;
import mainResourcesPackage.SoundEffect;

public class Portals extends Ability
{

	public Portal p1, p2;
	public double minPortalLength;
	public double maxPortalLength;
	public static double minimumDistanceBetweenPortalsPow2;
	public boolean alignPortals;
	int idOfFirstPortalEnv = -1;

	public Point holdTarget = null;

	public Portals(int p)
	{
		super("Portals", p);
		costType = CostType.MANA;
		rangeType = RangeType.EXACT_RANGE;
		toggleable = true;

		alignPortals = false;
		p1 = null;
		p2 = null;
		minimumDistanceBetweenPortalsPow2 = 120 * 120;
		sounds.add(new SoundEffect("Portal_failure.wav"));
	}

	public void updateStats()
	{
		cost = (10 - level) / 2; // divided by 2 because 2 portals
		range = 1000;
		minPortalLength = 100;
		maxPortalLength = 2000;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		if (holdTarget == null)
		{
			alignPortals = false;
			return;
		}
		if (p2 != null)
		{
			removePortals();
			alignPortals = false;
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
			}
			else // lock to cardinal directions
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
			setSounds(user.Point());
			if (p1 == null)
			{
				double length = Math.min(maxPortalLength, Math.sqrt(Methods.DistancePow2(holdTarget.x, holdTarget.y, target.x, target.y)));
				length = Math.max(minPortalLength, length);
				p1 = new Portal(holdTarget.x + length / 2 * Math.cos(angle), holdTarget.y + length / 2 * Math.sin(angle), user.z, angle, length, env.id);
				if (env.checkPortal(p1))
				{
					env.portals.add(p1);
					user.mana -= cost;
					p1.playPortalSound();
				}
				else
				{
					p1 = null;
					sounds.get(0).play();
				}
			}
			else if (p2 == null)
			{
				double length = p1.length;
				p2 = new Portal(holdTarget.x + length / 2 * Math.cos(angle), holdTarget.y + length / 2 * Math.sin(angle), user.z, angle, length, env.id);
				if (!env.checkPortal(p2))
				{
					p2 = null;
					sounds.get(0).play();
				}
				else
				{
					env.portals.add(p2);
					p1.join(p2);
					on = true;
					user.mana -= cost;
					p2.playPortalSound();
				}
			}
		}
		alignPortals = false;
		holdTarget = null;
	}

	public void removePortals()
	{
		on = false;
		p1.destroyThis = true;
		p2.destroyThis = true;
		p1.partner = null;
		p2.partner = null;
		p1 = null;
		p2 = null;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		// empty but existing on purpose
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			removePortals();
	}

	public void toggle()
	{
		alignPortals = !alignPortals;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.PORTALS;
		if (holdTarget == null) // when starting to hold mouse
			holdTarget = new Point(target.x, target.y);
		if (player.rightMousePressed) // cancel
			holdTarget = null;
	}
}
