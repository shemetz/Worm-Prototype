package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Life extends _PassiveAbility
{

	public Sense_Life(int p)
	{
		super("Sense Life", p);
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void updateStats()
	{
		range = (int) (50 * Math.pow(2, level));
		
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
