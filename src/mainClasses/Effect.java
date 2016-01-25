package mainClasses;

public class Effect
{
	public double	duration;	// -1 = forever
	public double	timeLeft;	// -1 = forever
	public String	name;		// can be an int or a short, honestly, but then the programming would be difficulter
	public int		strength;
	public int		animFrame;

	public Effect(String type, double duration1, int strength1)
	{
		name = type;
		duration = duration1;
		timeLeft = duration;
		strength = strength1;
		animFrame = 0;
	}

	// A constructor for indefinite effects
	public Effect(String type, int strength1)
	{
		name = type;
		duration = -1;
		timeLeft = -1;
		strength = strength1;
		animFrame = 0;
	}
	
	public void apply(Person target){
		target.effects.add(this);
	}
	
	public void remove(Person target){
		target.effects.remove(this);
	}
	
	public void update(Person target, double deltaTime){
		timeLeft -= deltaTime;
		if (timeLeft < duration){
			remove(target);
		}
	}
	
	public void nextFrame(int frameNum){}
}