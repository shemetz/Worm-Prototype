package mainClasses;

public class Effect
{
	public double	duration;	// -1 = forever
	public double	timeLeft;	// -1 = forever
	public String	name;		// can be an int or a short, honestly, but then the programming would be difficulter
	public int		strength;
	public int		animFrame;
	public boolean	stackable;

	public Effect(String type, double duration1, int strength1)
	{
		name = type;
		duration = duration1;
		timeLeft = duration;
		strength = strength1;
		animFrame = 0;
		// stackable - depends
	}

	public void apply(Person target)
	{
		target.effects.add(this);
	}

	public void remove(Person target)
	{
		target.effects.remove(this);
	}

	public void update(Person target, double deltaTime)
	{
		timeLeft -= deltaTime;
		if (timeLeft < 0)
		{
			remove(target);
		}
	}

	public void nextFrame(int frameNum)
	{
		switch (name)
		{
		case "Healed":
			break;
		case "Burning":
			if (frameNum % 17 == 0)
				animFrame++;
			if (animFrame >= Resources.effects.get(0).size())
				animFrame = 0;
			break;
		default:
			Main.errorMessage(
					"They call me 'Bell',\nThey call me 'Stacey',\nThey call me 'her',\nThey call me 'Jane',\nThat's not my name!\nThat's not my name!\nThat's not my name!\nThat's not my...name!\n");
			Main.errorMessage();
			if (Main.random.nextInt(5) == 0)
			{
				Main.errorMessage("(ame, ame, ame)");
				Main.errorMessage();
			}
			break;
		}
	}
}