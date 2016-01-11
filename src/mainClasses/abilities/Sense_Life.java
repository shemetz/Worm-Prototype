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
	}
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
