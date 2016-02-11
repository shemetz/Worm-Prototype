package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Tangled extends Effect
{
	public double damage;

	public Tangled(int strength1, Ability CA)
	{
		super("Tangled", 4, strength1, CA);
		stackable = true;
		removeOnDeath = false;
		damage = 1;
	}

	public Effect clone()
	{
		Effect e = new Tangled((int) this.strength, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		e.strength = this.strength;
		e.animFrame = this.animFrame;
		e.stackable = this.stackable;
		e.removeOnDeath = this.removeOnDeath;
		return e;
	}

	public void apply(Person target)
	{
		int numOfTangles = 0;
		for (Effect e : target.effects)
			if (e instanceof Tangled)
			{
				// reset timers for existing tangles
				e.timeLeft = e.duration;
				numOfTangles++;
			}
		if (Math.random() < numOfTangles * 0.10) // chance is 10% * number of existing tangles
			target.affect(this, false); // don't add this
	}

	public void unapply(Person target)
	{

	}

	public void nextFrame(int frameNum)
	{

	}
}
