package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Wound_Regeneration_II extends Ability
{

	public Wound_Regeneration_II(int p)
	{
		super("Wound Regeneration II", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.lifeRegen += val * 2 * points;
	}
}
