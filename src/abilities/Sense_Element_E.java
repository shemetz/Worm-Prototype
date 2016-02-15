package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Element_E extends Ability
{

	public Sense_Element_E(String elementName, int p)
	{
		super("Sense Element <" + elementName + ">", p);
		cost = -1;
		costType = CostType.NONE;
		cooldown = -1;
		range = (int) (50 * Math.pow(3, level));
		rangeType = RangeType.CIRCLE_AREA;
	}
	
	public void disable(Environment env, Person user)
	{
		disabled = true;
		on = false;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
