package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Armor;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class _ArmorAbility extends Ability
{

	Armor armor;
	public double decayRate;
	public double AR;
	public double touchDamage;
	public String armorType;
	double touchDamageTimer = 0;

	public _ArmorAbility(String name, int p)
	{
		super(name, p);
		costType = CostType.MANA;
		rangeType = RangeType.NONE;
	}

	/**
	 * Puts an armor on the user if off, sets the armor's armorRating to 0 if on.
	 */
	public void use(Environment env, Person user, Point target)
	{
		if (!on && cost >= user.mana)
		{
			armor = new Armor(AR, armorType);
			user.putArmor(armor);
			user.mana -= cost;
			on = true;
		}
		else if (on)
		{
			armor.armorRating = 0;
			// on = false; //not necessary, will do it anyways in maintain(...)
		}
	}

	/**
	 * uses {@link Armor#reduce(double)} on {@link #armor} with decayRate*deltaTime*AR, and hits people near the user if touchDamageTimer >= 1 (and resets it to 0)
	 */
	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		armor.reduce(decayRate * deltaTime * AR);
		touchDamageTimer += deltaTime;
		if (touchDamageTimer >= 1)
			for (Person p : env.people)
				if (p.highestPoint() > user.z && user.highestPoint() > p.z)
					if (!p.equals(user))
						if (Methods.DistancePow2(p.x, p.y, user.x, user.y) <= Math.pow(user.radius + p.radius, 2))
						{
							env.hitPerson(p, touchDamage, touchDamage, Math.atan2(p.y - user.y, p.x - user.x), elementNum);
							touchDamageTimer = 0;
						}
		if (!armor.equipped)
			on = false;
	}

	/**
	 * Removes armor (by setting its AR to 0).
	 */
	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (armor != null)
			armor.armorRating = 0;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.AIMLESS;
	}
}
