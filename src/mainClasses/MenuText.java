package mainClasses;

public class MenuText
{
	int		x, y;
	int		width, height;
	String	text;

	public enum Type
	{
		EXIT_GAME("Quit to desktop"), RESUME("Resume"), TEXT("");

		String text;

		Type(String t)
		{
			text = t;
		}
	};

	Type	type;
	boolean	clickable;
	boolean selected = false;

	public MenuText(int x1, int y1, int w1, int h1, String string)
	{
		try
		{
			// Button
			Type t = Type.valueOf(string.toUpperCase());
			type = t;
			text = t.text;
			clickable = true;
		} catch (IllegalArgumentException e)
		{
			// Text
			text = string;
			type = Type.TEXT;
			clickable = false;
		}
		x = x1;
		y = y1;
		width = w1;
		height = h1;
	}

	public boolean contains(int mx, int my)
	{
		return (mx >= x && mx < x + width && my >= y && my < y + height);
	}
	//draw is in Main
}
