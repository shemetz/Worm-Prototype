package mainClasses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class MenuThingie extends MenuElement
{
	BufferedImage image;
	boolean on = false; // whether or not it was clicked
	boolean available = true; // whether or not element is available for currently selected ability

	public MenuThingie(double x1, double y1, String type1, String text1)
	{
		x = (int) x1;
		y = (int) y1;
		width = 60; // temp ?
		height = 60; //
		type = Type.valueOf(type1);
		text = text1;
		switch (type)
		{
		case CHEATS_ELEMENT:
		case CHEATS_ABILITY:
			image = Resources.icons.get(text);
			break;
		default:
			MAIN.errorMessage("nope, something bad happening.    +" + type);
		}
	}

	public void draw(Graphics2D buffer)
	{
		buffer.setStroke(new BasicStroke(3));
		buffer.setColor(Color.black);
		buffer.drawRect(x, y, width, height);
		if (selected || on)
		{
			buffer.setColor(Color.orange);
			buffer.setStroke(new BasicStroke(1));
			buffer.drawRect(x, y, width, height);
		}
		buffer.setColor(new Color(255, 255, 255, 100));
		buffer.fillRect(x, y, width, height);
		buffer.drawImage(image, x, y, null);
		
		if (!available)
		{
			buffer.setStroke(new BasicStroke(5));
			buffer.setColor(Color.red);
			buffer.drawLine(x, y, x+width, y+height);
			buffer.drawLine(x+width, y, x, y+height);
		}
	}
}
