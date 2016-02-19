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

	final double verticalRange = 5;

	public Nullification_Aura_I(int p)
	{
		super("Nullification Aura I", p);
		range = 100 + level * 50;
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		affectedTargets = new ArrayList<Person>();
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
						other.affect(new Nullified(-1, true, this), false); // if the Nullified effect was undone, this line will do nothing
					}
				}
	}
}
