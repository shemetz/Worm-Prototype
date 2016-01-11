package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Mana_and_Stamina extends Ability
{

	public Sense_Mana_and_Stamina(int p)
	{
		super("Sense Mana and Stamina", p);
	}
	
	
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
