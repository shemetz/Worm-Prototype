package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Stunned extends Effect
{
	public Stunned(int strength1, Ability CA)
	{
		super("Stunned", -1, strength1, CA);
		stackable = false;
		removeOnDeath = true;
	}

	public Effect clone()
	{
		Stunned e = new Stunned((int) this.strength, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		e.strength = this.strength;
		e.animFrame = this.animFrame;
		e.stackable = this.stackable;
		e.removeOnDeath = this.removeOnDeath;
		return e;
	}

	public void apply(Person target)
	{
		;
	}

	public void unapply(Person target)
	{
		;
	}

	public void nextFrame(int frameNum)
	{
		;
	}
}
