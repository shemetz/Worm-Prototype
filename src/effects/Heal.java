package effects;

import mainClasses.Effect;
import mainClasses.Main;
import mainClasses.Person;
import mainClasses.Resources;

public class Heal extends Effect{
	public Heal(double duration1, int strength1, String type){
		super(type, duration1, strength1);
	}
	
	public Heal(String type, int strength1){
		super(type, strength1);
		duration = 0;
	}
	
	public void apply(Person target){
		super.apply(target);
		target.lifeRegen += 2 * strength;
	}
	
	public void remove(Person target){
		super.remove(target);
		target.lifeRegen -= 2 * strength;
	}
}
