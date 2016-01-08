
public class Player extends Person
{
	int[]	hotkeys;								// Mid-Click, Shift, Q, E, R, F, V, C, X, Z
	int		numOfHotkeys					= -1;
	String targetType = "";
	
	// maybe hotkeys shouldn't belong to the player?

	public Player(int x1, int y1)
	{
		super(x1, y1);
		hotkeys = new int[10];
		for (int i = 0; i < 10; i++)
			hotkeys[i] = -1;
	}

	public void updateAbilities()
	{
		selfFrame(0);
		defaultHotkeys();
	}

	public void defaultHotkeys()
	{
		int k = 0;
		for (int i = 0; i < abilities.size() && k < hotkeys.length; i++, k++)
			if (abilities.get(i).cooldown != -1)
				hotkeys[k] = i;
			else
			{
				k--;
			}
		numOfHotkeys = k;
	}

	public int checkHotkey(int num)
	{
		// returns num of ability in player's abilities list, or 0 if there's none
		return hotkeys[num];
	}
}
