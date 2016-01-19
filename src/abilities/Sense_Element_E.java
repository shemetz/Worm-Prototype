package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Element_E extends Ability
{

	public Sense_Element_E(String elementName, int p)
	{
		super("Sense Element <"+elementName+">", p);
		cost = -1;
		costType = "none";
		cooldown = -1;
		range = (int) (50 * Math.pow(3, points));
		rangeType = "Circle area";
	}
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
