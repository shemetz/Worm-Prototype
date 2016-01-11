package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Movement extends Ability
{

	public Sense_Movement(int p)
	{
		super("Sense Movement", p);
	}
	
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
