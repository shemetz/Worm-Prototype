package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Element_E extends _PassiveAbility
{

	public static String[] applicable = new String[]
	{ "Fire", "Water", "Metal", "Plant", "Earth", "Lava", "Acid", "Electricity", "Energy", "Wind", "Ice", "Flesh" };

	public Sense_Element_E(String elementName, int p)
	{
		super("Sense Element <" + elementName + ">", p);
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void updateStats()
	{
		range = (int) (50 * Math.pow(3, level));
		
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
