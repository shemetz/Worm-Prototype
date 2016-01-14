package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Life extends Ability
{

	public Sense_Life(int p)
	{
		super("Sense Life", p);
		range = (int) (50 * Math.pow(2, points));
		rangeType = "Circle area";
	}
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
