package abilities;

import java.awt.Point;

import effects.TimeSlowed;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;

public class SlowTarget extends ApplyEffect{
	public SlowTarget(int p){
		super("Slow Target", p, ApplyEffect.targetTypes.TARGETED, VisualEffect.Type.HEAL);
		cost = 0;
		costPerSecond = 1;
		costType = "mana";
		cooldown = 0;
		range = 50 * level;
		rangeType = "Circle area";
		stopsMovement = false;
		maintainable = false;
		instant = false;
	}
	
	public Effect effect(){
		return new TimeSlowed(level, this);
	}
	
	public boolean viableTarget(Person p, Person user){
		if (p != user){
			return true;
		}else{
			return false;
		}
	}
	
	public void use(Environment env, Person user, Point target){
		addNewTargets(env, user);
		if (user.mana >= cost && !user.maintaining && cooldownLeft == 0 && !user.prone){
			user.mana -= cost;
			cooldownLeft = cooldown;
			for (Person p : targets){
				p.affect(effect(), true);
			}
		}
	}
}
