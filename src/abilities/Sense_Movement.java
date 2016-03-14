package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Movement extends _PassiveAbility
{

	public Sense_Movement(int p)
	{
		super("Sense Movement", p);
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void updateStats()
	{
		range = (int) (100 * Math.pow(2, level));

	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
