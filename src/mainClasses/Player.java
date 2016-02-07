package mainClasses;

import java.awt.geom.Area;

public class Player extends Person
{
	public int[] hotkeys; // Mid-Click, Shift, Q, E, R, F, V, C, X, Z
	public String targetType = "";

	public boolean leftMousePressed;
	public boolean rightMousePressed = false;
	public boolean leftPressed, rightPressed, upPressed, downPressed;
	public boolean ctrlPressed = false;
	public boolean resizeUIButtonPressed = false;
	public boolean spacePressed = false;
	public boolean rotateButtonPressed = false;
	public boolean successfulTarget;
	public double portalMovementRotation = 0;
	public double portalCameraRotation = 0;
	public boolean[] wasdPortalArray; // used to disable/enable movement axis rotation
	Area visibleRememberArea = null;
	boolean limitedVisibility = true;

	// maybe hotkeys shouldn't belong to the player?

	public Player(int x1, int y1)
	{
		super(x1, y1);
		hotkeys = new int[10];
		for (int i = 0; i < 10; i++)
			hotkeys[i] = -1;
		wasdPortalArray = new boolean[4];
		for (int i = 0; i < 4; i++)
			wasdPortalArray[i] = false;
	}

	public void updateSubStats()
	{
		basicUpdateSubStats();
		// TODO evasion
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
	}
}
