package abilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import effects.Nullified;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;

public class Nullification_Aura_I extends _PassiveAbility
{
	List<Person> affectedTargets;

	public Nullification_Aura_I(int p)
	{
		super("Nullification Aura I", p);
		rangeType = RangeType.CIRCLE_AREA;
		affectedTargets = new ArrayList<Person>();
	}

	public void updateStats()
	{
		range = 100 + LEVEL * 50;
	}

	public boolean viableTarget(Person p, Person user)
	{
		if (p.equals(user))
			return false;
		if (p.dead)
			return false;
		if (p.highestPoint() < user.z - verticalRange || user.highestPoint() < p.z - verticalRange)
			return false;
		return true;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		affectedTargets = new ArrayList<Person>();
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		for (Person other : env.people)
			if (viableTarget(other, user))
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
					other.affect(new Nullified(-1, true, this), false); // if the Nullified effect was undone, this line will do nothing
				}
			}
	}
}
