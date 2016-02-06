package mainClasses;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class UIText
{
	int x, y;
	String text;
	Color color;
	Color altColor; // drawn behind the color
	int fontSize;
	int transparency;
	int type;

	public UIText(int x1, int y1, String t1, Color c1)
	{
		x = x1;
		y = y1;
		text = t1;
		color = c1;
		altColor = Color.black;
		fontSize = 16;
		transparency = 255;
	}

	public UIText(int x1, int y1, String text1, int type1)
	{
		x = x1;
		y = y1;
		text = text1;
		transparency = 255;
		type = type1;
		switch (type)
		{
		case 0: // black text
			color = Color.black;
			altColor = Color.white;
			fontSize = 16;
			break;
		case 1: // damage text
			color = Color.red;
			altColor = Color.black;
			fontSize = Integer.parseInt(text) * 2;
			fontSize = Math.max(12, fontSize);
			fontSize = Math.min(50, fontSize);
			break;
		case 2: // heal text
			color = Color.green;
			altColor = Color.black;
			fontSize = 16;
			break;
		case 3: // shield / Force Field damage text
			color = new Color(0, 220, 255); // cyan-ish
			altColor = Color.black;
			fontSize = Integer.parseInt(text) * 2;
			fontSize = Math.max(12, fontSize);
			fontSize = Math.min(30, fontSize);
			break;
		case 4: // armor damage text
			color = Color.yellow;
			altColor = Color.black;
			fontSize = 16;
			fontSize = Integer.parseInt(text) * 2;
			fontSize = Math.max(12, fontSize);
			fontSize = Math.min(30, fontSize);
			break;
		case 5: // object damage text (e.g. walls)
			color = new Color(80, 80, 80); // dark gray
			altColor = Color.white;
			fontSize = 16;
			fontSize = Integer.parseInt(text) * 2;
			fontSize = Math.max(12, fontSize);
			fontSize = Math.min(30, fontSize);
			break;
		case 6: // evasion text
			color = new Color(255, 255, 255); // dark gray
			altColor = Color.white;
			fontSize = 22;
			break;
		default:
			MAIN.errorMessage("Unknown UItext damage type number: " + type);
			color = Color.white;
			altColor = Color.black;
			fontSize = 16;
			break;
		}
	}
	
	public void addAmount(int amount)
	{
		//assumes type = damage type
		text = ""+(Integer.parseInt(text)+amount);
		fontSize = Integer.parseInt(text) * 2;
		fontSize = Math.max(12, fontSize);
		fontSize = Math.min(50, fontSize);
	}

	public void draw(Graphics2D buffer, double originX, double originY)
	{
		buffer.setFont(new Font("Sans-Serif", Font.BOLD, fontSize));
		// bg (altColor)
		buffer.setColor(new Color(altColor.getRed(), altColor.getGreen(), altColor.getBlue(), transparency));
		buffer.drawString(text, (int) originX + x - 1, (int) originY + y - 1);
		buffer.drawString(text, (int) originX + x - 1, (int) originY + y + 1);
		buffer.drawString(text, (int) originX + x + 1, (int) originY + y - 1);
		buffer.drawString(text, (int) originX + x + 1, (int) originY + y + 1);
		// text (color)
		buffer.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), transparency));
		buffer.drawString(text, (int) originX + x, (int) originY + y);
	}
}
