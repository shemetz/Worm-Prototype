package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class ChronobiologyEffect extends Effect
{

	public ChronobiologyEffect(double duration1, double strength1, Ability CA)
	{
		super("Time Stretched", duration1, strength1, CA);
		stackable = true;
		removeOnDeath = true;
	}

	public Effect clone()
	{
		ChronobiologyEffect e = new ChronobiologyEffect(this.duration, this.strength, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		e.strength = this.strength;
		e.animFrame = this.animFrame;
		e.stackable = this.stackable;
		e.removeOnDeath = this.removeOnDeath;
		return e;
	}

	public void apply(Person target)
	{
		target.timeEffect *= strength;
	}

	public void unapply(Person target)
	{
		target.timeEffect /= strength;
	}

	public void nextFrame(int frameNum)
	{
	}
}
