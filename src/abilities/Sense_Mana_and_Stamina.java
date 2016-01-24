package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Mana_and_Stamina extends Ability
{

	public Sense_Mana_and_Stamina(int p)
	{
		super("Sense Mana and Stamina", p);
		range = (int) (50 * Math.pow(2, level));
		rangeType = "Circle area";
	}
	
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
