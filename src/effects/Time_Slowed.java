package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Time_Slowed extends Effect
{
	// Slows to 50% of original amount

	public Time_Slowed(double duration1, Ability CA)
	{
		super("Time Slowed", duration1, 0.5, CA);
		stackable = true;
		removeOnDeath = true;
		duration *= strength; // so that the actual time is the actual time.
		timeLeft *= strength; // ditto
	}

	public Effect clone()
	{
		Time_Slowed e = new Time_Slowed(this.duration, this.creatorAbility);
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
