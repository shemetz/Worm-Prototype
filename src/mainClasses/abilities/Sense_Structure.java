package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Structure extends Ability
{

	public Sense_Structure(int p)
	{
		super("Sense_Structure", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
