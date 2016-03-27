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

	public void use(Environment env, Person user, Point target)
	{
		if (!on)
		{
			armor = new Armor(AR, armorType);
			user.putArmor(armor);
			on = true;
		}
		else
		{
			armor.armorRating = 0;
			// on = false; //not necessary, will do it anyways in maintain(...)
		}
	}

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
