package mainClasses;

import java.awt.Graphics2D;

public class MenuElement
{
	int x, y, width, height;
	String text;
	Type type;
	boolean selected = false;
	boolean clickable = true;

	enum Type
	{
		CHEATS_ELEMENT, CHEATS_ABILITY, EXIT_GAME, RESUME, TEXT, CHEATS, CHEATS_RESULT_ABILITY, ICON, ABILITIES;
	}

	public boolean contains(int mx, int my)
	{
		return (mx >= x && mx < x + width && my >= y && my < y + height);
	}

	@SuppressWarnings("unused")
	public void draw(Graphics2D buffer)
	{
		MAIN.errorMessage("lllklklkkllllkklk");
	}
}
