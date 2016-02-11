package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Time_Stopped extends Effect
{
	double originalValue;

	public Time_Stopped(double duration1, Ability CA)
	{
		super("Time Stopped", duration1, 1, CA);
		stackable = true;
		removeOnDeath = false;
		timeAffecting = true;
	}

	public Effect clone()
	{
		Time_Stopped e = new Time_Stopped(this.duration, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		e.strength = this.strength;
		e.animFrame = this.animFrame;
		e.stackable = this.stackable;
		e.removeOnDeath = this.removeOnDeath;
		return e;
	}

	public void apply(Person target)
	{
		originalValue = target.timeEffect;
		target.timeEffect = 0;
	}

	public void unapply(Person target)
	{
		target.timeEffect = originalValue;
		originalValue = 0; //unnecessary but who cares
	}

	public void nextFrame(int frameNum)
	{
	}
}
