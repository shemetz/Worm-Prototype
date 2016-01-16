package mainClasses;
import java.awt.Color;

public class UIText
{
	int x, y;
	String text;
	Color color;
	int fontSize;
	int transparency;

	public UIText(int x1, int y1, String t1, Color c1)
	{
		x = x1;
		y = y1;
		text = t1;
		color = c1;
		fontSize = 16;
		transparency = 255;
	}

	public UIText(int x1, int y1, String t1, int type)
	{
		x = x1;
		y = y1;
		text = t1;
		transparency = 255;
		switch (type)
		{
		case 0: // black text
			color = Color.black;
			break;
		case 1: // damage text
			color = Color.red;
			break;
		case 2: // heal text
			color = Color.green;
			break;
		case 3: // shield / Force Field damage text
			color = new Color(0, 220, 255); //cyan-ish
			break;
		case 4: // armor damage text
			color = Color.yellow;
			break;
		case 5: // object damage text (e.g. walls)
			color = new Color(80, 80, 80); //dark gray
			break;
		default:
			Main.errorMessage("Unknown UItext damage type number: " + type);
			color = Color.white;
			break;
		}
		//TODO make it different
		fontSize = 16;
	}
}
