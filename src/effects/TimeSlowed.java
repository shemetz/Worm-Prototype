package effects;

import mainClasses.Effect;
import mainClasses.Ability;
import mainClasses.Person;

public class TimeSlowed extends Effect{
	public TimeSlowed(int level, Ability CA){
		super("Time Slowed", 2 * level, 0.1, CA);
		stackable = false;
	}
	
	public void apply(Person target){
		target.timeEffect *= strength;
	}
	
	public void unapply(Person target){
		target.timeEffect /= strength;
	}
}
