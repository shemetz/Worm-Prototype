package mainClasses;

import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Map;

public class Player extends Person
{
	public int[] hotkeys; // Mid-Click, Shift, Q, E, R, F, V, C, X, Z

	public enum AimType
	{
		CREATE_FF, PORTALS, CREATE_IN_GRID, TARGET_IN_RANGE, EXPLOSION, TELEPORT, LOOP, WILD_POWER, CLONE, AIMLESS, NONE
	};

	public AimType aimType;

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
	boolean limitedVisibility = true;
	Map<Environment, Area> visibleRememberArea;

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
		visibleRememberArea = new HashMap<Environment, Area>();
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
		hotkeys = new int[10];
		int k = 0;
		for (int i = 0; i < abilities.size() && k < hotkeys.length; i++, k++)
			if (abilities.get(i).hasTag("passive"))
				k--;
			else
				hotkeys[k] = i;
		if (k < hotkeys.length)
			for (; k < hotkeys.length; k++)
				hotkeys[k] = -1;
	}
}
