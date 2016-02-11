package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Structure extends Ability
{

	public Sense_Structure(int p)
	{
		super("Sense Structure", p);
		range = (int) (50 * Math.pow(2, level));
		rangeType = RangeType.CIRCLE_AREA;
	}
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
