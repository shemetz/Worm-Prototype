package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Parahumans extends Ability
{

	public Sense_Parahumans(int p)
	{
		super("Sense Parahumans", p);
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
