package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Possessed extends Effect
{
	public Possessed(double duration, Ability CA)
	{
		super("Possessed", duration, 0, CA);
		stackable = true;
		removeOnDeath = true;
		removable = false;
	}

	public Effect clone()
	{
		Possessed e = new Possessed(this.duration, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		return e;
	}

	public void apply(Person target)
	{

	}

	public void unapply(Person target)
	{
		target.startStopPossession = true;
		target.possessionTargetID = target.possessingControllerID;
	}

	public void nextFrame(int frameNum)
	{

	}
}
