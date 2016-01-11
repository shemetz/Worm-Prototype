package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Parahumans extends Ability
{

	public Sense_Parahumans(int p)
	{
		super("Sense Parahumans", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
