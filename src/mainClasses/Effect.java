package mainClasses;

public class Effect
{
	double	duration;	// -1 = forever
	double	timeLeft;	// -1 = forever
	String	name;		// can be an int or a short, honestly, but then the programming would be difficulter
	int		strength;
	int		animFrame;

	public Effect(double duration1, String type, int strength1)
	{
		duration = duration1;
		timeLeft = duration;
		name = type;
		strength = strength1;
		animFrame = 0;
	}

	// A constructor for indefinite effects
	public Effect(String type, int strength1)
	{
		duration = -1;
		timeLeft = -1;
		name = type;
		strength = strength1;
		animFrame = 0;
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
			// error message
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
