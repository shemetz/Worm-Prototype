package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Heal_II extends Ability
{

	public Heal_II(int p)
	{
		super("Heal II", p);
		cost = 0;
		costPerSecond = 3;
		costType = "mana";
		cooldown = 0;
		range = 100 * level;
		rangeType = "Circle area";
		stopsMovement = false;
		maintainable = true;
		instant = true;
	}
	
	public void use(Environment env, Person user, Point target)
	{
		
	}
}
