package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Ethereal extends Effect
{
	public Ethereal(double duration, Ability CA)
	{
		super("Ethereal", duration, 0, CA);
		stackable = false;
		removeOnDeath = true;
	}

	public Effect clone()
	{
		Ethereal e = new Ethereal(this.duration, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		return e;
	}

	public void apply(Person target)
	{

	}

	public void unapply(Person target)
	{
	}

	public void nextFrame(int frameNum)
	{
			
	}
}
