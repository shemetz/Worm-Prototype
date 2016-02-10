package mainClasses;

public class Effect
{
	public double duration; // -1 = forever
	public double timeLeft; // -1 = forever
	public String name; // can be an int or a short, honestly, but then the programming would be difficulter
	public double strength;
	public int animFrame;
	public Ability creatorAbility; // to avoid same ability giving same effect multiple times to same person
	
	public boolean removeOnDeath;
	public boolean stackable;

	public Effect(String name1, double duration1, double strength1, Ability creatorAbility1)
	{
		creatorAbility = creatorAbility1;
		name = name1;
		duration = duration1;
		timeLeft = duration;
		strength = strength1;
		animFrame = 0;
		init();
	}

	public void init()
	{
		// you must extend this!! important!
		MAIN.errorMessage("YOU FORGOT TO ADD AN INIT METHOD TO THE EFFECT CALLED " + name + "!!!!!!111111");
	}

	public Effect()
	{

	}

	public Effect clone()
	{
		// does NOT clone any variable that belongs to abilities that extend this one! Please know this!
		Effect e = new Effect();
		e.duration = this.duration;
		e.timeLeft = this.timeLeft;
		e.name = this.name;
		e.strength = this.strength;
		e.animFrame = this.animFrame;
		e.stackable = this.stackable;
		e.creatorAbility = this.creatorAbility; // SAME ABILITY INSTANCE
		e.removeOnDeath = this.removeOnDeath;
		e.init();
		return e;
	}

	@SuppressWarnings("unused")
	public void apply(Person target)
	{
		MAIN.errorMessage("Error message. 1234567");
	}

	@SuppressWarnings("unused")
	public void unapply(Person target)
	{
		MAIN.errorMessage("String literal out of exception campaign");
	}

	public void update(Person target, double deltaTime)
	{
		timeLeft -= deltaTime;
		if (timeLeft < 0)
		{
			unapply(target);
		}
	}

	@SuppressWarnings("unused")
	public void nextFrame(int frameNum)
	{
		MAIN.errorMessage("Please don't kill me! I have a dog and kids!");
	}
}