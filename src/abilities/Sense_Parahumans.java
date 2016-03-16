package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Parahumans extends _PassiveAbility
{

	public Sense_Parahumans(int p)
	{
		super("Sense Parahumans", p);
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void updateStats()
	{
		range = (int) (50 * Math.pow(3, LEVEL));
		
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
