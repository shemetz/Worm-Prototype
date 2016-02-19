package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Nullified extends Effect
{
	boolean removeEffects;

	public Nullified(double duration1, boolean removeEffects1, Ability CA)
	{
		super("Nullified", duration1, 0, CA);
		stackable = false;
		removeOnDeath = true;
		removable = false;
		removeEffects = removeEffects1;
	}

	public Effect clone()
	{
		Nullified e = new Nullified(this.duration, this.removeEffects, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		e.strength = this.strength;
		e.removeEffects = this.removeEffects;
		return e;
	}

	public void apply(Person target)
	{
		for (Ability a : target.abilities)
			if (!a.natural)
				a.prepareToDisable = true; // "disables" the ability
		if (removeEffects)
			for (Effect e : target.effects)
				if (e.removable)
					e.timeLeft = 0; // "removes" the effect
	}

	public void unapply(Person target)
	{
		for (Ability a : target.abilities)
			if (!a.natural)
				a.prepareToEnable = true;
	}

	public void nextFrame(int frameNum)
	{
	}
}
