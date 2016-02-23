package abilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import effects.Nullified;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Nullification_Aura_II extends Ability
{
	List<Person> affectedTargets;

	final double verticalRange = 5;

	public Nullification_Aura_II(int p)
	{
		super("Nullification Aura II", p);
		costType = CostType.MANA;
		range = 100 + level * 100;
		rangeType = RangeType.CIRCLE_AREA;
		cost = 5;
		instant = true;
		cooldown = 0.8;
		affectedTargets = new ArrayList<Person>();
	}

	public void use(Environment env, Person user, Point target)
	{
		if (on)
		{
			int oldRange = range;
			range = 0;
			maintain(env, user, null, 0);
			range = oldRange;
		}
		if (cost <= user.mana && cooldownLeft == 0)
		{
			cooldownLeft = cooldown;
			user.mana -= cost;
			on = !on;
			affectedTargets = new ArrayList<Person>();
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		for (Person other : env.people)
			if (!other.equals(user))
				if (other.z <= user.z + verticalRange && other.z >= user.z - verticalRange)
				{
					if (Methods.DistancePow2(other.Point(), user.Point()) < range * range)
					{
						if (!affectedTargets.contains(other))
						{
							affectedTargets.add(other);
							other.affect(new Nullified(-1, true, this), true);
						}
					}
					else if (affectedTargets.contains(other))
					{
						affectedTargets.remove(other);
						other.affect(new Nullified(-1, true, this), false);
					}
				}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
		{
			int oldRange = range;
			range = 0;
			maintain(env, user, null, 0);
			range = oldRange;
			on = false;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
		player.target = new Point(-1, -1);
	}
}
