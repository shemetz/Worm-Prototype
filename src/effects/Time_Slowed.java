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
	}
	public void init()
	{
		stackable = true;
		removeOnDeath = true;
		duration *= strength; // so that the actual time is the actual time.
		timeLeft *= strength; // ditto
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
