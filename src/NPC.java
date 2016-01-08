
public class NPC extends Person
{
	String	strategy;	// "aggressive" = attack enemies if possible, then heal/buff and follow if possible.
						// "defensive" = push away enemies and block them if possible, run away if possible.
						// "passive" = does nothing.
						//
	String	tactic;		// "circle strafing" = move in a constant angular direction around the target, shooting at it when able. When hitting something or randomly while moving, change direction.
						// "move into position" = move towards or away from the target until in optimalRange, strafing left or right when being hit by something. If can't see target, stop.
	boolean	hasAllies;

	int		targetID				= -1;
	boolean	rightOrLeft; //true = right or CW. false = left or CCW.
	boolean justCollided = false;
	boolean justGotHit = false;

	public NPC(int x1, int y1, String s1)
	{
		super(x1, y1);
		hasAllies = false;
		// TEMP
		strategy = s1;
		tactic = "no target";
		rightOrLeft = true;
	}

	public void setCommander(int comID)
	{
		commanderID = comID;
		hasAllies = true;
	}

	public boolean viableTarget (Person enemy)
	{
		if (enemy.commanderID == commanderID)
			return false;
		if (enemy.z >= z + 1)
			return false;
		return true;
	}
}
