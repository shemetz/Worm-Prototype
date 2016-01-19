package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Wound_Regeneration_I extends Ability
{

	public Wound_Regeneration_I(int p)
	{
		super("Wound Regeneration I", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.lifeRegen += val * 2 * points;
	}
}
